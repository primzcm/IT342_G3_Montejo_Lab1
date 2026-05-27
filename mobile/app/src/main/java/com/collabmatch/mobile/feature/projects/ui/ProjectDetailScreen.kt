package com.collabmatch.mobile.feature.projects.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.collabmatch.mobile.core.data.CollabMatchRepository
import com.collabmatch.mobile.core.data.RepoResult
import com.collabmatch.mobile.feature.projects.data.JoinRequestDto
import com.collabmatch.mobile.feature.projects.data.ProjectDetailDto
import com.collabmatch.mobile.feature.projects.data.ProjectMessageDto
import com.collabmatch.mobile.ui.theme.AppBadge
import com.collabmatch.mobile.ui.theme.AppCard
import com.collabmatch.mobile.ui.theme.AppDangerButton
import com.collabmatch.mobile.ui.theme.AppGradientFrame
import com.collabmatch.mobile.ui.theme.AppMiniButton
import com.collabmatch.mobile.ui.theme.AppPrimaryButton
import com.collabmatch.mobile.ui.theme.AppSecondaryButton
import com.collabmatch.mobile.ui.theme.AppSectionTabs
import com.collabmatch.mobile.ui.theme.AppTextField
import com.collabmatch.mobile.ui.theme.AppTopBar
import com.collabmatch.mobile.ui.theme.Gold
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val displayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a").withZone(ZoneId.systemDefault())

