package com.collabmatch.mobile.core.data

import com.collabmatch.mobile.core.network.CollabMatchApi
import com.collabmatch.mobile.core.network.ApiResponse
import com.collabmatch.mobile.feature.auth.data.AuthPayload
import com.collabmatch.mobile.feature.projects.data.CreateJoinRequest
import com.collabmatch.mobile.feature.projects.data.CreateProjectMessageRequest
import com.collabmatch.mobile.feature.projects.data.CreateProjectRequest
import com.collabmatch.mobile.feature.projects.data.JoinRequestDto
import com.collabmatch.mobile.feature.auth.data.LoginRequest
import com.collabmatch.mobile.feature.auth.data.RegisterRequest
import com.collabmatch.mobile.feature.projects.data.ProjectDetailDto
import com.collabmatch.mobile.feature.projects.data.ProjectMessageDto
import com.collabmatch.mobile.feature.projects.data.ProjectMemberDto
import com.collabmatch.mobile.feature.projects.data.ProjectSummaryDto
import com.collabmatch.mobile.feature.projects.data.UpdateProjectRequest
import com.collabmatch.mobile.feature.profile.data.UpdateUserProfileRequest
import com.collabmatch.mobile.feature.profile.data.UserDto
import com.collabmatch.mobile.core.database.AppDatabase
import com.collabmatch.mobile.feature.projects.data.CachedJoinRequestEntity
import com.collabmatch.mobile.feature.projects.data.CachedProjectEntity
import com.collabmatch.mobile.feature.projects.data.CachedProjectMemberEntity
import com.collabmatch.mobile.core.session.SessionManager
import com.collabmatch.mobile.feature.profile.data.CachedUserEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonObject
import retrofit2.Response

