package com.collabmatch.mobile.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.collabmatch.mobile.data.model.JoinRequestDto
import com.collabmatch.mobile.data.model.ProjectDetailDto
import com.collabmatch.mobile.data.repository.AuthRepository
import com.collabmatch.mobile.data.repository.RepoResult
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val displayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a").withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    repository: AuthRepository,
    projectId: Long,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onDeleted: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var project by remember { mutableStateOf<ProjectDetailDto?>(null) }
    var requests by remember { mutableStateOf<List<JoinRequestDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var requestError by remember { mutableStateOf<String?>(null) }
    var joining by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var actingRequestId by remember { mutableStateOf<Long?>(null) }
    var editing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var rolesNeeded by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("OPEN") }

    fun populateForm(detail: ProjectDetailDto) {
        title = detail.title
        description = detail.description
        category = detail.category
        rolesNeeded = detail.rolesNeeded
        status = detail.status
    }

    suspend fun loadProject() {
        loading = true
        error = null

        when (val result = repository.fetchProjectDetail(projectId)) {
            is RepoResult.Success -> {
                project = result.data
                populateForm(result.data)
                if (result.data.owner) {
                    when (val requestResult = repository.fetchProjectRequests(projectId)) {
                        is RepoResult.Success -> requests = requestResult.data
                        is RepoResult.Error -> requestError = requestResult.message
                    }
                } else {
                    requests = emptyList()
                }
            }

            is RepoResult.Error -> error = result.message
        }

        loading = false
    }

    LaunchedEffect(projectId) {
        loadProject()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project?.title ?: "Project Detail") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
                actions = {
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

            project == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error ?: "Unable to load project")
                }
            }

            else -> {
                val currentProject = project!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ProjectDetailSummaryCard(currentProject)
                    }

                    if (!error.isNullOrBlank()) {
                        item {
                            Text(
                                text = error ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (currentProject.owner) {
                        item {
                            OwnerActionsCard(
                                editing = editing,
                                saving = saving,
                                deleting = deleting,
                                title = title,
                                onTitleChange = { title = it },
                                description = description,
                                onDescriptionChange = { description = it },
                                category = category,
                                onCategoryChange = { category = it },
                                rolesNeeded = rolesNeeded,
                                onRolesNeededChange = { rolesNeeded = it },
                                status = status,
                                onStatusChange = { status = it },
                                onEditToggle = { editing = !editing },
                                onSave = {
                                    scope.launch {
                                        saving = true
                                        error = null
                                        when (
                                            val result = repository.updateProject(
                                                projectId = projectId,
                                                title = title.trim(),
                                                description = description.trim(),
                                                category = category.trim(),
                                                rolesNeeded = rolesNeeded.trim(),
                                                status = status
                                            )
                                        ) {
                                            is RepoResult.Success -> {
                                                loadProject()
                                                editing = false
                                            }

                                            is RepoResult.Error -> error = result.message
                                        }
                                        saving = false
                                    }
                                },
                                onDelete = { showDeleteDialog = true }
                            )
                        }
                    } else {
                        item {
                            Button(
                                onClick = {
                                    scope.launch {
                                        joining = true
                                        error = null
                                        when (val result = repository.requestToJoinProject(projectId)) {
                                            is RepoResult.Success -> loadProject()
                                            is RepoResult.Error -> error = result.message
                                        }
                                        joining = false
                                    }
                                },
                                enabled = !currentProject.joinRequested && currentProject.status == "OPEN" && !joining,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    when {
                                        currentProject.joinRequested -> "Join Requested"
                                        joining -> "Submitting..."
                                        currentProject.status != "OPEN" -> "Project Closed"
                                        else -> "Request to Join"
                                    }
                                )
                            }
                        }
                    }

                    item {
                        DetailSectionCard(
                            title = "Project Members",
                            subtitle = "Approved collaborators on this project."
                        ) {
                            if (currentProject.members.isEmpty()) {
                                Text("No members yet.")
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    currentProject.members.forEach { member ->
                                        Card {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(member.name, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    text = "Joined ${formatTimestamp(member.joinedAt)}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (currentProject.owner) {
                        item {
                            DetailSectionCard(
                                title = "Join Requests",
                                subtitle = "Approve or reject incoming requests."
                            ) {
                                if (!requestError.isNullOrBlank()) {
                                    Text(
                                        text = requestError ?: "",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }

                                if (requests.isEmpty()) {
                                    Text("No join requests yet.")
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        requests.forEach { request ->
                                            JoinRequestCard(
                                                request = request,
                                                acting = actingRequestId == request.id,
                                                onApprove = {
                                                    scope.launch {
                                                        actingRequestId = request.id
                                                        requestError = null
                                                        when (val result = repository.approveJoinRequest(request.id)) {
                                                            is RepoResult.Success -> loadProject()
                                                            is RepoResult.Error -> requestError = result.message
                                                        }
                                                        actingRequestId = null
                                                    }
                                                },
                                                onReject = {
                                                    scope.launch {
                                                        actingRequestId = request.id
                                                        requestError = null
                                                        when (val result = repository.rejectJoinRequest(request.id)) {
                                                            is RepoResult.Success -> loadProject()
                                                            is RepoResult.Error -> requestError = result.message
                                                        }
                                                        actingRequestId = null
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            showDeleteDialog = false
                            deleting = true
                            error = null
                            when (val result = repository.deleteProject(projectId)) {
                                is RepoResult.Success -> onDeleted()
                                is RepoResult.Error -> {
                                    error = result.message
                                    deleting = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete project?") },
            text = { Text("This removes the project, join requests, and members.") }
        )
    }
}

@Composable
private fun ProjectDetailSummaryCard(project: ProjectDetailDto) {
    Card {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(project.category, color = MaterialTheme.colorScheme.primary)
                Text(project.status, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = project.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(project.description, style = MaterialTheme.typography.bodyMedium)
            Text("Owner: ${project.ownerName}", style = MaterialTheme.typography.bodySmall)
            Text("Roles: ${project.rolesNeeded}", style = MaterialTheme.typography.bodySmall)
            Text("Created: ${formatTimestamp(project.createdAt)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun OwnerActionsCard(
    editing: Boolean,
    saving: Boolean,
    deleting: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    rolesNeeded: String,
    onRolesNeededChange: (String) -> Unit,
    status: String,
    onStatusChange: (String) -> Unit,
    onEditToggle: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    DetailSectionCard(
        title = "Owner Controls",
        subtitle = "Edit project details or remove the project."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (editing) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = onCategoryChange,
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rolesNeeded,
                    onValueChange = onRolesNeededChange,
                    label = { Text("Roles needed") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = status,
                    onValueChange = { onStatusChange(it.uppercase()) },
                    label = { Text("Status (OPEN or CLOSED)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEditToggle,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (editing) "Cancel Edit" else "Edit Project")
                }
                Button(
                    onClick = onDelete,
                    enabled = !deleting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (deleting) "Deleting..." else "Delete")
                }
            }

            if (editing) {
                Button(
                    onClick = onSave,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (saving) "Saving..." else "Save Changes")
                }
            }
        }
    }
}

@Composable
private fun JoinRequestCard(
    request: JoinRequestDto,
    acting: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(request.requesterName, fontWeight = FontWeight.SemiBold)
            Text(request.message ?: "No message provided.", style = MaterialTheme.typography.bodyMedium)
            Text("Status: ${request.status}", style = MaterialTheme.typography.bodySmall)
            if (request.reviewedAt != null) {
                Text("Reviewed: ${formatTimestamp(request.reviewedAt)}", style = MaterialTheme.typography.bodySmall)
            }
            if (request.status == "PENDING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        enabled = !acting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (acting) "Saving..." else "Approve")
                    }
                    Button(
                        onClick = onReject,
                        enabled = !acting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            content()
        }
    }
}

private fun formatTimestamp(value: String): String {
    return runCatching {
        displayFormatter.format(Instant.parse(value))
    }.getOrDefault(value)
}
