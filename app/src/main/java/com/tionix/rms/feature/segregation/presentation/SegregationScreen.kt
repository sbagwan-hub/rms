package com.tionix.rms.feature.segregation.presentation

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
import com.tionix.rms.feature.segregation.domain.model.FileRecord
import com.tionix.rms.feature.segregation.domain.model.SessionStatus
import com.tionix.rms.feature.segregation.domain.model.SegregationStatus
import com.tionix.rms.ui.common.StepIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegregationScreen(
    onBack: () -> Unit,
    canStartSegregation: Boolean = false, // Role-gated: SUPERVISOR/WAREHOUSE_MANAGER only
    viewModel: SegregationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scannedBarcode by viewModel.scannedBarcode.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sourceBox by viewModel.sourceBox.collectAsStateWithLifecycle()
    val targetBox by viewModel.targetBox.collectAsStateWithLifecycle()
    val validationError by viewModel.validationError.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
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

    // Local val captures so smart casts work on delegated properties
    val session = currentSession
    val srcBox = sourceBox
    val valError = validationError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Segregation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAssignedSegregations() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    
                    if (session != null) {
                        IconButton(onClick = { viewModel.resetSegregation() }) {
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
            // Live counters when in segregation flow
            if (session != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CounterItem("Remaining", viewModel.getRemainingCount(), Color(0xFF4CAF50))
                        CounterItem("Moved", viewModel.getMovedCount(), MaterialTheme.colorScheme.primary)
                        CounterItem("Total", viewModel.getTotalCount(), MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start segregation button (when not in flow)
                if (currentSession == null && canStartSegregation) {
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
                                
                                Text(
                                    text = "Scan source box to begin segregation process",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                }
                
                // Source box scanning
                if (session != null && session.status == SessionStatus.SCANNING_SOURCE) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Scan Source Box",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                OutlinedTextField(
                                    value = scannedBarcode,
                                    onValueChange = viewModel::onScannedBarcodeChanged,
                                    label = { Text("Source Box Barcode") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            viewModel.scannerRepository.startCameraScan(context) { barcode ->
                                                viewModel.onScannedBarcodeChanged(barcode)
                                                viewModel.scanSourceBox(barcode)
                                            }
                                        }) {
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
                                    Text("Scan Source Box")
                                }
                            }
                        }
                    }
                }
                
                // Target box scanning
                if (session != null && session.status == SessionStatus.SCANNING_TARGET) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Scan Target Box",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                if (srcBox != null) {
                                    Text(
                                        text = "Source: ${srcBox.barcode} - ${srcBox.description}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                OutlinedTextField(
                                    value = scannedBarcode,
                                    onValueChange = viewModel::onScannedBarcodeChanged,
                                    label = { Text("Target Box Barcode") },
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            viewModel.scannerRepository.startCameraScan(context) { barcode ->
                                                viewModel.onScannedBarcodeChanged(barcode)
                                                viewModel.scanTargetBox(barcode)
                                            }
                                        }) {
                                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                        }
                                    }
                                )
                                
                                Button(
                                    onClick = { viewModel.scanTargetBox(scannedBarcode) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Scan Target Box")
                                }
                            }
                        }
                    }
                    
                    // Show source files
                    if (session?.sourceFiles?.isNotEmpty() == true) {
                        item {
                            Text(
                                text = "Files in Source Box",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(session.sourceFiles) { file ->
                            FileCard(file)
                        }
                    }
                }
                
                // File movement
                if (session != null && session.status == SessionStatus.MOVING_FILES) {
                    item {
                        Card {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Move Files",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Source: ${sourceBox?.barcode}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    @Suppress("DEPRECATION")
                                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                                    Text(
                                        text = "Target: ${targetBox?.barcode}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
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
                                                viewModel.moveFile(barcode)
                                            }
                                        }) {
                                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
                                        }
                                    }
                                )
                                
                                Button(
                                    onClick = { viewModel.moveFile(scannedBarcode) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Move File")
                                }
                            }
                        }
                    }
                    
                    // Validation error
                    if (valError != null) {
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
                                            text = "Validation Error",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "File does not belong to source box",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = "Barcode: ${valError.barcode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                    
                                    IconButton(onClick = { viewModel.clearValidationError() }) {
                                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Remaining files
                    if (session?.sourceFiles?.isNotEmpty() == true) {
                        item {
                            Text(
                                text = "Remaining Files (${viewModel.getRemainingCount()})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(session.sourceFiles) { file ->
                            FileCard(file)
                        }
                    }
                    
                    // Moved files
                    if (session?.movedFiles?.isNotEmpty() == true) {
                        item {
                            Text(
                                text = "Moved Files (${viewModel.getMovedCount()})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        items(session.movedFiles) { file ->
                            FileCard(file, isMoved = true)
                        }
                    }
                    
                    // Complete button when no files remaining
                    if (session?.sourceFiles?.isEmpty() == true && session.movedFiles.isNotEmpty()) {
                        item {
                            Button(
                                onClick = { viewModel.completeSegregation() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOffline) Color(0xFFFF9800) else Color(0xFF4CAF50)
                                )
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isOffline) "Queue for Sync" else "Complete Segregation")
                            }
                        }
                    }
                }
                
                // Completed state
                if (uiState is SegregationUiState.SegregationCompleted) {
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
                                    text = "Segregation Completed",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = "All files have been moved to the target box",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                                
                                Button(
                                    onClick = { viewModel.resetSegregation() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                ) {
                                    Text("Done", color = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }
                
                // Assigned Segregations (when not in flow)
                if (currentSession == null) {
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
                                        onComplete = { viewModel.completeAssignedSegregation(segregation.id) }
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
    }
}

@Composable
private fun CounterItem(label: String, value: Int, color: Color) {
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
private fun FileCard(file: FileRecord, isMoved: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMoved) MaterialTheme.colorScheme.primaryContainer else CardDefaults.cardColors().containerColor
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Barcode: ${file.barcode}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Box: ${file.boxBarcode}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (isMoved) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Moved", tint = MaterialTheme.colorScheme.primary)
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
