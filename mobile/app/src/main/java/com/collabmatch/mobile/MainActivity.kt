package com.collabmatch.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.collabmatch.mobile.core.data.CollabMatchRepository
import com.collabmatch.mobile.core.database.AppDatabase
import com.collabmatch.mobile.core.network.ApiModule
import com.collabmatch.mobile.core.session.SessionManager
import com.collabmatch.mobile.ui.navigation.CollabMatchApp
import com.collabmatch.mobile.ui.theme.CollabMatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = CollabMatchRepository(
            collabMatchApi = ApiModule.collabMatchApi,
            sessionManager = SessionManager(this),
            appDatabase = AppDatabase.getInstance(this)
        )

        setContent {
            CollabMatchTheme {
                CollabMatchApp(repository = repository)
            }
        }
    }
}
