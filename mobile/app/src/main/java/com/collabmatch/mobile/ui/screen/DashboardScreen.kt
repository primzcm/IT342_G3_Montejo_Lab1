package com.collabmatch.mobile.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.collabmatch.mobile.data.model.ProjectSummaryDto
import com.collabmatch.mobile.data.model.UserDto
import com.collabmatch.mobile.data.repository.AuthRepository
import com.collabmatch.mobile.data.repository.RepoResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: AuthRepository,
    onLoggedOut: () -> Unit,
    onCreateProject: () -> Unit,
    onOpenProject: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserDto?>(null) }
    var projects by remember { mutableStateOf<List<ProjectSummaryDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun loadDashboard() {
        loading = true
        error = null

        val currentUserResult = repository.fetchCurrentUser()
        val projectsResult = repository.fetchProjects()

        when (currentUserResult) {
            is RepoResult.Success -> user = currentUserResult.data
            is RepoResult.Error -> error = currentUserResult.message
        }

        when (projectsResult) {
            is RepoResult.Success -> projects = projectsResult.data
            is RepoResult.Error -> error = projectsResult.message
        }

        loading = false
    }

    LaunchedEffect(Unit) {
        loadDashboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CollabMatch") },
                actions = {
                    TextButton(onClick = onCreateProject) {
                        Text("Create")
                    }
                    TextButton(
                        onClick = {
                            scope.launch {
                                repository.logout()
                                onLoggedOut()
                            }
                        }
                    ) {
                        Text("Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateProject) {
                Text("+")
            }
        }
    ) { innerPadding ->
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Welcome back",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = user?.let { "${it.firstname} ${it.lastname}" } ?: "CollabMatch member",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Browse collaboration posts, open project details, and manage team requests.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    if (!error.isNullOrBlank()) {
                        item {
                            Text(
                                text = error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (projects.isEmpty()) {
                        item {
                            Card {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "No projects yet",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Create the first collaboration post from mobile.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Button(onClick = onCreateProject) {
                                        Text("Create Project")
                                    }
                                }
                            }
                        }
                    } else {
                        items(projects, key = { it.id }) { project ->
                            ProjectSummaryCard(
                                project = project,
                                onClick = { onOpenProject(project.id) }
                            )
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                scope.launch { loadDashboard() }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Refresh Feed")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectSummaryCard(
    project: ProjectSummaryDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (project.owner) "Owner" else project.status,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                text = project.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Owner: ${project.ownerName}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Roles: ${project.rolesNeeded}",
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (project.owner) "Manage Project" else "View Details")
            }
        }
    }
}
