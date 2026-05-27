package com.collabmatch.mobile.feature.projects.data

import androidx.room.Entity

@Entity(
    tableName = "cached_project_members",
    primaryKeys = ["projectId", "userId"]
)
data class CachedProjectMemberEntity(
    val projectId: Long,
    val userId: Long,
    val name: String,
    val joinedAt: String
)
