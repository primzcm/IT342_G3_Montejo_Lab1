package com.collabmatch.mobile.feature.projects.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_projects")
data class CachedProjectEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val rolesNeeded: String,
    val requiredSkillsSerialized: String,
    val status: String,
    val createdAt: String,
    val ownerId: Long,
    val ownerName: String,
    val owner: Boolean,
    val joined: Boolean,
    val joinRequested: Boolean
)
