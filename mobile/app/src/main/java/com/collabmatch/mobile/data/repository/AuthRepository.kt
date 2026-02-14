package com.collabmatch.mobile.data.repository

import com.collabmatch.mobile.data.api.AuthApi
import com.collabmatch.mobile.data.model.ApiResponse
import com.collabmatch.mobile.data.model.AuthPayload
import com.collabmatch.mobile.data.model.LoginRequest
import com.collabmatch.mobile.data.model.RegisterRequest
import com.collabmatch.mobile.data.model.UserDto
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) {
    private val gson = Gson()

    suspend fun register(username: String, email: String, password: String): RepoResult<AuthPayload> {
        val response = authApi.register(RegisterRequest(username, email, password))
        return mapAuthResponse(response)
    }

    suspend fun login(username: String, password: String): RepoResult<AuthPayload> {
        val response = authApi.login(LoginRequest(username, password))
        return mapAuthResponse(response)
    }

    suspend fun fetchCurrentUser(): RepoResult<UserDto> {
        val token = sessionManager.getToken() ?: return RepoResult.Error("Please login first")
        val response = authApi.me("Bearer $token")
        return mapResponse(response)
    }

    suspend fun logout(): RepoResult<Unit> {
        val token = sessionManager.getToken() ?: return RepoResult.Success(Unit)
        val response = authApi.logout("Bearer $token")
        sessionManager.clearToken()
        return if (response.isSuccessful) RepoResult.Success(Unit) else RepoResult.Error(extractErrorMessage(response))
    }

    fun saveToken(token: String) = sessionManager.saveToken(token)

    fun getToken(): String? = sessionManager.getToken()

    private fun mapAuthResponse(response: Response<ApiResponse<AuthPayload>>): RepoResult<AuthPayload> {
        if (!response.isSuccessful) {
            return RepoResult.Error(extractErrorMessage(response))
        }

        val body = response.body()
        val data = body?.data
        return if (body?.success == true && data != null) {
            RepoResult.Success(data)
        } else {
            RepoResult.Error(body?.message ?: "Request failed")
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
            RepoResult.Error(body?.message ?: "Request failed")
        }
    }

    private fun extractErrorMessage(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()
        if (errorBody.isNullOrBlank()) {
            return "Request failed"
        }

        return try {
            val json = gson.fromJson(errorBody, JsonObject::class.java)
            val errors = json.getAsJsonObject("errors")
            if (errors != null && errors.entrySet().isNotEmpty()) {
                errors.entrySet().first().value.asString
            } else {
                json.get("message")?.asString ?: "Request failed"
            }
        } catch (_: Exception) {
            "Request failed"
        }
    }
}
