package com.tionix.rms.feature.refile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.tionix.rms.feature.refile.domain.model.RefileAction
import com.tionix.rms.feature.refile.domain.model.RefileActionStatus
import com.tionix.rms.feature.refile.domain.model.RefileStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefileScreen(
    onBack: () -> Unit,
    canOverride: Boolean = false, // Role-gated: SUPERVISOR/WAREHOUSE_MANAGER only
    viewModel: RefileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scannedBarcode by viewModel.scannedBarcode.collectAsStateWithLifecycle()
    val currentFile by viewModel.currentFile.collectAsStateWithLifecycle()
    val destinationBoxBarcode by viewModel.destinationBoxBarcode.collectAsStateWithLifecycle()
    val overrideReason by viewModel.overrideReason.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val sessionActions by viewModel.sessionActions.collectAsStateWithLifecycle()
    val showMismatchDialog by viewModel.showMismatchDialog.collectAsStateWithLifecycle()
    val showSessionSummary by viewModel.showSessionSummary.collectAsStateWithLifecycle()
    val batchMode by viewModel.batchMode.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> viewModel.scannerRepository.enableScanner()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> viewModel.scannerRepository.disableScanner()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.scannerRepository.disableScanner()
        }
    }

    val session = currentSession
    val file = currentFile

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
                    // Batch mode toggle
                    Switch(
                        checked = batchMode,
                        onCheckedChange = { viewModel.toggleBatchMode() },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Batch Mode", modifier = Modifier.padding(end = 16.dp))
                    
                    IconButton(onClick = { viewModel.loadAssignedRefiles() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    
                    if (batchMode && sessionActions.isNotEmpty()) {
                        IconButton(onClick = { viewModel.showSessionSummary() }) {
                            Icon(Icons.Default.List, contentDescription = "Session Summary")
                        }
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
            // File Scanning Section
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (batchMode) "Batch Mode: File Scanning" else "Scan File",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (batchMode && session != null) {
                                Text(
                                    text = "Session: ${session.sessionId.take(8)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        OutlinedTextField(
                            value = scannedBarcode,
                            onValueChange = viewModel::onScannedBarcodeChanged,
                            label = { Text("File Barcode") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.scannerRepository.startCameraScan(context) { barcode ->
                                        viewModel.onScannedBarcodeChanged(barcode)
                                        viewModel.scanFile()
                                    }
                                }) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                }
                            }
                        )
                        
                        // Show current file details when scanned
                        if (file != null) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "File: ${file.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Barcode: ${file.barcode}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Current Box: ${file.currentBox.barcode}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Current Location: ${file.currentLocation.room} - ${file.currentLocation.name}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        
                        // Destination Box Scanning
                        if (file != null) {
                            OutlinedTextField(
                                value = destinationBoxBarcode,
                                onValueChange = viewModel::onDestinationBoxBarcodeChanged,
                                label = { Text("Destination Box Barcode") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        viewModel.scannerRepository.startCameraScan(context) { barcode ->
                                            viewModel.onDestinationBoxBarcodeChanged(barcode)
                                            viewModel.confirmRefile()
                                        }
                                    }) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                    }
                                }
                            )
                            
                            Button(
                                onClick = { viewModel.confirmRefile() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (batchMode) "Confirm & Continue" else "Confirm Refile")
                            }
                        }
                    }
                }
            }
            
            // Session Summary (batch mode)
            if (batchMode && sessionActions.isNotEmpty()) {
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Session Summary",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row {
                                    if (sessionActions.isNotEmpty()) {
                                        Button(
                                            onClick = { viewModel.undoLastAction() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            )
                                        ) {
                                            Icon(Icons.Default.Undo, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Undo Last")
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.endSession() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.Stop, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("End Session")
                                    }
                                }
                            }
                            
                            val summary = viewModel.getSessionSummary()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SummaryItem("Total", summary.totalRefiled)
                                SummaryItem("Success", summary.successful, Color(0xFF4CAF50))
                                SummaryItem("Override", summary.overridden, Color(0xFFFF9800))
                                SummaryItem("Rejected", summary.rejected, Color(0xFFF44336))
                            }
                        }
                    }
                }
                
                // Session Actions List
                item {
                    Text(
                        text = "Files Refiled This Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(sessionActions) { action ->
                    SessionActionCard(action)
                }
            }
            
            // Assigned Refiles Section (non-batch mode)
            if (!batchMode) {
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
    
    // Mismatch Dialog
    if (showMismatchDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissMismatchDialog() },
            title = { Text("Location Mismatch") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("The destination box does not match the expected location.")
                    if (canOverride) {
                        Text(
                            "As a Supervisor or Warehouse Manager, you can override this mismatch by providing a reason.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            "Only Supervisors and Warehouse Managers can override location mismatches.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    if (canOverride) {
                        OutlinedTextField(
                            value = overrideReason,
                            onValueChange = viewModel::onOverrideReasonChanged,
                            label = { Text("Override Reason (Required)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                if (canOverride) {
                    Button(onClick = { viewModel.overrideMismatch() }) {
                        Text("Override")
                    }
                } else {
                    Button(onClick = { viewModel.dismissMismatchDialog() }) {
                        Text("OK")
                    }
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.dismissMismatchDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Session Summary Dialog
    if (showSessionSummary) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSessionSummary() },
            title = { Text("Session Summary") },
            text = {
                val summary = viewModel.getSessionSummary()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Files Refiled: ${summary.totalRefiled}")
                    Text("Successful: ${summary.successful}", color = Color(0xFF4CAF50))
                    Text("Overridden: ${summary.overridden}", color = Color(0xFFFF9800))
                    Text("Rejected: ${summary.rejected}", color = Color(0xFFF44336))
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.dismissSessionSummary() }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SummaryItem(label: String, value: Int, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SessionActionCard(action: RefileAction) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = action.fileRecord.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Barcode: ${action.fileRecord.barcode}",
                style = MaterialTheme.typography.bodySmall
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "From: ${action.sourceBox.barcode}",
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(Icons.Default.ArrowForward, contentDescription = null)
                Text(
                    text = "To: ${action.destinationBox.barcode}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            val (statusColor, statusLabel) = when (action.status) {
                RefileActionStatus.CONFIRMED -> Color(0xFF4CAF50) to "Confirmed"
                RefileActionStatus.OVERRIDDEN -> Color(0xFFFF9800) to "Overridden"
                RefileActionStatus.REJECTED_MISMATCH -> Color(0xFFF44336) to "Rejected"
                RefileActionStatus.PENDING -> MaterialTheme.colorScheme.tertiary to "Pending"
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = statusLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
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
