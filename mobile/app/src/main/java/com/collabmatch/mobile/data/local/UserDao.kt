package com.collabmatch.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM cached_user LIMIT 1")
    suspend fun getCurrentUser(): CachedUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: CachedUserEntity)

    @Query("DELETE FROM cached_user")
    suspend fun clear()
}