@Composable
fun ProjectDetailScreen(
    repository: CollabMatchRepository,
    projectId: Long,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onDeleted: () -> Unit
) {
    if (repository.getAccessToken().isNullOrBlank()) {
        LaunchedEffect(Unit) {
            onLoggedOut()
        }
        return
    }

    val scope = rememberCoroutineScope()
    var project by remember { mutableStateOf<ProjectDetailDto?>(null) }
    var requests by remember { mutableStateOf<List<JoinRequestDto>>(emptyList()) }
    var messages by remember { mutableStateOf<List<ProjectMessageDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var requestError by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf<String?>(null) }
    var joining by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var postingMessage by remember { mutableStateOf(false) }
    var actingRequestId by remember { mutableStateOf<Long?>(null) }
    var editing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var rolesNeeded by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("OPEN") }
    var messageDraft by remember { mutableStateOf("") }

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
        requestError = null
        messageError = null

        when (val result = repository.fetchProjectDetail(projectId)) {
            is RepoResult.Success -> {
                project = result.data
                populateForm(result.data)
                if (result.data.owner) {
                    when (val requestResult = repository.fetchProjectRequests(projectId)) {
                        is RepoResult.Success -> requests = requestResult.data
                        is RepoResult.Error -> {
                            if (repository.isAuthenticationError(requestResult.message)) {
                                onLoggedOut()
                                return
                            }
                            requestError = requestResult.message
                        }
                    }
                } else {
                    requests = emptyList()
                }

                if (result.data.owner || result.data.joined) {
                    when (val messageResult = repository.fetchProjectMessages(projectId)) {
                        is RepoResult.Success -> messages = messageResult.data
                        is RepoResult.Error -> {
                            if (repository.isAuthenticationError(messageResult.message)) {
                                onLoggedOut()
                                return
                            }
                            messageError = messageResult.message
                        }
                    }
                } else {
                    messages = emptyList()
                }
            }

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

    LaunchedEffect(projectId) {
        loadProject()
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

            project == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    AppCard {
                        Text(
                            text = error ?: "Unable to load project",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        AppSecondaryButton(
                            text = "Back",
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            else -> {
                val currentProject = project!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        AppTopBar(
                            title = "Project",
                            badge = if (currentProject.owner) "Owner View" else "Project Detail",
                            meta = "${currentProject.status} • ${currentProject.category}",
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
                    }

                    item {
                        AppCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                AppBadge(currentProject.status)
                                AppBadge(currentProject.category)
                            }

                            Text(
                                text = currentProject.title,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = currentProject.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            DetailMetaRow(
                                "Owner",
                                currentProject.ownerName,
                                "Skills Needed",
                                currentProject.requiredSkills.takeIf { it.isNotEmpty() }?.joinToString(", ")
                                    ?: currentProject.rolesNeeded
                            )
                            DetailMetaRow("Created", formatTimestamp(currentProject.createdAt), "Members", currentProject.members.size.toString())
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

                    if (currentProject.owner) {
                        item {
                            OwnerControlsCard(
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
                                onDelete = { showDeleteDialog = true }
                            )
                        }
                    } else if (!currentProject.joined) {
                        item {
                            AppCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                                Text(
                                    text = "Join this project",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Send a join request.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AppPrimaryButton(
                                    text = when {
                                        currentProject.joinRequested -> "Requested"
                                        joining -> "Submitting..."
                                        currentProject.status != "OPEN" -> "Project Closed"
                                        else -> "Request to Join"
                                    },
                                    onClick = {
                                        scope.launch {
                                            joining = true
                                            error = null
                                            when (val result = repository.requestToJoinProject(projectId)) {
                                                is RepoResult.Success -> loadProject()
                                                is RepoResult.Error -> {
                                                    if (repository.isAuthenticationError(result.message)) {
                                                        onLoggedOut()
                                                        return@launch
                                                    }
                                                    error = result.message
                                                }
                                            }
                                            joining = false
                                        }
                                    },
                                    enabled = !currentProject.joinRequested && currentProject.status == "OPEN" && !joining,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        item {
                            AppCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                                Text(
                                    text = "You are on this team",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Your request was approved. Track the roster and project details here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (currentProject.owner || currentProject.joined) {
                        item {
                            DetailSectionCard(
                                title = "Project Board",
                                subtitle = "Share introductions, updates, and quick coordination notes with the team.",
                                modifier = Modifier.padding(horizontal = 20.dp)
                            ) {
                                AppTextField(
                                    value = messageDraft,
                                    onValueChange = { if (it.length <= 2000) messageDraft = it },
                                    label = "New message",
                                    minLines = 4
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${messageDraft.trim().length}/2000",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AppPrimaryButton(
                                        text = if (postingMessage) "Posting..." else "Post Message",
                                        onClick = {
                                            scope.launch {
                                                if (messageDraft.trim().isEmpty()) {
                                                    return@launch
                                                }
                                                postingMessage = true
                                                messageError = null
                                                when (val result = repository.createProjectMessage(projectId, messageDraft)) {
                                                    is RepoResult.Success -> {
                                                        messageDraft = ""
                                                        when (val messagesResult = repository.fetchProjectMessages(projectId)) {
                                                            is RepoResult.Success -> messages = messagesResult.data
                                                            is RepoResult.Error -> {
                                                                if (repository.isAuthenticationError(messagesResult.message)) {
                                                                    onLoggedOut()
                                                                    return@launch
                                                                }
                                                                messageError = messagesResult.message
                                                            }
                                                        }
                                                    }

                                                    is RepoResult.Error -> {
                                                        if (repository.isAuthenticationError(result.message)) {
                                                            onLoggedOut()
                                                            return@launch
                                                        }
                                                        messageError = result.message
                                                    }
                                                }
                                                postingMessage = false
                                            }
                                        },
                                        enabled = !postingMessage && messageDraft.trim().isNotEmpty(),
                                        modifier = Modifier.weight(0.45f, fill = false)
                                    )
                                }

                                if (!messageError.isNullOrBlank()) {
                                    Text(
                                        text = messageError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (messages.isEmpty()) {
                                    Text(
                                        text = "No messages yet. Start the conversation with a short introduction or update.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        messages.forEach { message ->
                                            AppCard {
                                                Text(
                                                    text = message.authorName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(
                                                    text = formatTimestamp(message.createdAt),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = message.content,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        DetailSectionCard(
                            title = "Project Members",
                            subtitle = "Approved collaborators attached to this project.",
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            if (currentProject.members.isEmpty()) {
                                Text(
                                    text = "No members yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    currentProject.members.forEach { member ->
                                        AppCard {
                                            Text(
                                                text = member.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = "Joined ${formatTimestamp(member.joinedAt)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
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
                                subtitle = "Approve or reject incoming requests.",
                                modifier = Modifier.padding(horizontal = 20.dp)
                            ) {
                                if (!requestError.isNullOrBlank()) {
                                    Text(
                                        text = requestError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (requests.isEmpty()) {
                                    Text(
                                        text = "No join requests yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                                                            is RepoResult.Error -> {
                                                                if (repository.isAuthenticationError(result.message)) {
                                                                    onLoggedOut()
                                                                    return@launch
                                                                }
                                                                requestError = result.message
                                                            }
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
                                                            is RepoResult.Error -> {
                                                                if (repository.isAuthenticationError(result.message)) {
                                                                    onLoggedOut()
                                                                    return@launch
                                                                }
                                                                requestError = result.message
                                                            }
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
                TextButton(
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
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            title = { Text("Delete project?") },
            text = { Text("This removes the project, join requests, and members.") },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun OwnerControlsCard(
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
        subtitle = "Edit the brief, update the status, or remove the project.",
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        if (editing) {
            AppTextField(value = title, onValueChange = onTitleChange, label = "Project title")
            AppTextField(value = category, onValueChange = onCategoryChange, label = "Category")
            AppTextField(value = rolesNeeded, onValueChange = onRolesNeededChange, label = "Skills / roles needed")
            AppSectionTabs(
                options = listOf("OPEN", "CLOSED"),
                selected = status,
                onSelect = onStatusChange
            )
            AppTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = "Description",
                minLines = 4
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppSecondaryButton(
                text = if (editing) "Cancel Edit" else "Edit Project",
                onClick = onEditToggle,
                modifier = Modifier.weight(1f)
            )
            AppDangerButton(
                text = if (deleting) "Deleting..." else "Delete",
                onClick = onDelete,
                enabled = !deleting,
                modifier = Modifier.weight(1f)
            )
        }

        if (editing) {
            AppPrimaryButton(
                text = if (saving) "Saving..." else "Save Changes",
                onClick = onSave,
                enabled = !saving,
                modifier = Modifier.fillMaxWidth()
            )
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
    AppCard {
        Text(
            text = request.requesterName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = request.message ?: "No message provided.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Status: ${request.status}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (request.reviewedAt != null) {
            Text(
                text = "Reviewed: ${formatTimestamp(request.reviewedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (request.status == "PENDING") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppPrimaryButton(
                    text = if (acting) "Saving..." else "Approve",
                    onClick = onApprove,
                    enabled = !acting,
                    modifier = Modifier.weight(1f)
                )
                AppSecondaryButton(
                    text = "Reject",
                    onClick = onReject,
                    enabled = !acting,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AppCard(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        content()
    }
}

@Composable
private fun DetailMetaRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppCard(modifier = Modifier.weight(1f)) {
            Text(leftLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(leftValue, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        AppCard(modifier = Modifier.weight(1f)) {
            Text(rightLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(rightValue, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
        }
    }
}

private fun formatTimestamp(value: String): String {
    return runCatching {
        displayFormatter.format(Instant.parse(value))
    }.getOrDefault(value)
}
