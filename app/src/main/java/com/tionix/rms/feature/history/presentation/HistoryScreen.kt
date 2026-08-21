package com.tionix.rms.feature.history.presentation

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.feature.history.domain.model.PendingOperationItem
import com.tionix.rms.feature.history.domain.model.SyncedOperationItem
import com.tionix.rms.ui.common.EmptyState
import com.tionix.rms.ui.common.LoadingState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "HistoryScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onBoxClick: (String) -> Unit = {},
    onFileClick: (String) -> Unit = {},
    onRefileClick: () -> Unit = {},
    onTransferClick: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val pendingCount = uiState.pendingOps.count { it.state == "QUEUED" || it.state == "FAILED" }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.refreshError.collect { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "History",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Operation Logs & Audit Trail",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    com.tionix.rms.ui.components.RMSRefreshIconButton(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refresh() }
                    )
                    TextButton(onClick = { viewModel.manualSync() }) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Sync now")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
                Tab(
                    selected = uiState.selectedTab == HistoryTab.PENDING,
                    onClick = { viewModel.selectTab(HistoryTab.PENDING) },
                    text = {
                        BadgedBox(
                            badge = {
                                if (pendingCount > 0) {
                                    Badge { Text(pendingCount.toString()) }
                                }
                            }
                        ) {
                            Text("Pending Sync")
                        }
                    }
                )
                Tab(
                    selected = uiState.selectedTab == HistoryTab.SYNCED,
                    onClick = { viewModel.selectTab(HistoryTab.SYNCED) },
                    text = { Text("Synced") }
                )
            }

            when (uiState.selectedTab) {
                HistoryTab.PENDING -> PendingTab(uiState.pendingOps)
                HistoryTab.SYNCED -> SyncedTab(
                    items = uiState.syncedOps,
                    loading = uiState.loadingSynced,
                    error = uiState.syncedError,
                    onRetry = { viewModel.loadSynced() },
                    onItemClick = { item ->
                        val targetBoxId = item.boxId ?: item.boxBarcode
                        val targetFileId = item.fileId ?: item.fileBarcode
                        Log.d(TAG, "History click: id=${item.id}, type=${item.type}, boxId=$targetBoxId, fileId=$targetFileId")

                        when (item.type.uppercase()) {
                            "FRESH_BOX", "FRESH_BOX_MOVE", "INTAKE", "INVENTORY" -> {
                                if (!targetBoxId.isNullOrBlank()) {
                                    Log.d(TAG, "Navigating to BoxDetails: $targetBoxId")
                                    onBoxClick(targetBoxId)
                                }
                            }
                            "REFILE" -> {
                                if (!targetFileId.isNullOrBlank()) {
                                    Log.d(TAG, "Navigating to FileDetails: $targetFileId")
                                    onFileClick(targetFileId)
                                } else if (!targetBoxId.isNullOrBlank()) {
                                    Log.d(TAG, "Navigating to BoxDetails: $targetBoxId")
                                    onBoxClick(targetBoxId)
                                } else {
                                    onRefileClick()
                                }
                            }
                            "TRANSFER" -> {
                                if (!targetBoxId.isNullOrBlank()) {
                                    Log.d(TAG, "Navigating to BoxDetails: $targetBoxId")
                                    onBoxClick(targetBoxId)
                                } else {
                                    onTransferClick()
                                }
                            }
                            else -> {
                                if (!targetBoxId.isNullOrBlank()) {
                                    Log.d(TAG, "Navigating to BoxDetails: $targetBoxId")
                                    onBoxClick(targetBoxId)
                                } else if (!targetFileId.isNullOrBlank()) {
                                    Log.d(TAG, "Navigating to FileDetails: $targetFileId")
                                    onFileClick(targetFileId)
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PendingTab(items: List<PendingOperationItem>) {
    if (items.isEmpty()) {
        EmptyState(
            message = "All operations synced ✓",
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.clientOpId }) { item ->
            PendingOperationCard(item)
        }
    }
}

@Composable
private fun PendingOperationCard(item: PendingOperationItem) {
    val formatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.type.replace('_', ' '), fontWeight = FontWeight.Bold)
                StateBadge(item.state)
            }
            Text(
                text = item.clientOpId.take(8) + "…",
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formatter.format(Date(item.createdAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (item.state == "FAILED" && !item.lastError.isNullOrBlank()) {
                Text(
                    text = item.lastError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StateBadge(state: String) {
    val (label, color) = when (state) {
        "SENDING" -> "SENDING" to Color(0xFF2563EB)
        "FAILED" -> "FAILED" to Color(0xFFDC2626)
        else -> "QUEUED" to Color(0xFF64748B)
    }
    Surface(color = color.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SyncedTab(
    items: List<SyncedOperationItem>,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onItemClick: (SyncedOperationItem) -> Unit
) {
    when {
        loading -> LoadingState(modifier = Modifier.fillMaxSize())
        error != null -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRetry) { Text("Retry") }
            }
        }
        items.isEmpty() -> EmptyState(message = "No synced operations yet", modifier = Modifier.fillMaxSize())
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.id }) { item ->
                SyncedOperationCard(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun SyncedOperationCard(
    item: SyncedOperationItem,
    onClick: () -> Unit
) {
    val isRejected = item.status.equals("REJECTED", ignoreCase = true)
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isRejected) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.type.replace('_', ' '), fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isRejected) Icons.Default.Sync else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isRejected) MaterialTheme.colorScheme.error else Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            item.status,
                            color = if (isRejected) MaterialTheme.colorScheme.error else Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Text(item.summary, style = MaterialTheme.typography.bodyMedium)
                Text(
                    item.performedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                item.reasonCode?.let {
                    Text("Reason: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
