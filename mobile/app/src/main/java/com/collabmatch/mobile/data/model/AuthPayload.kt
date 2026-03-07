package com.collabmatch.mobile.data.model

data class AuthPayload(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String
)
