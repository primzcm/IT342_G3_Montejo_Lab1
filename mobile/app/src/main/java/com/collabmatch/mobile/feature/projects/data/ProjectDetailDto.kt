package com.collabmatch.mobile.feature.projects.data

data class ProjectDetailDto(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val rolesNeeded: String,
    val requiredSkills: List<String>,
    val status: String,
    val createdAt: String,
    val ownerId: Long,
    val ownerName: String,
    val owner: Boolean,
    val joined: Boolean,
    val joinRequested: Boolean,
    val members: List<ProjectMemberDto>
)
