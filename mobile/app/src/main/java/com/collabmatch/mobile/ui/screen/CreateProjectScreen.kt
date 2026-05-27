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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
fun CreateProjectScreen(
    repository: AuthRepository,
    onBack: () -> Unit,
    onProjectCreated: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var rolesNeeded by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AppGradientFrame {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            AppTopBar(
                title = "New Project",
                badge = "Create",
                meta = "Publish a collaboration post",
                onBack = onBack,
                actions = {
                    AppMiniButton(text = "Cancel", onClick = onBack)
                }
            )

            AppCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Project details",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Project title"
                )
                AppTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = "Category"
                )
                AppTextField(
                    value = rolesNeeded,
                    onValueChange = { rolesNeeded = it },
                    label = "Skills or roles needed"
                )
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description",
                    minLines = 5
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
                    text = if (loading) "Publishing..." else "Publish Project",
                    onClick = {
                        loading = true
                        error = null
                        scope.launch {
                            when (
                                val result = repository.createProject(
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = category.trim(),
                                    rolesNeeded = rolesNeeded.trim()
                                )
                            ) {
                                is RepoResult.Success -> onProjectCreated(result.data.id)
                                is RepoResult.Error -> error = result.message
                            }
                            loading = false
                        }
                    },
                    enabled = !loading &&
                        title.isNotBlank() &&
                        description.isNotBlank() &&
                        category.isNotBlank() &&
                        rolesNeeded.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
