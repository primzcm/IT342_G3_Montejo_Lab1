package com.collabmatch.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedUserEntity::class,
        CachedProjectEntity::class,
        CachedProjectMemberEntity::class,
        CachedJoinRequestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun projectMemberDao(): ProjectMemberDao
    abstract fun joinRequestDao(): JoinRequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "collabmatch_room.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
