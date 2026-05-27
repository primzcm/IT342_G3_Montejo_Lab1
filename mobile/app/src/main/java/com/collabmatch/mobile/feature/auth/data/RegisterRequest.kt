package com.collabmatch.mobile.feature.auth.data

data class RegisterRequest(
    val email: String,
    val firstname: String,
    val lastname: String,
    val password: String
)
