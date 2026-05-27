package com.collabmatch.mobile.feature.auth.data

import com.collabmatch.mobile.feature.profile.data.UserDto

data class AuthPayload(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String
)
