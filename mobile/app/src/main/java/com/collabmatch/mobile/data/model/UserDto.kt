package com.collabmatch.mobile.data.model

data class UserDto(
    val id: Long,
    val email: String,
    val firstname: String,
    val lastname: String,
    val role: String,
    val createdAt: String
)
