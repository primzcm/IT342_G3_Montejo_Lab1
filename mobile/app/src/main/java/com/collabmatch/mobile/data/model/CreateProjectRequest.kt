package com.collabmatch.mobile.data.model

data class CreateProjectRequest(
    val title: String,
    val description: String,
    val category: String,
    val rolesNeeded: String,
    val requiredSkills: List<String>
)
