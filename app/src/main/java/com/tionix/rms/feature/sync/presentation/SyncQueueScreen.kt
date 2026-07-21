package com.tionix.rms.feature.sync.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.feature.sync.domain.model.SyncStatus
import com.tionix.rms.ui.common.EmptyState
import com.tionix.rms.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncQueueScreen(
    onBack: () -> Unit,
    userRole: String = "OPERATOR", // MANAGER can delete failed items
    viewModel: SyncQueueViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncQueue by viewModel.syncQueue.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadSyncQueue()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Queue") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadSyncQueue() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live counts
            syncQueue?.let { queue ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CountCard(
                        label = "Pending",
                        count = queue.pendingCount,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                    CountCard(
                        label = "Failed",
                        count = queue.failedCount,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Action buttons
                if (queue.pendingCount > 0) {
                    Button(
                        onClick = { viewModel.retryAllFailedItems() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry All Failed")
                    }
                }

                if (userRole == "WAREHOUSE_MANAGER" && queue.failedCount > 0) {
                    OutlinedButton(
                        onClick = { viewModel.deleteAllFailedItems() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete All Failed")
                    }
                }
            }

            // List
            when (val state = uiState) {
                is SyncQueueUiState.Loading -> {
                    LoadingState()
                }
                is SyncQueueUiState.Success -> {
                    if (syncQueue?.pendingItems.isNullOrEmpty() && syncQueue?.failedItems.isNullOrEmpty()) {
                        EmptyState(
                            message = "No pending or failed sync items",
                            modifier = Modifier
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pending items
                            syncQueue?.pendingItems?.let { items ->
                                if (items.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Pending (${items.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    items(items) { item ->
                                        SyncItemCard(
                                            item = item,
                                            onRetry = { viewModel.retrySyncItem(item.id) },
                                            canDelete = false
                                        )
                                    }
                                }
                            }

                            // Failed items
                            syncQueue?.failedItems?.let { items ->
                                if (items.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Failed (${items.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    items(items) { item ->
                                        SyncItemCard(
                                            item = item,
                                            onRetry = { viewModel.retrySyncItem(item.id) },
                                            onDelete = { viewModel.deleteFailedItem(item.id) },
                                            canDelete = userRole == "WAREHOUSE_MANAGER"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                is SyncQueueUiState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountCard(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}

@Composable
private fun SyncItemCard(
    item: com.tionix.rms.feature.sync.domain.model.SyncItem,
    onRetry: () -> Unit,
    onDelete: (() -> Unit)? = null,
    canDelete: Boolean = false
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = getStatusColor(item.status).copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = getStatusIcon(item.status),
                    contentDescription = null,
                    tint = getStatusColor(item.status),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.actionType,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Created: ${item.createdAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.status == SyncStatus.FAILED) {
                    Text(
                        text = "Error: ${item.errorMessage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Retries: ${item.retryCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.status == SyncStatus.FAILED) {
                    IconButton(onClick = onRetry) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry")
                    }
                    if (canDelete && onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getStatusIcon(status: SyncStatus) = when (status) {
    SyncStatus.PENDING -> Icons.Default.Sync
    SyncStatus.SYNCED -> Icons.Default.CheckCircle
    SyncStatus.FAILED -> Icons.Default.Error
}

@Composable
private fun getStatusColor(status: SyncStatus) = when (status) {
    SyncStatus.PENDING -> Color(0xFFFF9800)
    SyncStatus.SYNCED -> Color(0xFF4CAF50)
    SyncStatus.FAILED -> MaterialTheme.colorScheme.error
}
