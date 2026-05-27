package com.collabmatch.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.collabmatch.mobile.data.model.UserDto
import com.collabmatch.mobile.data.repository.AuthRepository
import com.collabmatch.mobile.data.repository.RepoResult
import com.collabmatch.mobile.ui.theme.AppCard
import com.collabmatch.mobile.ui.theme.AppGradientFrame
import com.collabmatch.mobile.ui.theme.AppMiniButton
import com.collabmatch.mobile.ui.theme.AppPrimaryButton
import com.collabmatch.mobile.ui.theme.AppTextField
import com.collabmatch.mobile.ui.theme.AppTopBar
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    repository: AuthRepository,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    if (repository.getAccessToken().isNullOrBlank()) {
        LaunchedEffect(Unit) {
            onLoggedOut()
        }
        return
    }

    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserDto?>(null) }
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }

    fun populate(userDto: UserDto) {
        user = userDto
        firstname = userDto.firstname
        lastname = userDto.lastname
        bio = userDto.bio.orEmpty()
        skills = userDto.skills.orEmpty()
    }

    suspend fun loadProfile() {
        loading = true
        error = null
        when (val result = repository.fetchCurrentUser()) {
            is RepoResult.Success -> populate(result.data)
            is RepoResult.Error -> {
                if (repository.isAuthenticationError(result.message)) {
                    onLoggedOut()
                    return
                }
                error = result.message
            }
        }
        loading = false
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    AppGradientFrame {
        if (loading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text("Loading profile...", color = MaterialTheme.colorScheme.onBackground)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                AppTopBar(
                    title = "Profile",
                    badge = "Account",
                    meta = user?.username ?: "",
                    onBack = onBack,
                    actions = {
                        AppMiniButton(
                            text = "Logout",
                            onClick = {
                                scope.launch {
                                    repository.logout()
                                    onLoggedOut()
                                }
                            }
                        )
                    }
                )

                AppCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Profile details",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    user?.let { currentUser ->
                        Text(
                            text = currentUser.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AppTextField(
                        value = firstname,
                        onValueChange = { firstname = it },
                        label = "First name"
                    )
                    AppTextField(
                        value = lastname,
                        onValueChange = { lastname = it },
                        label = "Last name"
                    )
                    AppTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = "Bio",
                        minLines = 4
                    )
                    AppTextField(
                        value = skills,
                        onValueChange = { skills = it },
                        label = "Skills",
                        minLines = 4
                    )

                    if (!error.isNullOrBlank()) {
                        Text(
                            text = error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!success.isNullOrBlank()) {
                        Text(
                            text = success ?: "",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    AppPrimaryButton(
                        text = if (saving) "Saving..." else "Save Profile",
                        onClick = {
                            scope.launch {
                                saving = true
                                error = null
                                success = null
                                when (val result = repository.updateCurrentUser(firstname.trim(), lastname.trim(), bio, skills)) {
                                    is RepoResult.Success -> {
                                        populate(result.data)
                                        success = "Profile updated."
                                    }

                                    is RepoResult.Error -> {
                                        if (repository.isAuthenticationError(result.message)) {
                                            onLoggedOut()
                                            return@launch
                                        }
                                        error = result.message
                                    }
                                }
                                saving = false
                            }
                        },
                        enabled = !saving && firstname.isNotBlank() && lastname.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
