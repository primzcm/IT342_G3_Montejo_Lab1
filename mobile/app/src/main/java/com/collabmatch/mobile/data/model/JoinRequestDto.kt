package com.collabmatch.mobile.data.model

data class JoinRequestDto(
    val id: Long,
    val projectId: Long,
    val requesterId: Long,
    val requesterName: String,
    val status: String,
    val message: String?,
    val createdAt: String,
    val reviewedAt: String?
)