class CollabMatchRepository(
    private val collabMatchApi: CollabMatchApi,
    private val sessionManager: SessionManager,
    private val appDatabase: AppDatabase
) {
    private val gson = Gson()
    private val userDao = appDatabase.userDao()
    private val projectDao = appDatabase.projectDao()
    private val projectMemberDao = appDatabase.projectMemberDao()
    private val joinRequestDao = appDatabase.joinRequestDao()

    suspend fun register(firstname: String, lastname: String, email: String, password: String): RepoResult<AuthPayload> {
        val response = collabMatchApi.register(RegisterRequest(email, firstname, lastname, password))
        return mapAuthResponse(response)
    }

    suspend fun login(email: String, password: String): RepoResult<AuthPayload> {
        val response = collabMatchApi.login(LoginRequest(email, password))
        return mapAuthResponse(response)
    }

    suspend fun fetchCurrentUser(): RepoResult<UserDto> {
        val token = sessionManager.getAccessToken() ?: return RepoResult.Error("Please login first")
        val response = collabMatchApi.me("Bearer $token")
        return when (val result = mapResponse(response)) {
            is RepoResult.Success -> {
                userDao.upsert(result.data.toEntity())
                result
            }

            is RepoResult.Error -> {
                if (isAuthenticationError(result.message)) {
                    result
                } else {
                    userDao.getCurrentUser()?.let { RepoResult.Success(it.toDto()) } ?: result
                }
            }
        }
    }

    suspend fun updateCurrentUser(
        firstname: String,
        lastname: String,
        bio: String,
        skills: String
    ): RepoResult<UserDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(
            collabMatchApi.updateMe(
                token,
                UpdateUserProfileRequest(
                    firstname = firstname,
                    lastname = lastname,
                    bio = bio.trim().ifBlank { null },
                    skills = skills.trim().ifBlank { null }
                )
            )
        )
    }

    suspend fun logout(): RepoResult<Unit> {
        val accessToken = sessionManager.getAccessToken() ?: return RepoResult.Success(Unit)
        val refreshToken = sessionManager.getRefreshToken() ?: return RepoResult.Success(Unit)
        val response = collabMatchApi.logout("Bearer $accessToken", com.collabmatch.mobile.feature.auth.data.LogoutRequest(refreshToken))
        sessionManager.clearToken()
        clearLocalCache()
        return if (response.isSuccessful) RepoResult.Success(Unit) else RepoResult.Error(extractErrorMessage(response))
    }

    suspend fun fetchProjects(): RepoResult<List<ProjectSummaryDto>> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return when (val result = mapResponse(collabMatchApi.fetchProjects(token))) {
            is RepoResult.Success -> {
                projectDao.clear()
                projectDao.upsertAll(result.data.map { it.toEntity() })
                result
            }

            is RepoResult.Error -> {
                if (isAuthenticationError(result.message)) {
                    result
                } else {
                    val cached = projectDao.getAllProjects().map { it.toDto() }
                    if (cached.isNotEmpty()) RepoResult.Success(cached) else result
                }
            }
        }
    }

    suspend fun fetchProjectDetail(projectId: Long): RepoResult<ProjectDetailDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return when (val result = mapResponse(collabMatchApi.fetchProjectDetail(token, projectId))) {
            is RepoResult.Success -> {
                cacheProjectDetail(result.data)
                result
            }

            is RepoResult.Error -> {
                if (isAuthenticationError(result.message)) {
                    result
                } else {
                    cachedProjectDetail(projectId)?.let { RepoResult.Success(it) } ?: result
                }
            }
        }
    }

    suspend fun createProject(
        title: String,
        description: String,
        category: String,
        rolesNeeded: String
    ): RepoResult<ProjectSummaryDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(
            collabMatchApi.createProject(
                token,
                CreateProjectRequest(
                    title = title,
                    description = description,
                    category = category,
                    rolesNeeded = rolesNeeded,
                    requiredSkills = parseSkills(rolesNeeded)
                )
            )
        ).alsoIfSuccess { projectDao.upsert(it.toEntity()) }
    }

    suspend fun updateProject(
        projectId: Long,
        title: String,
        description: String,
        category: String,
        rolesNeeded: String,
        status: String
    ): RepoResult<ProjectSummaryDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(
            collabMatchApi.updateProject(
                token,
                projectId,
                UpdateProjectRequest(
                    title = title,
                    description = description,
                    category = category,
                    rolesNeeded = rolesNeeded,
                    requiredSkills = parseSkills(rolesNeeded),
                    status = status
                )
            )
        ).alsoIfSuccess { projectDao.upsert(it.toEntity()) }
    }

    suspend fun deleteProject(projectId: Long): RepoResult<Unit> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        val response = collabMatchApi.deleteProject(token, projectId)
        return if (response.isSuccessful) {
            projectDao.deleteById(projectId)
            projectMemberDao.clearForProject(projectId)
            joinRequestDao.clearForProject(projectId)
            RepoResult.Success(Unit)
        } else {
            RepoResult.Error(extractErrorMessage(response))
        }
    }

    suspend fun requestToJoinProject(projectId: Long, message: String = ""): RepoResult<JoinRequestDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(collabMatchApi.requestToJoinProject(token, projectId, CreateJoinRequest(message))).alsoIfSuccess {
            projectDao.getProject(projectId)?.let { cached ->
                projectDao.upsert(cached.copy(joinRequested = true))
            }
            joinRequestDao.upsert(it.toEntity())
        }
    }

    suspend fun fetchProjectRequests(projectId: Long): RepoResult<List<JoinRequestDto>> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return when (val result = mapResponse(collabMatchApi.fetchProjectRequests(token, projectId))) {
            is RepoResult.Success -> {
                joinRequestDao.clearForProject(projectId)
                joinRequestDao.upsertAll(result.data.map { it.toEntity() })
                result
            }

            is RepoResult.Error -> {
                if (isAuthenticationError(result.message)) {
                    result
                } else {
                    val cached = joinRequestDao.getRequestsForProject(projectId).map { it.toDto() }
                    if (cached.isNotEmpty()) RepoResult.Success(cached) else result
                }
            }
        }
    }

    suspend fun fetchProjectMessages(projectId: Long): RepoResult<List<ProjectMessageDto>> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(collabMatchApi.fetchProjectMessages(token, projectId))
    }

    suspend fun createProjectMessage(projectId: Long, content: String): RepoResult<ProjectMessageDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(collabMatchApi.createProjectMessage(token, projectId, CreateProjectMessageRequest(content.trim())))
    }

    suspend fun approveJoinRequest(requestId: Long): RepoResult<JoinRequestDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(collabMatchApi.approveJoinRequest(token, requestId)).alsoIfSuccess {
            joinRequestDao.upsert(it.toEntity())
        }
    }

    suspend fun rejectJoinRequest(requestId: Long): RepoResult<JoinRequestDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(collabMatchApi.rejectJoinRequest(token, requestId)).alsoIfSuccess {
            joinRequestDao.upsert(it.toEntity())
        }
    }

    fun saveTokens(accessToken: String, refreshToken: String) = sessionManager.saveTokens(accessToken, refreshToken)

    fun getAccessToken(): String? = sessionManager.getAccessToken()

    fun isAuthenticationError(message: String?): Boolean {
        val normalized = message?.trim()?.lowercase().orEmpty()
        return normalized.contains("invalid token") ||
            normalized.contains("unauthorized") ||
            normalized.contains("please login first") ||
            normalized.contains("forbidden") ||
            normalized.contains("jwt")
    }

    private fun bearerToken(): String? = sessionManager.getAccessToken()?.let { "Bearer $it" }

    private suspend fun mapAuthResponse(response: Response<ApiResponse<AuthPayload>>): RepoResult<AuthPayload> {
        if (!response.isSuccessful) {
            val message = extractErrorMessage(response)
            clearSessionIfUnauthorized(response, message)
            return RepoResult.Error(message)
        }

        val body = response.body()
        val data = body?.data
        return if (body?.success == true && data != null) {
            RepoResult.Success(data)
        } else {
            RepoResult.Error(body?.error?.message ?: "Request failed")
        }
    }

    private suspend fun <T> mapResponse(response: Response<ApiResponse<T>>): RepoResult<T> {
        if (!response.isSuccessful) {
            val message = extractErrorMessage(response)
            clearSessionIfUnauthorized(response, message)
            return RepoResult.Error(message)
        }

        val body = response.body()
        val data = body?.data
        return if (body?.success == true && data != null) {
            RepoResult.Success(data)
        } else {
            RepoResult.Error(body?.error?.message ?: "Request failed")
        }
    }

    private fun extractErrorMessage(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()
        if (errorBody.isNullOrBlank()) {
            return "Request failed"
        }

        return try {
            val json = gson.fromJson(errorBody, JsonObject::class.java)
            val error = json.getAsJsonObject("error")
            val details = error?.get("details")
            val detailMessage = when {
                details == null -> null
                details.isJsonPrimitive -> details.asString
                details.isJsonObject -> {
                    val obj = details.asJsonObject
                    if (obj.entrySet().isNotEmpty()) obj.entrySet().first().value.asString else null
                }
                else -> null
            }

            detailMessage ?: error?.get("message")?.asString ?: "Request failed"
        } catch (_: Exception) {
            "Request failed"
        }
    }

    private fun parseSkills(raw: String): List<String> {
        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    private suspend fun clearLocalCache() {
        userDao.clear()
        projectDao.clear()
        projectMemberDao.clear()
        joinRequestDao.clear()
    }

    private suspend fun clearSessionIfUnauthorized(response: Response<*>, message: String) {
        if (response.code() == 401 || response.code() == 403 || isAuthenticationError(message)) {
            sessionManager.clearToken()
            clearLocalCache()
        }
    }

    private suspend fun cacheProjectDetail(project: ProjectDetailDto) {
        projectDao.upsert(project.toEntity())
        projectMemberDao.clearForProject(project.id)
        projectMemberDao.upsertAll(project.members.map { it.toEntity(project.id) })
    }

    private suspend fun cachedProjectDetail(projectId: Long): ProjectDetailDto? {
        val project = projectDao.getProject(projectId) ?: return null
        val members = projectMemberDao.getMembersForProject(projectId).map { it.toDto() }
        return project.toDetailDto(members)
    }

    private suspend fun <T> RepoResult<T>.alsoIfSuccess(block: suspend (T) -> Unit): RepoResult<T> {
        return when (this) {
            is RepoResult.Success -> {
                block(data)
                this
            }

            is RepoResult.Error -> this
        }
    }

    private fun UserDto.toEntity() = CachedUserEntity(id, username, email, firstname, lastname, role, createdAt, bio, skills)

    private fun CachedUserEntity.toDto() = UserDto(id, username, email, firstname, lastname, role, createdAt, bio, skills)

    private fun ProjectSummaryDto.toEntity() = CachedProjectEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        rolesNeeded = rolesNeeded,
        requiredSkillsSerialized = gson.toJson(requiredSkills),
        status = status,
        createdAt = createdAt,
        ownerId = ownerId,
        ownerName = ownerName,
        owner = owner,
        joined = joined,
        joinRequested = joinRequested
    )

    private fun ProjectDetailDto.toEntity() = CachedProjectEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        rolesNeeded = rolesNeeded,
        requiredSkillsSerialized = gson.toJson(requiredSkills),
        status = status,
        createdAt = createdAt,
        ownerId = ownerId,
        ownerName = ownerName,
        owner = owner,
        joined = joined,
        joinRequested = joinRequested
    )

    private fun CachedProjectEntity.toDto(): ProjectSummaryDto = ProjectSummaryDto(
        id = id,
        title = title,
        description = description,
        category = category,
        rolesNeeded = rolesNeeded,
        requiredSkills = deserializeSkills(requiredSkillsSerialized),
        status = status,
        createdAt = createdAt,
        ownerId = ownerId,
        ownerName = ownerName,
        owner = owner,
        joined = joined,
        joinRequested = joinRequested
    )

    private fun CachedProjectEntity.toDetailDto(members: List<ProjectMemberDto>): ProjectDetailDto = ProjectDetailDto(
        id = id,
        title = title,
        description = description,
        category = category,
        rolesNeeded = rolesNeeded,
        requiredSkills = deserializeSkills(requiredSkillsSerialized),
        status = status,
        createdAt = createdAt,
        ownerId = ownerId,
        ownerName = ownerName,
        owner = owner,
        joined = joined,
        joinRequested = joinRequested,
        members = members
    )

    private fun ProjectMemberDto.toEntity(projectId: Long) = CachedProjectMemberEntity(projectId, userId, name, joinedAt)

    private fun CachedProjectMemberEntity.toDto() = ProjectMemberDto(userId, name, joinedAt)

    private fun JoinRequestDto.toEntity() = CachedJoinRequestEntity(id, projectId, requesterId, requesterName, status, message, createdAt, reviewedAt)

    private fun CachedJoinRequestEntity.toDto() = JoinRequestDto(id, projectId, requesterId, requesterName, status, message, createdAt, reviewedAt)

    private fun deserializeSkills(serialized: String): List<String> {
        return runCatching {
            gson.fromJson<List<String>>(serialized, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        }.getOrDefault(emptyList())
    }
}
