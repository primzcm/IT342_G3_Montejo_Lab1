package com.collabmatch.mobile.data.repository

import com.collabmatch.mobile.data.api.AuthApi
import com.collabmatch.mobile.data.model.ApiResponse
import com.collabmatch.mobile.data.model.AuthPayload
import com.collabmatch.mobile.data.model.CreateJoinRequest
import com.collabmatch.mobile.data.model.CreateProjectRequest
import com.collabmatch.mobile.data.model.JoinRequestDto
import com.collabmatch.mobile.data.model.LoginRequest
import com.collabmatch.mobile.data.model.RegisterRequest
import com.collabmatch.mobile.data.model.ProjectDetailDto
import com.collabmatch.mobile.data.model.ProjectSummaryDto
import com.collabmatch.mobile.data.model.UpdateProjectRequest
import com.collabmatch.mobile.data.model.UserDto
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) {
    private val gson = Gson()

    suspend fun register(firstname: String, lastname: String, email: String, password: String): RepoResult<AuthPayload> {
        val response = authApi.register(RegisterRequest(email, firstname, lastname, password))
        return mapAuthResponse(response)
    }

    suspend fun login(email: String, password: String): RepoResult<AuthPayload> {
        val response = authApi.login(LoginRequest(email, password))
        return mapAuthResponse(response)
    }

    suspend fun fetchCurrentUser(): RepoResult<UserDto> {
        val token = sessionManager.getAccessToken() ?: return RepoResult.Error("Please login first")
        val response = authApi.me("Bearer $token")
        return mapResponse(response)
    }

    suspend fun logout(): RepoResult<Unit> {
        val accessToken = sessionManager.getAccessToken() ?: return RepoResult.Success(Unit)
        val refreshToken = sessionManager.getRefreshToken() ?: return RepoResult.Success(Unit)
        val response = authApi.logout("Bearer $accessToken", com.collabmatch.mobile.data.model.LogoutRequest(refreshToken))
        sessionManager.clearToken()
        return if (response.isSuccessful) RepoResult.Success(Unit) else RepoResult.Error(extractErrorMessage(response))
    }

    suspend fun fetchProjects(): RepoResult<List<ProjectSummaryDto>> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(authApi.fetchProjects(token))
    }

    suspend fun fetchProjectDetail(projectId: Long): RepoResult<ProjectDetailDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(authApi.fetchProjectDetail(token, projectId))
    }

    suspend fun createProject(
        title: String,
        description: String,
        category: String,
        rolesNeeded: String
    ): RepoResult<ProjectSummaryDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(
            authApi.createProject(
                token,
                CreateProjectRequest(
                    title = title,
                    description = description,
                    category = category,
                    rolesNeeded = rolesNeeded
                )
            )
        )
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
            authApi.updateProject(
                token,
                projectId,
                UpdateProjectRequest(
                    title = title,
                    description = description,
                    category = category,
                    rolesNeeded = rolesNeeded,
                    status = status
                )
            )
        )
    }

    suspend fun deleteProject(projectId: Long): RepoResult<Unit> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        val response = authApi.deleteProject(token, projectId)
        return if (response.isSuccessful) RepoResult.Success(Unit) else RepoResult.Error(extractErrorMessage(response))
    }

    suspend fun requestToJoinProject(projectId: Long, message: String = ""): RepoResult<JoinRequestDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(authApi.requestToJoinProject(token, projectId, CreateJoinRequest(message)))
    }

    suspend fun fetchProjectRequests(projectId: Long): RepoResult<List<JoinRequestDto>> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(authApi.fetchProjectRequests(token, projectId))
    }

    suspend fun approveJoinRequest(requestId: Long): RepoResult<JoinRequestDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(authApi.approveJoinRequest(token, requestId))
    }

    suspend fun rejectJoinRequest(requestId: Long): RepoResult<JoinRequestDto> {
        val token = bearerToken() ?: return RepoResult.Error("Please login first")
        return mapResponse(authApi.rejectJoinRequest(token, requestId))
    }

    fun saveTokens(accessToken: String, refreshToken: String) = sessionManager.saveTokens(accessToken, refreshToken)

    fun getAccessToken(): String? = sessionManager.getAccessToken()

    private fun bearerToken(): String? = sessionManager.getAccessToken()?.let { "Bearer $it" }

    private fun mapAuthResponse(response: Response<ApiResponse<AuthPayload>>): RepoResult<AuthPayload> {
        if (!response.isSuccessful) {
            return RepoResult.Error(extractErrorMessage(response))
        }

        val body = response.body()
        val data = body?.data
        return if (body?.success == true && data != null) {
            RepoResult.Success(data)
        } else {
            RepoResult.Error(body?.error?.message ?: "Request failed")
        }
    }

    private fun <T> mapResponse(response: Response<ApiResponse<T>>): RepoResult<T> {
        if (!response.isSuccessful) {
            return RepoResult.Error(extractErrorMessage(response))
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
}
