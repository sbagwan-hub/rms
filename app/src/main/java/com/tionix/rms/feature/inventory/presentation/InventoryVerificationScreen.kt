package com.tionix.rms.feature.inventory.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.tionix.rms.ui.components.ScannerEffect
import com.tionix.rms.utils.scanner.ScannerManager
import com.tionix.rms.feature.inventory.domain.model.VerificationStatus

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.tionix.rms.feature.inventory.domain.model.Box
import com.tionix.rms.feature.inventory.domain.model.BoxStatus
import com.tionix.rms.feature.inventory.domain.model.InventoryVerification
import com.tionix.rms.feature.inventory.domain.model.ScannedBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryVerificationScreen(
    onBack: () -> Unit,
    viewModel: InventoryVerificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedLocationId by viewModel.selectedLocationId.collectAsStateWithLifecycle()
    val scannedBarcode by viewModel.scannedBarcode.collectAsStateWithLifecycle()
    val currentVerification by viewModel.currentVerification.collectAsStateWithLifecycle()
    val expectedBoxes by viewModel.expectedBoxes.collectAsStateWithLifecycle()
    val scannedBoxes by viewModel.scannedBoxes.collectAsStateWithLifecycle()
    val showDiscrepancyDialog by viewModel.showDiscrepancyDialog.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scannerManager = remember {
        val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
            context.applicationContext,
            InventoryVerificationScreenEntryPoint::class.java
        )
        entryPoint.scannerManager()
    }

    val activeVerification = currentVerification

    if (activeVerification != null) {
        ScannerEffect(
            scannerManager = scannerManager,
            continuousMode = true,
            onBarcodeScanned = { barcode ->
                viewModel.verifyBox(barcode)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (activeVerification != null) 
                            "Verify: ${activeVerification.verificationCode}" 
                        else 
                            "Inventory Verification"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (activeVerification != null) {
                            viewModel.exitVerification()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (activeVerification == null) {
                        IconButton(onClick = { viewModel.loadAssignedVerifications() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    } else {
                        IconButton(onClick = { viewModel.exitVerification() }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (activeVerification != null) {
            // Active Verification scanning/progress screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Details & Progress
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Box: ${activeVerification.locationName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        val progress = viewModel.getProgress()
                        val verifiedCount = viewModel.getVerifiedCount()
                        val totalCount = expectedBoxes.size
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress: $verifiedCount / $totalCount files",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "$progress%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        LinearProgressIndicator(
                            progress = if (totalCount > 0) verifiedCount.toFloat() / totalCount.toFloat() else 0f,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }
                }

                // Summary Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val stats = listOf(
                        "Verified" to viewModel.getVerifiedCount() to Color(0xFF4CAF50),
                        "Missing" to viewModel.getMissingCount() to Color(0xFFF44336),
                        "Unexpected" to viewModel.getUnexpectedCount() to Color(0xFFFF9800)
                    )
                    
                    stats.forEach { (labelCount, color) ->
                        val (label, count) = labelCount
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(
                                containerColor = color.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color
                                )
                            }
                        }
                    }
                }

                // Barcode input for scanning boxes
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Scan File Barcode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        OutlinedTextField(
                            value = scannedBarcode,
                            onValueChange = viewModel::onScannedBarcodeChanged,
                            label = { Text("File Barcode") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    scannerManager.startCameraScan(context) { barcode ->
                                        viewModel.onScannedBarcodeChanged(barcode)
                                        viewModel.verifyBox(barcode)
                                    }
                                }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                }
                            }
                        )
                        
                        Button(
                            onClick = { viewModel.verifyBox(scannedBarcode) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify File")
                        }
                    }
                }

                // Expected & Scanned Boxes lists
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (scannedBoxes.isNotEmpty()) {
                        item {
                            Text(
                                text = "Scanned Files (${scannedBoxes.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(scannedBoxes) { box ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = box.barcode,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = box.scanStatus.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (box.scanStatus.name == "VERIFIED") Color(0xFF4CAF50) else Color(0xFFFF9800)
                                    )
                                }
                            }
                        }
                    }

                    val remaining = expectedBoxes.filter { it.status == BoxStatus.PENDING }
                    if (remaining.isNotEmpty()) {
                        item {
                            Text(
                                text = "Remaining Expected Files (${remaining.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(remaining) { box ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = box.barcode,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = box.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Complete Action Button
                Button(
                    onClick = { viewModel.prepareForSubmit() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Verification")
                }
            }

            // Discrepancy Dialog
            if (showDiscrepancyDialog) {
                val missing = viewModel.getMissingCount()
                val unexpected = viewModel.getUnexpectedCount()
                
                AlertDialog(
                    onDismissRequest = { viewModel.dismissDiscrepancyDialog() },
                    title = { Text("Submit with Discrepancies?") },
                    text = {
                        Text("This verification has discrepancies:\n- Missing files: $missing\n- Unexpected files: $unexpected\n\nDo you want to proceed and submit?")
                    },
                    confirmButton = {
                        Button(onClick = { viewModel.completeVerification() }) {
                            Text("Submit")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissDiscrepancyDialog() }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } else {
            // Assigned Verifications list screen
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
                                label = { Text("Box Barcode") },
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
                                        viewModel.resumeVerification(verification)
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

// Entry point interface to resolve Hilt ScannerManager inside Composable
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface InventoryVerificationScreenEntryPoint {
    fun scannerManager(): ScannerManager
}
