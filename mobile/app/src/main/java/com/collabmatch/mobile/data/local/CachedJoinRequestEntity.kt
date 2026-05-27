package com.collabmatch.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_join_requests")
data class CachedJoinRequestEntity(
    @PrimaryKey val id: Long,
    val projectId: Long,
    val requesterId: Long,
    val requesterName: String,
    val status: String,
    val message: String?,
    val createdAt: String,
    val reviewedAt: String?
)
