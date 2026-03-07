package com.collabmatch.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.collabmatch.mobile.data.model.UserDto
import com.collabmatch.mobile.data.repository.AuthRepository
import com.collabmatch.mobile.data.repository.RepoResult
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    repository: AuthRepository,
    onLoggedOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        when (val result = repository.fetchCurrentUser()) {
            is RepoResult.Success -> user = result.data
            is RepoResult.Error -> error = result.message
        }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("CollabMatch Dashboard")

        when {
            loading -> Text("Loading account...")
            error != null -> Text(error ?: "Unable to load account")
            user != null -> {
                Text("Welcome, ${user?.firstname} ${user?.lastname}")
                Text("Email: ${user?.email}")
                Text("User ID: ${user?.id}")
                Text("Joined: ${user?.createdAt}")
            }
        }

        Button(
            onClick = {
                scope.launch {
                    repository.logout()
                    onLoggedOut()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}
