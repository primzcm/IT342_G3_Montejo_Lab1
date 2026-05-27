package com.collabmatch.mobile.data.local

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
