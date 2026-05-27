package com.collabmatch.mobile.feature.projects.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProjectMemberDao {
    @Query("SELECT * FROM cached_project_members WHERE projectId = :projectId ORDER BY joinedAt ASC")
    suspend fun getMembersForProject(projectId: Long): List<CachedProjectMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(members: List<CachedProjectMemberEntity>)

    @Query("DELETE FROM cached_project_members WHERE projectId = :projectId")
    suspend fun clearForProject(projectId: Long)

    @Query("DELETE FROM cached_project_members")
    suspend fun clear()
}
