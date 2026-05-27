package com.collabmatch.mobile.feature.projects.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface JoinRequestDao {
    @Query("SELECT * FROM cached_join_requests WHERE projectId = :projectId ORDER BY createdAt DESC")
    suspend fun getRequestsForProject(projectId: Long): List<CachedJoinRequestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(requests: List<CachedJoinRequestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(request: CachedJoinRequestEntity)

    @Query("DELETE FROM cached_join_requests WHERE projectId = :projectId")
    suspend fun clearForProject(projectId: Long)

    @Query("DELETE FROM cached_join_requests")
    suspend fun clear()
}
