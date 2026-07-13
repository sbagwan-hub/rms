package com.tionix.rms.feature.transfer.presentation

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
import com.tionix.rms.feature.transfer.domain.model.SessionStatus
import com.tionix.rms.feature.transfer.domain.model.TransferItem
import com.tionix.rms.feature.transfer.domain.model.TransferStatus
import com.tionix.rms.feature.transfer.domain.model.TransferType
import com.tionix.rms.ui.common.StepIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    onBack: () -> Unit,
    canStartTransfer: Boolean = false, // Role-gated: SUPERVISOR/WAREHOUSE_MANAGER only
    viewModel: TransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scannedBarcode by viewModel.scannedBarcode.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val selectedTransferType by viewModel.selectedTransferType.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val steps by remember { mutableStateOf(viewModel.getSteps()) }
    // Capture to local val so smart casts work (delegated properties can't be smart-cast directly)
    val session = currentSession

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAssignedTransfers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    
                    if (currentSession != null) {
                        IconButton(onClick = { viewModel.resetTransfer() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Show step indicator when in transfer flow
            if (session != null) {
                StepIndicator(
                    steps = steps,
                    currentStep = currentStep,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Transfer type selection (Step 0)
                if (session == null && canStartTransfer) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Select Transfer Type",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                TransferTypeOption(
                                    type = TransferType.BOX_TO_LOCATION,
                                    title = "Box to Location",
                                    description = "Transfer boxes to a different location within the warehouse",
                                    icon = Icons.Default.LocationOn,
                                    onClick = { viewModel.selectTransferType(TransferType.BOX_TO_LOCATION) }
                                )
                                
                                TransferTypeOption(
                                    type = TransferType.BOX_TO_WAREHOUSE,
                                    title = "Box to Warehouse",
                                    description = "Transfer boxes to a different warehouse",
                                    icon = Icons.Default.Warehouse,
                                    onClick = { viewModel.selectTransferType(TransferType.BOX_TO_WAREHOUSE) }
                                )
                            }
                        }
                    }
                }
                
                // Source scanning (Step 1)
                if (session != null && session.status == SessionStatus.SCANNING_SOURCE) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Scan Source Items",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = "Transfer Type: ${selectedTransferType?.name?.replace("_", " ") ?: ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                OutlinedTextField(
                                    value = scannedBarcode,
                                    onValueChange = viewModel::onScannedBarcodeChanged,
                                    label = { Text("Barcode") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { viewModel.addItem(scannedBarcode) }) {
                                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                        }
                                    }
                                )
                                
                                Button(
                                    onClick = { viewModel.addItem(scannedBarcode) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Item")
                                }
                            }
                        }
                    }
                    
                    // Show scanned items
                    if (session.sourceItems.isNotEmpty()) {
                        item {
                            Text(
                                text = "Scanned Items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(session.sourceItems) { item ->
                            TransferItemCard(
                                item = item,
                                onRemove = { viewModel.removeItem(item.id) }
                            )
                        }
                    }
                    
                    item {
                        Button(
                            onClick = { viewModel.moveToDestinationStep() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = session.sourceItems.isNotEmpty()
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Next: Select Destination")
                        }
                    }
                }
                
                // Destination selection (Step 2)
                if (session != null && session.status == SessionStatus.SELECTING_DESTINATION) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Select Destination",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                OutlinedTextField(
                                    value = destination,
                                    onValueChange = viewModel::onDestinationChanged,
                                    label = { Text("Destination Barcode") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = { viewModel.setDestination(destination) }) {
                                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                        }
                                    }
                                )
                                
                                Button(
                                    onClick = { viewModel.setDestination(destination) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Confirm Destination")
                                }
                            }
                        }
                    }
                }
                
                // Review (Step 3)
                if (session != null && session.status == SessionStatus.REVIEWING) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Review Transfer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                ReviewSummaryItem("Transfer Type", selectedTransferType?.name?.replace("_", " ") ?: "")
                                ReviewSummaryItem("Items to Transfer", session.sourceItems.size.toString())
                                ReviewSummaryItem("Destination", destination)
                            }
                        }
                    }
                    
                    item {
                        Text(
                            text = "Items to Transfer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(session.sourceItems) { item ->
                        TransferItemCard(
                            item = item,
                            onRemove = { viewModel.removeItem(item.id) }
                        )
                    }
                    
                    item {
                        Button(
                            onClick = { viewModel.submitTransfer() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOffline) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isOffline) "Queue for Sync" else "Submit Transfer")
                        }
                    }
                }
                
                // Submit result (Step 4)
                if (session != null && (session.status == SessionStatus.SUBMITTED || session.status == SessionStatus.QUEUED_OFFLINE)) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (session.status == SessionStatus.QUEUED_OFFLINE) 
                                    Color(0xFFFF9800) 
                                else Color(0xFF4CAF50)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    if (session.status == SessionStatus.QUEUED_OFFLINE) Icons.Default.CloudOff
                                    else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                                
                                Text(
                                    text = if (session.status == SessionStatus.QUEUED_OFFLINE) 
                                        "Transfer Queued" 
                                    else 
                                        "Transfer Submitted",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = if (session.status == SessionStatus.QUEUED_OFFLINE) 
                                        "Will sync when connection is restored" 
                                    else 
                                        "Transfer has been submitted successfully",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                
                                Button(
                                    onClick = { viewModel.resetTransfer() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("Done", color = if (session.status == SessionStatus.QUEUED_OFFLINE) 
                                        Color(0xFFFF9800) 
                                    else Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }
                
                // Assigned Transfers (when not in transfer flow)
                if (session == null) {
                    item {
                        Text(
                            text = "Assigned Transfers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    when (val state = uiState) {
                        is TransferUiState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is TransferUiState.Success -> {
                            if (state.transfers.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No assigned transfers",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(state.transfers) { transfer ->
                                    TransferCard(
                                        transfer = transfer,
                                        onComplete = { viewModel.completeTransfer(transfer.id) }
                                    )
                                }
                            }
                        }
                        is TransferUiState.Error -> {
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
}

@Composable
private fun TransferTypeOption(
    type: TransferType,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun TransferItemCard(
    item: TransferItem,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Barcode: ${item.barcode}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "From: ${item.currentLocation}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ReviewSummaryItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TransferCard(
    transfer: com.tionix.rms.feature.transfer.domain.model.Transfer,
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
                    text = transfer.transferCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(transfer.status)
            }
            
            Text(
                text = transfer.boxBarcode,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (transfer.boxName != null) {
                Text(
                    text = transfer.boxName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "From: ${transfer.sourceLocation}",
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "To: ${transfer.destinationLocation}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (transfer.reason != null) {
                Text(
                    text = "Reason: ${transfer.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (transfer.status == TransferStatus.ACCEPTED) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Transfer")
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TransferStatus) {
    val color: Color
    val label: String
    when (status) {
        TransferStatus.PENDING_ACCEPTANCE -> { color = MaterialTheme.colorScheme.tertiary; label = "Pending Acceptance" }
        TransferStatus.ACCEPTED -> { color = MaterialTheme.colorScheme.secondary; label = "Accepted" }
        TransferStatus.COMPLETED -> { color = Color(0xFF4CAF50); label = "Completed" }
        TransferStatus.REJECTED -> { color = MaterialTheme.colorScheme.error; label = "Rejected" }
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
