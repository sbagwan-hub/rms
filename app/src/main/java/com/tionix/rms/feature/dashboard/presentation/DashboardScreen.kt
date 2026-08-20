package com.tionix.rms.feature.dashboard.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.feature.dashboard.domain.model.ReportsSummary
import com.tionix.rms.feature.dashboard.domain.model.Task
import com.tionix.rms.feature.dashboard.domain.model.TaskPriority
import com.tionix.rms.feature.dashboard.domain.model.TaskStatus
import com.tionix.rms.feature.dashboard.domain.model.TaskType

private val SuccessGreen = Color(0xFF16A34A)
private val WarningAmber = Color(0xFFF59E0B)
private val ErrorRose = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onTaskClick: (Task) -> Unit,
    onSearch: () -> Unit = {},
    onHistory: () -> Unit = {},
    onProfile: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loggedOut.collect { onLogout() }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "RMS Operations",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Warehouse Dashboard & Workflows",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DashboardUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. METRICS OVERVIEW
                    item {
                        DashboardSectionHeader(title = "Key Metrics", icon = Icons.Default.BarChart)
                    }
                    item {
                        StatsGrid(stats = state.stats)
                    }

                    if (pendingSyncCount > 0) {
                        item {
                            PendingSyncBanner(
                                count = pendingSyncCount,
                                onClick = onHistory
                            )
                        }
                    }

                    if (state.canViewReports && state.reportsSummary != null) {
                        item {
                            ReportsSummaryCard(summary = state.reportsSummary)
                        }
                    }

                    // 2. WORKFLOWS GRID
                    item {
                        DashboardSectionHeader(title = "Barcode Scan Workflows", icon = Icons.Default.QrCodeScanner)
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                WorkflowCard(
                                    title = "Fresh Box Move",
                                    icon = Icons.Default.MoveToInbox,
                                    description = "Scan fresh box to location",
                                    onClick = {
                                        onTaskClick(
                                            Task(
                                                id = "",
                                                type = TaskType.FRESH_BOX_MOVE,
                                                title = "",
                                                description = "",
                                                status = TaskStatus.PENDING,
                                                priority = TaskPriority.MEDIUM,
                                                assignedTo = "",
                                                createdAt = "",
                                                dueDate = null
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                WorkflowCard(
                                    title = "Refile",
                                    icon = Icons.Default.Upload,
                                    description = "Scan files back to box",
                                    onClick = {
                                        onTaskClick(
                                            Task(
                                                id = "",
                                                type = TaskType.REFILE,
                                                title = "",
                                                description = "",
                                                status = TaskStatus.PENDING,
                                                priority = TaskPriority.MEDIUM,
                                                assignedTo = "",
                                                createdAt = "",
                                                dueDate = null
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                WorkflowCard(
                                    title = "Segregation",
                                    icon = Icons.Default.CallSplit,
                                    description = "Move files out of box",
                                    onClick = {
                                        onTaskClick(
                                            Task(
                                                id = "",
                                                type = TaskType.SEGREGATION,
                                                title = "",
                                                description = "",
                                                status = TaskStatus.PENDING,
                                                priority = TaskPriority.MEDIUM,
                                                assignedTo = "",
                                                createdAt = "",
                                                dueDate = null
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                WorkflowCard(
                                    title = "Merge",
                                    icon = Icons.Default.CallMerge,
                                    description = "Combine boxes together",
                                    onClick = {
                                        onTaskClick(
                                            Task(
                                                id = "",
                                                type = TaskType.MERGE,
                                                title = "",
                                                description = "",
                                                status = TaskStatus.PENDING,
                                                priority = TaskPriority.MEDIUM,
                                                assignedTo = "",
                                                createdAt = "",
                                                dueDate = null
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                WorkflowCard(
                                    title = "Transfer",
                                    icon = Icons.Default.LocalShipping,
                                    description = "Transfer items/boxes",
                                    onClick = {
                                        onTaskClick(
                                            Task(
                                                id = "",
                                                type = TaskType.TRANSFER,
                                                title = "",
                                                description = "",
                                                status = TaskStatus.PENDING,
                                                priority = TaskPriority.MEDIUM,
                                                assignedTo = "",
                                                createdAt = "",
                                                dueDate = null
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                WorkflowCard(
                                    title = "Verification",
                                    icon = Icons.Default.FactCheck,
                                    description = "Inventory verification scan",
                                    onClick = {
                                        onTaskClick(
                                            Task(
                                                id = "",
                                                type = TaskType.INVENTORY_VERIFICATION,
                                                title = "",
                                                description = "",
                                                status = TaskStatus.PENDING,
                                                priority = TaskPriority.MEDIUM,
                                                assignedTo = "",
                                                createdAt = "",
                                                dueDate = null
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 3. ASSIGNED TASKS
                    item {
                        DashboardSectionHeader(title = "Assigned Tasks", icon = Icons.Default.Assignment)
                    }

                    if (state.tasks.isEmpty()) {
                        item {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No tasks assigned",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.tasks) { task ->
                            TaskCard(
                                task = task,
                                onClick = { onTaskClick(task) }
                            )
                        }
                    }

                    // 4. QUICK FOOTER ACTIONS
                    item {
                        DashboardFooter(
                            pendingSyncCount = pendingSyncCount,
                            onSearch = onSearch,
                            onHistory = onHistory,
                            onProfile = onProfile
                        )
                    }

                    item {
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PendingSyncBanner(
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = WarningAmber.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
            Text(
                text = "$count operation${if (count == 1) "" else "s"} pending sync",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = WarningAmber
            )
        }
    }
}

@Composable
private fun ReportsSummaryCard(summary: ReportsSummary) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Today's Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Today's operations", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(summary.todayOperationsCount.toString(), fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Missing files", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = summary.missingFilesCount.toString(),
                    fontWeight = FontWeight.Bold,
                    color = if (summary.missingFilesCount > 0) ErrorRose else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DashboardFooter(
    pendingSyncCount: Int,
    onSearch: () -> Unit,
    onHistory: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(onClick = onSearch, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Search")
        }
        BadgedBox(
            badge = {
                if (pendingSyncCount > 0) {
                    Badge { Text(pendingSyncCount.toString()) }
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("History")
            }
        }
        OutlinedButton(onClick = onProfile, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Profile")
        }
    }
}

@Composable
private fun StatsGrid(stats: com.tionix.rms.feature.dashboard.domain.model.DashboardStats) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Total Tasks",
                value = stats.totalTasks.toString(),
                icon = Icons.Default.Assignment,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pending",
                value = stats.pendingTasks.toString(),
                icon = Icons.Default.Schedule,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "In Progress",
                value = stats.inProgressTasks.toString(),
                icon = Icons.Default.Sync,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Completed",
                value = stats.completedTasks.toString(),
                icon = Icons.Default.CheckCircle,
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Urgent",
                value = stats.urgentTasks.toString(),
                icon = Icons.Default.PriorityHigh,
                color = ErrorRose,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Boxes Today",
                value = stats.boxesProcessedToday.toString(),
                icon = Icons.Default.Inventory2,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: com.tionix.rms.feature.dashboard.domain.model.Task,
    onClick: () -> Unit
) {
    val indicatorColor = when (task.priority) {
        TaskPriority.LOW -> MaterialTheme.colorScheme.secondary
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
        TaskPriority.HIGH -> WarningAmber
        TaskPriority.URGENT -> ErrorRose
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        onClick = onClick,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(96.dp)
                    .background(
                        color = indicatorColor,
                        shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusBadge(task.status)
                }

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TypeBadge(task.type)
                        PriorityBadge(task.priority)
                    }
                    Text(
                        text = "Assigned to: ${task.assignedTo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TaskStatus) {
    val (color, label) = when (status) {
        TaskStatus.PENDING -> MaterialTheme.colorScheme.tertiary to "Pending"
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary to "In Progress"
        TaskStatus.COMPLETED -> SuccessGreen to "Completed"
        TaskStatus.FAILED -> MaterialTheme.colorScheme.error to "Failed"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TypeBadge(type: TaskType) {
    val label = when (type) {
        TaskType.FRESH_BOX_MOVE -> "Box Move"
        TaskType.INVENTORY_VERIFICATION -> "Verification"
        TaskType.REFILE -> "Refile"
        TaskType.SEGREGATION -> "Segregation"
        TaskType.MERGE -> "Merge"
        TaskType.TRANSFER -> "Transfer"
    }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PriorityBadge(priority: TaskPriority) {
    val (color, label) = when (priority) {
        TaskPriority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) to "Low"
        TaskPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary to "Medium"
        TaskPriority.HIGH -> WarningAmber to "High"
        TaskPriority.URGENT -> ErrorRose to "Urgent"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun WorkflowCard(
    title: String,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified
                )
            }
        }
    }
}
