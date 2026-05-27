package com.collabmatch.mobile.core.network

data class ApiError(
    val code: String,
    val message: String,
    val details: Any?
)

