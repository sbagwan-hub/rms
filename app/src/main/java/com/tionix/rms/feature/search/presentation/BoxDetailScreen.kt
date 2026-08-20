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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxDetailScreen(
    boxId: String,
    onBack: () -> Unit,
    onNavigateToTransfer: (String) -> Unit = {},
    onNavigateToRefile: (String) -> Unit = {},
    onOnFileClick: (String) -> Unit = {},
    canTransfer: Boolean = false, // Role-gated: SUPERVISOR/WAREHOUSE_MANAGER
    canRefile: Boolean = false, // Role-gated: OPERATOR+
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val boxDetail by viewModel.boxDetail.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.refreshError.collect { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
        }
    }

    var showInsertDialog by remember { mutableStateOf(false) }
    var inputBarcode by remember { mutableStateOf("") }
    var insertStatusMessage by remember { mutableStateOf<String?>(null) }
    var isInserting by remember { mutableStateOf(false) }

    LaunchedEffect(boxId) {
        viewModel.getBoxDetail(boxId)
    }

    LaunchedEffect(showInsertDialog) {
        if (showInsertDialog) {
            viewModel.startScanner()
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
            if (showInsertDialog) viewModel.stopScanner()
            viewModel.scannerRepository.disableScanner()
            viewModel.clearBoxDetail()
        }
    }

    val scope = rememberCoroutineScope()

    if (showInsertDialog) {
        val isDuplicateSameBox = inputBarcode.isNotBlank() && boxDetail?.contents?.any { it.barcode.equals(inputBarcode.trim(), ignoreCase = true) } == true
        val isBoxFull = (boxDetail?.availableSlots ?: 1) <= 0

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
                        text = "Point Honeywell scanner at File barcode (e.g. MAC5832458) or tap Activate Scanner:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            viewModel.startScanner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (inputBarcode.isBlank()) "Activate Scanner" else "Scan Again")
                    }

                    if (isDuplicateSameBox) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚠ File already exists in this box", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(Modifier.height(4.dp))
                                Text("${inputBarcode.trim().uppercase()} is already inside Box ${boxDetail?.barcode ?: boxId}.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    } else if (isBoxFull) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("⚠ Box Full", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(Modifier.height(4.dp))
                                Text("Box ${boxDetail?.barcode ?: boxId} has reached its maximum capacity of ${boxDetail?.capacity ?: 25} files. ${if (inputBarcode.isNotBlank()) "File ${inputBarcode.trim().uppercase()} cannot be inserted." else ""}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }

                    if (inputBarcode.isNotBlank() && !isDuplicateSameBox) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Scanned File Barcode:", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    inputBarcode,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Text(
                                    "Target Box: ${boxDetail?.barcode ?: boxId}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
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
                        if (cleanCode.isNotBlank() && !isDuplicateSameBox && !isBoxFull) {
                            isInserting = true
                            insertStatusMessage = null
                            viewModel.insertFile(
                                boxId = boxId,
                                fileBarcode = cleanCode,
                                title = null
                            ) { success, msg ->
                                isInserting = false
                                if (success) {
                                    val targetBoxLabel = boxDetail?.barcode ?: boxId
                                    val successToastMsg = "✓ File inserted successfully\n$cleanCode has been inserted into Box $targetBoxLabel"
                                    android.widget.Toast.makeText(context, successToastMsg, android.widget.Toast.LENGTH_LONG).show()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "✓ File inserted successfully: $cleanCode → $targetBoxLabel",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    showInsertDialog = false
                                    inputBarcode = ""
                                    insertStatusMessage = null
                                } else {
                                    insertStatusMessage = msg
                                }
                            }
                        }
                    },
                    enabled = inputBarcode.isNotBlank() && !isInserting && !isDuplicateSameBox && !isBoxFull
                ) {
                    if (isInserting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            when {
                                isDuplicateSameBox -> "Already Added"
                                isBoxFull -> "Box Full"
                                else -> "Confirm Insert"
                            }
                        )
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Box Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    com.tionix.rms.ui.components.RMSRefreshIconButton(
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.getBoxDetail(boxId, isRefresh = true) }
                    )
                }
            )
        }
    ) { paddingValues ->
        val currentDetail = boxDetail
        if (currentDetail != null) {
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
                                    text = currentDetail.barcode,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                StatusBadge(currentDetail.status)
                            }
                            
                            if (currentDetail.name != null) {
                                Text(
                                    text = currentDetail.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Divider()
                            
                            DetailItem("Box Type", currentDetail.boxType)
                            DetailItem("Status", if (currentDetail.availableSlots <= 0) "FULL" else currentDetail.status.name)
                            DetailItem("Warehouse", currentDetail.warehouse)
                            DetailItem("Site", currentDetail.site)
                            DetailItem("Location", currentDetail.location)
                            DetailItem("Files", "${currentDetail.fileCount} / ${currentDetail.capacity}")
                            DetailItem("Available Slots", currentDetail.availableSlots.toString())
                            if (currentDetail.lastActivity != null) {
                                DetailItem("Last Activity", currentDetail.lastActivity)
                            }
                            if (currentDetail.clientName != null) {
                                DetailItem("Client", currentDetail.clientName)
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
                            onClick = { onNavigateToTransfer(currentDetail.id) }
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
                            onClick = { onNavigateToRefile(currentDetail.id) }
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
                        text = "Contents (${currentDetail.contents.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (currentDetail.contents.isEmpty()) {
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
                    items(currentDetail.contents) { file ->
                        FileCard(file = file, onClick = { onOnFileClick(file.barcode) })
                    }
                }
            }
        } else {
            when (val state = uiState) {
                is SearchUiState.Loading, SearchUiState.Idle -> {
                    LoadingState()
                }
                is SearchUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Unable to load Box details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Button(
                                    onClick = { viewModel.getBoxDetail(boxId) }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                else -> {
                    LoadingState()
                }
            }
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
private fun FileCard(
    file: com.tionix.rms.feature.search.domain.model.FileRecord,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
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

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
