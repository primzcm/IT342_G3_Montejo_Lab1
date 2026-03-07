package com.collabmatch.mobile.data.api

import com.collabmatch.mobile.data.model.ApiResponse
import com.collabmatch.mobile.data.model.AuthPayload
import com.collabmatch.mobile.data.model.LoginRequest
import com.collabmatch.mobile.data.model.LogoutRequest
import com.collabmatch.mobile.data.model.RegisterRequest
import com.collabmatch.mobile.data.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthPayload>>

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthPayload>>

    @GET("api/v1/user/me")
    suspend fun me(@Header("Authorization") authorization: String): Response<ApiResponse<UserDto>>

    @POST("api/v1/auth/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String,
        @Body request: LogoutRequest
    ): Response<ApiResponse<Unit>>
}
