package com.collabmatch.mobile.feature.profile.data

data class UpdateUserProfileRequest(
    val firstname: String,
    val lastname: String,
    val bio: String?,
    val skills: String?
)
