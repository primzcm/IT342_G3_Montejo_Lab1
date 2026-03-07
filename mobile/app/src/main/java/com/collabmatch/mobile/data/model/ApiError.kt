package com.collabmatch.mobile.data.model

data class ApiError(
    val code: String,
    val message: String,
    val details: Any?
)

