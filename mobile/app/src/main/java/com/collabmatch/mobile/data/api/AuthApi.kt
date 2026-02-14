package com.collabmatch.mobile.data.api

import com.collabmatch.mobile.data.model.ApiResponse
import com.collabmatch.mobile.data.model.AuthPayload
import com.collabmatch.mobile.data.model.LoginRequest
import com.collabmatch.mobile.data.model.RegisterRequest
import com.collabmatch.mobile.data.model.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthPayload>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthPayload>>

    @GET("api/user/me")
    suspend fun me(@Header("Authorization") authorization: String): Response<ApiResponse<UserDto>>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") authorization: String): Response<ApiResponse<Unit>>
}
