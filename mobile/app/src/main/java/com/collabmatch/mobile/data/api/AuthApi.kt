package com.collabmatch.mobile.data.api

import com.collabmatch.mobile.data.model.ApiResponse
import com.collabmatch.mobile.data.model.AuthPayload
import com.collabmatch.mobile.data.model.CreateJoinRequest
import com.collabmatch.mobile.data.model.CreateProjectRequest
import com.collabmatch.mobile.data.model.JoinRequestDto
import com.collabmatch.mobile.data.model.LoginRequest
import com.collabmatch.mobile.data.model.LogoutRequest
import com.collabmatch.mobile.data.model.ProjectDetailDto
import com.collabmatch.mobile.data.model.ProjectSummaryDto
import com.collabmatch.mobile.data.model.RegisterRequest
import com.collabmatch.mobile.data.model.UpdateProjectRequest
import com.collabmatch.mobile.data.model.UserDto
import retrofit2.http.DELETE
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path

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

    @GET("api/v1/projects")
    suspend fun fetchProjects(@Header("Authorization") authorization: String): Response<ApiResponse<List<ProjectSummaryDto>>>

    @GET("api/v1/projects/{projectId}")
    suspend fun fetchProjectDetail(
        @Header("Authorization") authorization: String,
        @Path("projectId") projectId: Long
    ): Response<ApiResponse<ProjectDetailDto>>

    @POST("api/v1/projects")
    suspend fun createProject(
        @Header("Authorization") authorization: String,
        @Body request: CreateProjectRequest
    ): Response<ApiResponse<ProjectSummaryDto>>

    @PUT("api/v1/projects/{projectId}")
    suspend fun updateProject(
        @Header("Authorization") authorization: String,
        @Path("projectId") projectId: Long,
        @Body request: UpdateProjectRequest
    ): Response<ApiResponse<ProjectSummaryDto>>

    @DELETE("api/v1/projects/{projectId}")
    suspend fun deleteProject(
        @Header("Authorization") authorization: String,
        @Path("projectId") projectId: Long
    ): Response<ApiResponse<Unit>>

    @POST("api/v1/projects/{projectId}/requests")
    suspend fun requestToJoinProject(
        @Header("Authorization") authorization: String,
        @Path("projectId") projectId: Long,
        @Body request: CreateJoinRequest
    ): Response<ApiResponse<JoinRequestDto>>

    @GET("api/v1/projects/{projectId}/requests")
    suspend fun fetchProjectRequests(
        @Header("Authorization") authorization: String,
        @Path("projectId") projectId: Long
    ): Response<ApiResponse<List<JoinRequestDto>>>

    @PUT("api/v1/requests/{requestId}/approve")
    suspend fun approveJoinRequest(
        @Header("Authorization") authorization: String,
        @Path("requestId") requestId: Long
    ): Response<ApiResponse<JoinRequestDto>>

    @PUT("api/v1/requests/{requestId}/reject")
    suspend fun rejectJoinRequest(
        @Header("Authorization") authorization: String,
        @Path("requestId") requestId: Long
    ): Response<ApiResponse<JoinRequestDto>>
}
