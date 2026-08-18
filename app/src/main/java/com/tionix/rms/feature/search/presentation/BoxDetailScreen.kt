package com.tionix.rms.feature.search.presentation

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
import com.tionix.rms.feature.search.domain.model.BoxStatus
import com.tionix.rms.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxDetailScreen(
    boxId: String,
    onBack: () -> Unit,
    onNavigateToTransfer: (String) -> Unit = {},
    onNavigateToRefile: (String) -> Unit = {},
    canTransfer: Boolean = false, // Role-gated: SUPERVISOR/WAREHOUSE_MANAGER
    canRefile: Boolean = false, // Role-gated: OPERATOR+
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val boxDetail by viewModel.boxDetail.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showInsertDialog by remember { mutableStateOf(false) }
    var inputBarcode by remember { mutableStateOf("") }
    var insertStatusMessage by remember { mutableStateOf<String?>(null) }
    var isInserting by remember { mutableStateOf(false) }

    LaunchedEffect(boxId) {
        viewModel.getBoxDetail(boxId)
    }

    LaunchedEffect(showInsertDialog) {
        if (showInsertDialog) {
            viewModel.scannerRepository.scanResults.collect { scanResult ->
                if (scanResult.barcode.isNotBlank()) {
                    inputBarcode = scanResult.barcode.trim().uppercase()
                }
            }
        }
    }

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
            viewModel.clearBoxDetail()
        }
    }

    if (showInsertDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isInserting) {
                    showInsertDialog = false
                    inputBarcode = ""
                    insertStatusMessage = null
                }
            },
            title = { Text("Insert File into Box") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Point Honeywell scanner at File barcode (e.g. MAC5832438) or tap Scan below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { viewModel.scannerRepository.startCameraScan(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Scan File")
                    }

                    if (inputBarcode.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Scanned File:", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    inputBarcode,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (insertStatusMessage != null) {
                        Text(
                            text = insertStatusMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanCode = inputBarcode.trim().uppercase()
                        if (cleanCode.isNotBlank()) {
                            isInserting = true
                            insertStatusMessage = null
                            viewModel.insertFile(
                                boxId = boxId,
                                fileBarcode = cleanCode,
                                title = null
                            ) { success, msg ->
                                isInserting = false
                                if (success) {
                                    showInsertDialog = false
                                    inputBarcode = ""
                                    insertStatusMessage = null
                                } else {
                                    insertStatusMessage = msg
                                }
                            }
                        }
                    },
                    enabled = inputBarcode.isNotBlank() && !isInserting
                ) {
                    if (isInserting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Confirm Insert")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showInsertDialog = false
                        inputBarcode = ""
                        insertStatusMessage = null
                    },
                    enabled = !isInserting
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Box Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is SearchUiState.Loading -> {
                LoadingState()
            }
            is SearchUiState.BoxDetailLoaded -> {
                boxDetail?.let { detail ->
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
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = detail.barcode,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        StatusBadge(detail.status)
                                    }
                                    
                                    if (detail.name != null) {
                                        Text(
                                            text = detail.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Divider()
                                    
                                    DetailItem("Location", detail.location)
                                    DetailItem("File Count", detail.fileCount.toString())
                                    if (detail.lastActivity != null) {
                                        DetailItem("Last Activity", detail.lastActivity)
                                    }
                                    if (detail.clientName != null) {
                                        DetailItem("Client", detail.clientName)
                                    }
                                }
                            }
                        }
                        
                        // Quick actions
                        item {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Insert File Action (always available)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { showInsertDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Icon(
                                            Icons.Default.NoteAdd,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Insert File",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Scan or enter file barcode to attach to this box",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (canTransfer) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onNavigateToTransfer(detail.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Icon(
                                                Icons.Default.SwapHoriz,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Transfer Box",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Move this box to another location",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (canRefile) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onNavigateToRefile(detail.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Icon(
                                                Icons.Default.FileUpload,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Refile Files",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Refile files from this box",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Contents list
                        item {
                            Text(
                                text = "Contents (${detail.contents.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (detail.contents.isEmpty()) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No files in this box",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(detail.contents) { file ->
                                FileCard(file)
                            }
                        }
                    }
                }
            }
            is SearchUiState.Error -> {
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
            else -> {}
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
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
private fun StatusBadge(status: BoxStatus) {
    val (color, label) = when (status) {
        BoxStatus.ACTIVE -> Color(0xFF4CAF50) to "Active"
        BoxStatus.IN_TRANSIT -> Color(0xFFFF9800) to "In Transit"
        BoxStatus.LOCKED -> MaterialTheme.colorScheme.error to "Locked"
        BoxStatus.ARCHIVED -> MaterialTheme.colorScheme.tertiary to "Archived"
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

@Composable
private fun FileCard(file: com.tionix.rms.feature.search.domain.model.FileRecord) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.barcode,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = file.title,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Box: ${file.boxBarcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
