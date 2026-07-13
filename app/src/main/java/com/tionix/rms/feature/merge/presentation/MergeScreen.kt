package com.tionix.rms.feature.merge.presentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.feature.merge.domain.model.Box
import com.tionix.rms.feature.merge.domain.model.MergeStatus
import com.tionix.rms.feature.merge.domain.model.SessionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeScreen(
    onBack: () -> Unit,
    canStartMerge: Boolean = false, // Role-gated: SUPERVISOR/WAREHOUSE_MANAGER only
    viewModel: MergeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scannedBarcode by viewModel.scannedBarcode.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val destinationBox by viewModel.destinationBox.collectAsStateWithLifecycle()
    val duplicateError by viewModel.duplicateError.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    // Local val captures so smart casts work on delegated properties
    val session = currentSession
    val destBox = destinationBox
    val dupError = duplicateError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merge") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAssignedMerges() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    
                    if (session != null) {
                        IconButton(onClick = { viewModel.resetMerge() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
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
            // Start merge button (when not in flow)
            if (currentSession == null && canStartMerge) {
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
                            
                            Text(
                                text = "Scan destination box to begin merge process",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            }
            
            // Destination box scanning
            if (session != null && session.status == SessionStatus.SCANNING_DESTINATION) {
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Scan Destination Box",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            OutlinedTextField(
                                value = scannedBarcode,
                                onValueChange = viewModel::onScannedBarcodeChanged,
                                label = { Text("Destination Box Barcode") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { viewModel.scanDestinationBox(scannedBarcode) }) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                    }
                                }
                            )
                            
                            Button(
                                onClick = { viewModel.scanDestinationBox(scannedBarcode) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Destination Box")
                            }
                        }
                    }
                }
            }
            
            // Source box scanning
            if (session != null && session.status == SessionStatus.SCANNING_SOURCES) {
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Scan Source Boxes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            if (destBox != null) {
                                Text(
                                    text = "Destination: ${destBox.barcode} - ${destBox.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Files in destination: ${destBox.fileCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            OutlinedTextField(
                                value = scannedBarcode,
                                onValueChange = viewModel::onScannedBarcodeChanged,
                                label = { Text("Source Box Barcode") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    IconButton(onClick = { viewModel.scanSourceBox(scannedBarcode) }) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                    }
                                }
                            )
                            
                            Button(
                                onClick = { viewModel.scanSourceBox(scannedBarcode) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Source Box")
                            }
                        }
                    }
                }
                
                // Duplicate error
                if (duplicateError != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Duplicate Error",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = dupError ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                
                                IconButton(onClick = { viewModel.clearDuplicateError() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }
                
                // Source boxes list
                if (session != null && session.sourceBoxes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Source Boxes (${session.sourceBoxes.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(session.sourceBoxes) { box ->
                        SourceBoxCard(
                            box = box,
                            onRemove = { viewModel.removeSourceBox(box.barcode) }
                        )
                    }
                }
                
                item {
                    Button(
                        onClick = { viewModel.moveToConfirmation() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = session != null && session.sourceBoxes.isNotEmpty()
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Review Merge")
                    }
                }
            }
            
            // Confirmation
            if (session != null && session.status == SessionStatus.CONFIRMING) {
                item {
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Review Merge",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            SummaryItem("Destination Box", destinationBox?.barcode ?: "")
                            SummaryItem("Destination Location", destinationBox?.location ?: "")
                            SummaryItem("Source Boxes", session.sourceBoxes.size.toString())
                            SummaryItem("Total Files", viewModel.getTotalFileCount().toString())
                            
                            if (viewModel.getDestinationCapacity() != null) {
                                val capacity = viewModel.getDestinationCapacity()!!
                                val totalFiles = viewModel.getTotalFileCount()
                                val isOverCapacity = totalFiles > capacity
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isOverCapacity) 
                                            MaterialTheme.colorScheme.errorContainer 
                                        else 
                                            MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isOverCapacity) "Capacity Warning" else "Capacity",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOverCapacity) 
                                                    MaterialTheme.colorScheme.onErrorContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                            Text(
                                                text = "$totalFiles / $capacity files",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isOverCapacity) 
                                                    MaterialTheme.colorScheme.onErrorContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                        
                                        Icon(
                                            if (isOverCapacity) Icons.Default.Warning else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (isOverCapacity) 
                                                MaterialTheme.colorScheme.onErrorContainer 
                                            else 
                                                MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        text = "Source Boxes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(session?.sourceBoxes ?: emptyList()) { box ->
                    SourceBoxCard(
                        box = box,
                        onRemove = { viewModel.removeSourceBox(box.barcode) }
                    )
                }
                
                item {
                    Button(
                        onClick = { viewModel.submitMerge() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isOffline) Color(0xFFFF9800) else Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isOffline) "Queue for Sync" else "Submit Merge")
                    }
                }
            }
            
            // Completed state
            if (uiState is MergeUiState.MergeCompleted) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Text(
                                text = "Merge Completed",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "All source boxes have been merged into destination",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            
                            Button(
                                onClick = { viewModel.resetMerge() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                            ) {
                                Text("Done", color = Color(0xFF4CAF50))
                            }
                        }
                    }
                }
            }
            
            // Assigned Merges (when not in flow)
            if (currentSession == null) {
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
}

@Composable
private fun SummaryItem(label: String, value: String) {
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
private fun SourceBoxCard(box: Box, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = box.barcode,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = box.description,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Location: ${box.location}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Files: ${box.fileCount}",
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
