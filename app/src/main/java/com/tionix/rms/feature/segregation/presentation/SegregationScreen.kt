package com.tionix.rms.feature.segregation.presentation

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
import com.tionix.rms.feature.segregation.domain.model.SegregationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegregationScreen(
    onBack: () -> Unit,
    viewModel: SegregationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scannedBarcode by viewModel.scannedBarcode.collectAsStateWithLifecycle()
    val reasonCode by viewModel.reasonCode.collectAsStateWithLifecycle()
    val reason by viewModel.reason.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Segregation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAssignedSegregations() }) {
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
                            text = "Start New Segregation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = scannedBarcode,
                            onValueChange = viewModel::onScannedBarcodeChanged,
                            label = { Text("Box Barcode") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.scanBox() }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                }
                            }
                        )
                        
                        OutlinedTextField(
                            value = reasonCode,
                            onValueChange = viewModel::onReasonCodeChanged,
                            label = { Text("Reason Code") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = reason,
                            onValueChange = viewModel::onReasonChanged,
                            label = { Text("Reason (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = { viewModel.startSegregation() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Segregation")
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Assigned Segregations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            when (val state = uiState) {
                is SegregationUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is SegregationUiState.Success -> {
                    if (state.segregations.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No assigned segregations",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.segregations) { segregation ->
                            SegregationCard(
                                segregation = segregation,
                                onComplete = { viewModel.completeSegregation(segregation.id) }
                            )
                        }
                    }
                }
                is SegregationUiState.Error -> {
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
private fun SegregationCard(
    segregation: com.tionix.rms.feature.segregation.domain.model.Segregation,
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
                    text = segregation.segregationCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(segregation.status)
            }
            
            Text(
                text = segregation.boxBarcode,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (segregation.boxName != null) {
                Text(
                    text = segregation.boxName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "File Count: ${segregation.fileCount}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (segregation.reasonCode != null) {
                    Text(
                        text = "Reason: ${segregation.reasonCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (segregation.reason != null) {
                Text(
                    text = segregation.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (segregation.status == SegregationStatus.IN_PROGRESS) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Segregation")
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: SegregationStatus) {
    val (color, label) = when (status) {
        SegregationStatus.PENDING -> MaterialTheme.colorScheme.tertiary to "Pending"
        SegregationStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary to "In Progress"
        SegregationStatus.COMPLETED -> Color(0xFF4CAF50) to "Completed"
        SegregationStatus.FAILED -> MaterialTheme.colorScheme.error to "Failed"
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
