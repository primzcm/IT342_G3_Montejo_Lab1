package com.collabmatch.mobile.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.collabmatch.mobile.data.model.AuthPayload
import com.collabmatch.mobile.data.repository.AuthRepository
import com.collabmatch.mobile.data.repository.RepoResult
import com.collabmatch.mobile.ui.theme.AppFormScreen
import com.collabmatch.mobile.ui.theme.AppPrimaryButton
import com.collabmatch.mobile.ui.theme.AppSecondaryButton
import com.collabmatch.mobile.ui.theme.AppTextField
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: AuthRepository,
    onRegisterClick: () -> Unit,
    onLoginSuccess: (AuthPayload) -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AppFormScreen(
        title = "Welcome back",
        subtitle = "Sign in to continue."
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        AppTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            visualTransformation = PasswordVisualTransformation()
        )

        if (!error.isNullOrBlank()) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        AppPrimaryButton(
            text = if (loading) "Signing in..." else "Login",
            onClick = {
                loading = true
                error = null
                scope.launch {
                    when (val result = repository.login(email.trim(), password)) {
                        is RepoResult.Success -> onLoginSuccess(result.data)
                        is RepoResult.Error -> error = result.message
                    }
                    loading = false
                }
            },
            enabled = !loading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        AppSecondaryButton(
            text = "Create account",
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
