package com.collabmatch.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_user")
data class CachedUserEntity(
    @PrimaryKey val id: Long,
    val username: String,
    val email: String,
    val firstname: String,
    val lastname: String,
    val role: String,
    val createdAt: String,
    val bio: String?,
    val skills: String?
)
