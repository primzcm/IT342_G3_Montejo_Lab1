package com.collabmatch.mobile.feature.profile.data

data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val firstname: String,
    val lastname: String,
    val role: String,
    val createdAt: String,
    val bio: String?,
    val skills: String?
)
