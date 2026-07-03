package com.tionix.rms.feature.merge.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.feature.merge.domain.model.MergeStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeScreen(
    onBack: () -> Unit,
    viewModel: MergeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sourceBoxBarcode by viewModel.sourceBoxBarcode.collectAsStateWithLifecycle()
    val destinationBoxBarcode by viewModel.destinationBoxBarcode.collectAsStateWithLifecycle()
    val reason by viewModel.reason.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merge") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAssignedMerges() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Start New Merge",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = sourceBoxBarcode,
                            onValueChange = viewModel::onSourceBoxBarcodeChanged,
                            label = { Text("Source Box Barcode") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.scanBox() }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                }
                            }
                        )
                        
                        OutlinedTextField(
                            value = destinationBoxBarcode,
                            onValueChange = viewModel::onDestinationBoxBarcodeChanged,
                            label = { Text("Destination Box Barcode") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = reason,
                            onValueChange = viewModel::onReasonChanged,
                            label = { Text("Reason (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = { viewModel.startMerge() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Merge")
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Assigned Merges",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            when (val state = uiState) {
                is MergeUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is MergeUiState.Success -> {
                    if (state.merges.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No assigned merges",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.merges) { merge ->
                            MergeCard(
                                merge = merge,
                                onComplete = { viewModel.completeMerge(merge.id) }
                            )
                        }
                    }
                }
                is MergeUiState.Error -> {
                    item {
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
                else -> {}
            }
        }
    }
}

@Composable
private fun MergeCard(
    merge: com.tionix.rms.feature.merge.domain.model.Merge,
    onComplete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = merge.mergeCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(merge.status)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Source:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = merge.sourceBoxBarcode,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (merge.sourceBoxName != null) {
                        Text(
                            text = merge.sourceBoxName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Icon(Icons.Default.ArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                
                Column {
                    Text(
                        text = "Destination:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = merge.destinationBoxBarcode,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (merge.destinationBoxName != null) {
                        Text(
                            text = merge.destinationBoxName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Files: ${merge.fileCount}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (merge.reason != null) {
                    Text(
                        text = "Reason: ${merge.reason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (merge.status == MergeStatus.IN_PROGRESS) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Merge")
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: MergeStatus) {
    val (color, label) = when (status) {
        MergeStatus.PENDING -> MaterialTheme.colorScheme.tertiary to "Pending"
        MergeStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary to "In Progress"
        MergeStatus.COMPLETED -> Color(0xFF4CAF50) to "Completed"
        MergeStatus.FAILED -> MaterialTheme.colorScheme.error to "Failed"
    }
    
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
