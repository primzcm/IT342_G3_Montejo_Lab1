package com.collabmatch.mobile.data.model

data class RegisterRequest(
    val email: String,
    val firstname: String,
    val lastname: String,
    val password: String
)
