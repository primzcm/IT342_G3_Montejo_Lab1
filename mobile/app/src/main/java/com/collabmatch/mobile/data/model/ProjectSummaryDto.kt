package com.collabmatch.mobile.data.model

data class ProjectSummaryDto(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val rolesNeeded: String,
    val status: String,
    val createdAt: String,
    val ownerId: Long,
    val ownerName: String,
    val owner: Boolean,
    val joinRequested: Boolean
)
