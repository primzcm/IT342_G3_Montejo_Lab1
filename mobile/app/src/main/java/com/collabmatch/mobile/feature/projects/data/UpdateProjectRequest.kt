package com.collabmatch.mobile.feature.projects.data

data class UpdateProjectRequest(
    val title: String,
    val description: String,
    val category: String,
    val rolesNeeded: String,
    val requiredSkills: List<String>,
    val status: String
)
