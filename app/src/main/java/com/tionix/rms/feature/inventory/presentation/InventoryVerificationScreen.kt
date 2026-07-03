package com.tionix.rms.feature.inventory.presentation

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
import com.tionix.rms.feature.inventory.domain.model.VerificationStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryVerificationScreen(
    onBack: () -> Unit,
    viewModel: InventoryVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedLocationId by viewModel.selectedLocationId.collectAsStateWithLifecycle()
    val scannedBarcode by viewModel.scannedBarcode.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Verification") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAssignedVerifications() }) {
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
                            text = "Start New Verification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = selectedLocationId,
                            onValueChange = viewModel::onLocationIdChanged,
                            label = { Text("Location ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = { viewModel.startVerification() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Verification")
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Assigned Verifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            when (val state = uiState) {
                is InventoryVerificationUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is InventoryVerificationUiState.Success -> {
                    if (state.verifications.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No assigned verifications",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.verifications) { verification ->
                            VerificationCard(
                                verification = verification,
                                onScanBox = { 
                                    viewModel.scanBox(verification.id)
                                },
                                onComplete = { viewModel.completeVerification(verification.id) }
                            )
                        }
                    }
                }
                is InventoryVerificationUiState.Error -> {
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
private fun VerificationCard(
    verification: com.tionix.rms.feature.inventory.domain.model.InventoryVerification,
    onScanBox: () -> Unit,
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
                    text = verification.verificationCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(verification.status)
            }
            
            Text(
                text = verification.locationName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progress: ${verification.verifiedBoxes}/${verification.totalBoxes}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (verification.discrepancyBoxes > 0) {
                    Text(
                        text = "Discrepancies: ${verification.discrepancyBoxes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            LinearProgressIndicator(
                progress = verification.verifiedBoxes.toFloat() / verification.totalBoxes.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            
            if (verification.status == VerificationStatus.IN_PROGRESS) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onScanBox,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan Box")
                    }
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Complete")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: VerificationStatus) {
    val (color, label) = when (status) {
        VerificationStatus.PENDING -> MaterialTheme.colorScheme.tertiary to "Pending"
        VerificationStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary to "In Progress"
        VerificationStatus.COMPLETED -> Color(0xFF4CAF50) to "Completed"
        VerificationStatus.FAILED -> MaterialTheme.colorScheme.error to "Failed"
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
