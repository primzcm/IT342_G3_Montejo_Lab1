package com.collabmatch.mobile.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.collabmatch.mobile.feature.profile.data.CachedUserEntity
import com.collabmatch.mobile.feature.profile.data.UserDao
import com.collabmatch.mobile.feature.projects.data.CachedJoinRequestEntity
import com.collabmatch.mobile.feature.projects.data.CachedProjectEntity
import com.collabmatch.mobile.feature.projects.data.CachedProjectMemberEntity
import com.collabmatch.mobile.feature.projects.data.JoinRequestDao
import com.collabmatch.mobile.feature.projects.data.ProjectDao
import com.collabmatch.mobile.feature.projects.data.ProjectMemberDao

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
