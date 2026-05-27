package com.collabmatch.mobile.data.model

data class UpdateUserProfileRequest(
    val firstname: String,
    val lastname: String,
    val bio: String?,
    val skills: String?
)
