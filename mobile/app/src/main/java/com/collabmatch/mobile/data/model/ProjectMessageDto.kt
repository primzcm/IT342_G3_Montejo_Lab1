package com.collabmatch.mobile.data.model

data class ProjectMessageDto(
    val id: Long,
    val projectId: Long,
    val authorId: Long,
    val authorName: String,
    val content: String,
    val createdAt: String
)
