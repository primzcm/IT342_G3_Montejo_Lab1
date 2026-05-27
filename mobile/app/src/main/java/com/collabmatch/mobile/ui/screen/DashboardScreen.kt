package com.collabmatch.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.collabmatch.mobile.ui.theme.AppBadge
import com.collabmatch.mobile.ui.theme.AppCard
import com.collabmatch.mobile.ui.theme.AppGradientFrame
import com.collabmatch.mobile.ui.theme.AppMetricCard
import com.collabmatch.mobile.ui.theme.AppMiniButton
import com.collabmatch.mobile.ui.theme.AppPrimaryButton
import com.collabmatch.mobile.ui.theme.AppSecondaryButton
import com.collabmatch.mobile.ui.theme.AppSectionTabs
import com.collabmatch.mobile.ui.theme.AppTextField
import com.collabmatch.mobile.ui.theme.AppTopBar
import com.collabmatch.mobile.ui.theme.Gold
import com.collabmatch.mobile.ui.theme.MistMuted
import kotlinx.coroutines.launch

private const val SectionAll = "All Projects"
private const val SectionMine = "My Projects"
private const val SectionJoined = "Joined Projects"
private const val SectionApplications = "My Applications"

@Composable
fun DashboardScreen(
    repository: AuthRepository,
    onLoggedOut: () -> Unit,
    onOpenProfile: () -> Unit,
    onCreateProject: () -> Unit,
    onOpenProject: (Long) -> Unit
) {
    if (repository.getAccessToken().isNullOrBlank()) {
        LaunchedEffect(Unit) {
            onLoggedOut()
        }
        return
    }

    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<UserDto?>(null) }
    var projects by remember { mutableStateOf<List<ProjectSummaryDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var joiningProjectId by remember { mutableStateOf<Long?>(null) }
    var activeSection by remember { mutableStateOf(SectionAll) }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    suspend fun loadDashboard() {
        loading = true
        error = null

        when (val currentUserResult = repository.fetchCurrentUser()) {
            is RepoResult.Success -> user = currentUserResult.data
            is RepoResult.Error -> {
                if (repository.isAuthenticationError(currentUserResult.message)) {
                    onLoggedOut()
                    return
                }
                error = currentUserResult.message
            }
        }

        when (val projectsResult = repository.fetchProjects()) {
            is RepoResult.Success -> projects = projectsResult.data
            is RepoResult.Error -> {
                if (repository.isAuthenticationError(projectsResult.message)) {
                    onLoggedOut()
                    return
                }
                error = projectsResult.message
            }
        }

        loading = false
    }

    LaunchedEffect(Unit) {
        loadDashboard()
    }

    val categories = listOf("All") + projects.map { it.category }.distinct()
    val filteredProjects = projects.filter { project ->
        val categoryMatches = selectedCategory == "All" || project.category == selectedCategory
        val query = searchQuery.trim().lowercase()
        val searchable = listOf(
            project.title,
            project.category,
            project.description,
            project.rolesNeeded,
            project.ownerName,
            project.requiredSkills.joinToString(" ")
        ).joinToString(" ").lowercase()
        val searchMatches = query.isBlank() || searchable.contains(query)
        categoryMatches && searchMatches
    }
    val visibleProjects = when (activeSection) {
        SectionMine -> filteredProjects.filter { it.owner }
        SectionJoined -> filteredProjects.filter { it.joined }
        SectionApplications -> filteredProjects.filter { it.joinRequested }
        else -> filteredProjects
    }
    val openCount = visibleProjects.count { it.status == "OPEN" }
    val requestCount = projects.count { it.joinRequested }
    val joinedCount = projects.count { it.joined }
    val sectionDescription = when (activeSection) {
        SectionMine -> "Projects you own."
        SectionJoined -> "Projects you already joined."
        SectionApplications -> "Requests you already sent."
        else -> "Browse current collaboration posts."
    }

    AppGradientFrame {
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Gold)
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        AppTopBar(
                            title = "Project Feed",
                            badge = activeSection,
                            meta = "${visibleProjects.size} visible",
                            actions = {
                                AppMiniButton(
                                    text = "Profile",
                                    onClick = onOpenProfile
                                )
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
                    }

                    item {
                        AppCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                text = "Hi, ${user?.firstname ?: "Builder"}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = sectionDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AppMetricCard(
                                    label = "Visible",
                                    value = visibleProjects.size.toString(),
                                    modifier = Modifier.width(108.dp)
                                )
                                AppMetricCard(
                                    label = "Open",
                                    value = openCount.toString(),
                                    modifier = Modifier.width(108.dp)
                                )
                                AppMetricCard(
                                    label = "Requested",
                                    value = requestCount.toString(),
                                    modifier = Modifier.width(108.dp)
                                )
                                AppMetricCard(
                                    label = "Joined",
                                    value = joinedCount.toString(),
                                    modifier = Modifier.width(108.dp)
                                )
                            }

                            AppPrimaryButton(
                                text = "Post Project",
                                onClick = onCreateProject,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        AppCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Text(
                                text = "Browse",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            AppSectionTabs(
                                options = listOf(SectionAll, SectionMine, SectionJoined, SectionApplications),
                                selected = activeSection,
                                onSelect = { activeSection = it }
                            )
                            AppSectionTabs(
                                options = categories,
                                selected = selectedCategory,
                                onSelect = { selectedCategory = it }
                            )
                            AppTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = "Search skills or keywords"
                            )
                        }
                    }

                    if (!error.isNullOrBlank()) {
                        item {
                            AppCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                                Text(
                                    text = error ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (visibleProjects.isEmpty()) {
                        item {
                            AppCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                                Text(
                                    text = when {
                                        searchQuery.isNotBlank() || selectedCategory != "All" -> "No projects match these filters."
                                        activeSection == SectionMine -> "You have not posted a project yet."
                                        activeSection == SectionJoined -> "You have not joined a project yet."
                                        activeSection == SectionApplications -> "You have not applied to a project yet."
                                        else -> "No projects yet."
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                AppPrimaryButton(
                                    text = if (activeSection == SectionApplications || activeSection == SectionJoined) "Browse Projects" else "Post Project",
                                    onClick = {
                                        if (activeSection == SectionApplications || activeSection == SectionJoined) {
                                            activeSection = SectionAll
                                        } else {
                                            onCreateProject()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        items(visibleProjects, key = { it.id }) { project ->
                            MobileProjectCard(
                                project = project,
                                joining = joiningProjectId == project.id,
                                onOpenProject = { onOpenProject(project.id) },
                                onJoinProject = {
                                    scope.launch {
                                        joiningProjectId = project.id
                                        error = null
                                        when (val result = repository.requestToJoinProject(project.id)) {
                                            is RepoResult.Success -> {
                                                projects = projects.map {
                                                    if (it.id == project.id) it.copy(joinRequested = true) else it
                                                }
                                            }

                                            is RepoResult.Error -> error = result.message
                                        }
                                        joiningProjectId = null
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileProjectCard(
    project: ProjectSummaryDto,
    joining: Boolean,
    onOpenProject: () -> Unit,
    onJoinProject: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppBadge(project.category)
            AppBadge(
                if (project.owner) "Owner" else if (project.joined) "Member" else if (project.joinRequested) "Requested" else project.status
            )
        }

        Text(
            text = project.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = project.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = project.ownerName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (project.requiredSkills.isNotEmpty()) {
                project.requiredSkills.joinToString(" • ")
            } else {
                project.rolesNeeded
            },
            style = MaterialTheme.typography.bodySmall,
            color = MistMuted
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppSecondaryButton(
                text = "View Details",
                onClick = onOpenProject,
                modifier = Modifier.fillMaxWidth()
            )
            if (project.owner) {
                AppPrimaryButton(
                    text = "Manage Project",
                    onClick = onOpenProject,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (project.joined) {
                AppPrimaryButton(
                    text = "Open Project",
                    onClick = onOpenProject,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                AppPrimaryButton(
                    text = when {
                        project.joinRequested -> "Requested"
                        joining -> "Joining..."
                        else -> "Request to Join"
                    },
                    onClick = onJoinProject,
                    enabled = !project.joinRequested && !joining,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
