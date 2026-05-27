package com.collabmatch.mobile.feature.projects.data

data class ProjectMessageDto(
    val id: Long,
    val projectId: Long,
    val authorId: Long,
    val authorName: String,
    val content: String,
    val createdAt: String
)
