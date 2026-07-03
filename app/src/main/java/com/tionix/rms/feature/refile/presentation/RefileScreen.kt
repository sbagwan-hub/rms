package com.tionix.rms.feature.refile.presentation

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
import com.tionix.rms.feature.refile.domain.model.RefileStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefileScreen(
    onBack: () -> Unit,
    viewModel: RefileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scannedBarcode by viewModel.scannedBarcode.collectAsStateWithLifecycle()
    val newLocation by viewModel.newLocation.collectAsStateWithLifecycle()
    val reason by viewModel.reason.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAssignedRefiles() }) {
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
                            text = "Start New Refile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = scannedBarcode,
                            onValueChange = viewModel::onScannedBarcodeChanged,
                            label = { Text("File Barcode") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { viewModel.scanFile() }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                }
                            }
                        )
                        
                        OutlinedTextField(
                            value = newLocation,
                            onValueChange = viewModel::onNewLocationChanged,
                            label = { Text("New Location") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = reason,
                            onValueChange = viewModel::onReasonChanged,
                            label = { Text("Reason (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = { viewModel.startRefile() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Refile")
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Assigned Refiles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            when (val state = uiState) {
                is RefileUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is RefileUiState.Success -> {
                    if (state.refiles.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No assigned refiles",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.refiles) { refile ->
                            RefileCard(
                                refile = refile,
                                onComplete = { viewModel.completeRefile(refile.id) }
                            )
                        }
                    }
                }
                is RefileUiState.Error -> {
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
private fun RefileCard(
    refile: com.tionix.rms.feature.refile.domain.model.Refile,
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
                    text = refile.refileCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(refile.status)
            }
            
            Text(
                text = refile.fileBarcode,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (refile.fileName != null) {
                Text(
                    text = refile.fileName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "From: ${refile.currentLocation}",
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "To: ${refile.newLocation}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (refile.reason != null) {
                Text(
                    text = "Reason: ${refile.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (refile.status == RefileStatus.IN_PROGRESS) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Refile")
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: RefileStatus) {
    val (color, label) = when (status) {
        RefileStatus.PENDING -> MaterialTheme.colorScheme.tertiary to "Pending"
        RefileStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary to "In Progress"
        RefileStatus.COMPLETED -> Color(0xFF4CAF50) to "Completed"
        RefileStatus.FAILED -> MaterialTheme.colorScheme.error to "Failed"
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
