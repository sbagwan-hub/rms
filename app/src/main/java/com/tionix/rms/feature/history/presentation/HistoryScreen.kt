package com.tionix.rms.feature.history.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingCount = uiState.pendingOps.count { it.state == "QUEUED" || it.state == "FAILED" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.manualSync() }) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Sync now")
                    }
                }
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
                    onRetry = { viewModel.loadSynced() }
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
    Card(modifier = Modifier.fillMaxWidth()) {
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
    onRetry: () -> Unit
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
                SyncedOperationCard(item)
            }
        }
    }
}

@Composable
private fun SyncedOperationCard(item: SyncedOperationItem) {
    val isRejected = item.status.equals("REJECTED", ignoreCase = true)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRejected) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
    }
}
