package com.tionix.rms.feature.history.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.tionix.rms.feature.history.domain.model.ActionFilter
import com.tionix.rms.feature.history.domain.model.DateFilter
import com.tionix.rms.feature.history.domain.model.SyncStatus
import com.tionix.rms.ui.common.EmptyState
import com.tionix.rms.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    userRole: String = "OPERATOR", // OPERATOR sees own actions, SUPERVISOR/MANAGER see all
    userId: String? = null, // Current user's ID for filtering
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actionFilter by viewModel.actionFilter.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()

    LaunchedEffect(userRole, userId) {
        // Set user filter based on role
        val filteredUserId = if (userRole in listOf("SUPERVISOR", "WAREHOUSE_MANAGER")) {
            null // See all actions
        } else {
            userId // See own actions only
        }
        viewModel.setUserId(filteredUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // Action Filter Chips
            Text(
                text = "Filter by Action",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionFilterChip(
                    label = "All",
                    selected = actionFilter == ActionFilter.ALL,
                    onClick = { viewModel.setActionFilter(ActionFilter.ALL) }
                )
                ActionFilterChip(
                    label = "Fresh Box",
                    selected = actionFilter == ActionFilter.FRESH_BOX,
                    onClick = { viewModel.setActionFilter(ActionFilter.FRESH_BOX) }
                )
                ActionFilterChip(
                    label = "Refile",
                    selected = actionFilter == ActionFilter.REFILE,
                    onClick = { viewModel.setActionFilter(ActionFilter.REFILE) }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionFilterChip(
                    label = "Transfer",
                    selected = actionFilter == ActionFilter.TRANSFER,
                    onClick = { viewModel.setActionFilter(ActionFilter.TRANSFER) }
                )
                ActionFilterChip(
                    label = "Segregation",
                    selected = actionFilter == ActionFilter.SEGREGATION,
                    onClick = { viewModel.setActionFilter(ActionFilter.SEGREGATION) }
                )
                ActionFilterChip(
                    label = "Merge",
                    selected = actionFilter == ActionFilter.MERGE,
                    onClick = { viewModel.setActionFilter(ActionFilter.MERGE) }
                )
                ActionFilterChip(
                    label = "Verification",
                    selected = actionFilter == ActionFilter.VERIFICATION,
                    onClick = { viewModel.setActionFilter(ActionFilter.VERIFICATION) }
                )
            }
            
            // Date Filter Chips
            Text(
                text = "Filter by Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilterChip(
                    label = "Today",
                    selected = dateFilter == DateFilter.TODAY,
                    onClick = { viewModel.setDateFilter(DateFilter.TODAY) }
                )
                DateFilterChip(
                    label = "7 Days",
                    selected = dateFilter == DateFilter.SEVEN_DAYS,
                    onClick = { viewModel.setDateFilter(DateFilter.SEVEN_DAYS) }
                )
                DateFilterChip(
                    label = "30 Days",
                    selected = dateFilter == DateFilter.THIRTY_DAYS,
                    onClick = { viewModel.setDateFilter(DateFilter.THIRTY_DAYS) }
                )
                DateFilterChip(
                    label = "All",
                    selected = dateFilter == DateFilter.ALL,
                    onClick = { viewModel.setDateFilter(DateFilter.ALL) }
                )
            }
            
            // History List
            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    LoadingState()
                }
                is HistoryUiState.Success -> {
                    if (state.history.isEmpty()) {
                        EmptyState(
                            message = "No history found for selected filters",
                            modifier = Modifier
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.history) { item ->
                                HistoryItemCard(
                                    item = item,
                                    onRetrySync = { viewModel.retrySync(item.id) }
                                )
                            }
                        }
                    }
                }
                is HistoryUiState.Error -> {
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
private fun ActionFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun DateFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun HistoryItemCard(
    item: com.tionix.rms.feature.history.domain.model.HistoryItem,
    onRetrySync: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = getActionTypeColor(item.actionType).copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = getActionTypeIcon(item.actionType),
                    contentDescription = null,
                    tint = getActionTypeColor(item.actionType),
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.itemName ?: item.itemId,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "By: ${item.userName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.details != null) {
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            SyncStatusChip(
                status = item.syncStatus,
                onRetrySync = if (item.syncStatus == SyncStatus.FAILED) onRetrySync else null
            )
        }
    }
}

private data class SyncChipData(val color: Color, val label: String, val icon: ImageVector)

@Composable
private fun SyncStatusChip(
    status: SyncStatus,
    onRetrySync: (() -> Unit)? = null
) {
    val chipData = when (status) {
        SyncStatus.SYNCED -> SyncChipData(Color(0xFF4CAF50), "Synced", Icons.Default.CheckCircle)
        SyncStatus.PENDING -> SyncChipData(Color(0xFFFF9800), "Pending", Icons.Default.Sync)
        SyncStatus.FAILED -> SyncChipData(MaterialTheme.colorScheme.error, "Failed", Icons.Default.Refresh)
    }
    val color = chipData.color
    val label = chipData.label
    val icon = chipData.icon
    
    if (status == SyncStatus.FAILED && onRetrySync != null) {
        Button(
            onClick = onRetrySync,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.height(32.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    } else {
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun getActionTypeIcon(type: com.tionix.rms.feature.history.domain.model.ActionType): ImageVector = when (type) {
    com.tionix.rms.feature.history.domain.model.ActionType.FRESH_BOX_MOVE -> Icons.Default.MoveDown
    com.tionix.rms.feature.history.domain.model.ActionType.REFILE -> Icons.Default.Upload
    com.tionix.rms.feature.history.domain.model.ActionType.TRANSFER -> Icons.Default.SwapHoriz
    com.tionix.rms.feature.history.domain.model.ActionType.SEGREGATION -> Icons.Default.CallSplit
    com.tionix.rms.feature.history.domain.model.ActionType.MERGE -> Icons.Default.MergeType
    com.tionix.rms.feature.history.domain.model.ActionType.VERIFICATION -> Icons.Default.FactCheck
    com.tionix.rms.feature.history.domain.model.ActionType.OTHER -> Icons.Default.History
}

@Composable
private fun getActionTypeColor(type: com.tionix.rms.feature.history.domain.model.ActionType): Color = when (type) {
    com.tionix.rms.feature.history.domain.model.ActionType.FRESH_BOX_MOVE -> Color(0xFF4CAF50)
    com.tionix.rms.feature.history.domain.model.ActionType.REFILE -> MaterialTheme.colorScheme.secondary
    com.tionix.rms.feature.history.domain.model.ActionType.TRANSFER -> MaterialTheme.colorScheme.primary
    com.tionix.rms.feature.history.domain.model.ActionType.SEGREGATION -> MaterialTheme.colorScheme.tertiary
    com.tionix.rms.feature.history.domain.model.ActionType.MERGE -> Color(0xFF9C27B0)
    com.tionix.rms.feature.history.domain.model.ActionType.VERIFICATION -> Color(0xFFFF9800)
    com.tionix.rms.feature.history.domain.model.ActionType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
}
