package com.collabmatch.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProjectDao {
    @Query("SELECT * FROM cached_projects ORDER BY createdAt DESC")
    suspend fun getAllProjects(): List<CachedProjectEntity>

    @Query("SELECT * FROM cached_projects WHERE id = :projectId LIMIT 1")
    suspend fun getProject(projectId: Long): CachedProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(project: CachedProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(projects: List<CachedProjectEntity>)

    @Query("DELETE FROM cached_projects")
    suspend fun clear()

    @Query("DELETE FROM cached_projects WHERE id = :projectId")
    suspend fun deleteById(projectId: Long)
}
