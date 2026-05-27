package com.collabmatch.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.collabmatch.mobile.data.api.ApiModule
import com.collabmatch.mobile.data.local.AppDatabase
import com.collabmatch.mobile.data.repository.AuthRepository
import com.collabmatch.mobile.data.repository.SessionManager
import com.collabmatch.mobile.ui.navigation.CollabMatchApp
import com.collabmatch.mobile.ui.theme.CollabMatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = AuthRepository(
            authApi = ApiModule.authApi,
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
