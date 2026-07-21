package com.tionix.rms.feature.freshboxmove.presentation

import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxScanEntity
import com.tionix.rms.ui.components.PrimaryButton
import com.tionix.rms.ui.components.RMSTextField
import com.tionix.rms.ui.components.ScannerEffect
import com.tionix.rms.ui.components.SecondaryButton
import com.tionix.rms.ui.theme.Dimens
import com.tionix.rms.utils.scanner.ScannerManager
import dagger.hilt.android.EntryPointAccessors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreshBoxMoveScreen(
    onBack: () -> Unit,
    viewModel: FreshBoxMoveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeSession by viewModel.activeSession.collectAsStateWithLifecycle()
    val locationBarcode by viewModel.locationBarcode.collectAsStateWithLifecycle()
    val boxBarcode by viewModel.boxBarcode.collectAsStateWithLifecycle()
    val lockLocation by viewModel.lockLocation.collectAsStateWithLifecycle()
    
    val scans by viewModel.scansList.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }

    // Retrieve ScannerManager via Hilt EntryPoint (ScannerManager is bound in SingletonComponent)
    val scannerManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            FreshBoxScreenEntryPoint::class.java
        ).scannerManager()
    }

    // Collect scanned barcodes from hardware imager
    ScannerEffect(
        scannerManager = scannerManager,
        continuousMode = true,
        onBarcodeScanned = { barcode ->
            viewModel.handleBarcodeScan(barcode)
        }
    )

    // Handle warning alerts on duplicate scans
    LaunchedEffect(Unit) {
        viewModel.duplicateScanWarning.collect { warningMessage ->
            Toast.makeText(context, warningMessage, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fresh Box Intake", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeSession) {
                null -> {
                    // Session not started UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Active Intake Session",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start a session to begin scanning and registering fresh boxes to warehouse locations.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        PrimaryButton(
                            text = "Start Session",
                            onClick = {
                                val deviceId = Settings.Secure.getString(
                                    context.contentResolver,
                                    Settings.Secure.ANDROID_ID
                                )
                                viewModel.startSession(deviceId)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                        )
                    }
                }
                else -> {
                    // Active scanning UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Location Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Warehouse Location",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (locationBarcode.isNotBlank()) {
                                        TextButton(onClick = viewModel::resetLocation) {
                                            Text("Change Location", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }

                                if (locationBarcode.isBlank()) {
                                    OutlinedTextField(
                                        value = locationBarcode,
                                        onValueChange = { viewModel.handleBarcodeScan(it) },
                                        label = { Text("Scan or Enter Location Barcode") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    scannerManager.startCameraScan(context) { barcode ->
                                                        viewModel.handleBarcodeScan(barcode)
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.QrCodeScanner,
                                                    contentDescription = "Scan Location",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Place,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = locationBarcode,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Keep same location for all boxes",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Switch(
                                        checked = lockLocation,
                                        onCheckedChange = viewModel::onLockLocationChanged
                                    )
                                }
                            }
                        }

                        if (locationBarcode.isNotBlank()) {
                            // Box Scanner Input Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RMSTextField(
                                    value = boxBarcode,
                                    onValueChange = viewModel::onBoxBarcodeChanged,
                                    label = "Scan Box Barcode",
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            scannerManager.startCameraScan(context) { barcode ->
                                                viewModel.onBoxBarcodeChanged(barcode)
                                                viewModel.submitScan(barcode)
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.QrCodeScanner,
                                                contentDescription = "Scan Box"
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 56.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.submitScan(boxBarcode) },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    @Suppress("DEPRECATION")
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = "Submit",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        // Scanned Items List Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scanned Boxes (${scans.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (scans.isNotEmpty()) {
                                TextButton(onClick = { showClearConfirm = true }) {
                                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        // Scanned Boxes List
                        if (scans.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (locationBarcode.isBlank()) "Awaiting Location Scan" else "No boxes scanned yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(scans) { scan ->
                                    ScanRowItem(scan = scan)
                                }
                            }
                        }

                        // Submit Session Control
                        PrimaryButton(
                            text = "Finish Session",
                            onClick = { viewModel.endSession() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                        )
                    }
                }
            }

            if (uiState is FreshBoxMoveUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Scans?") },
            text = { Text("Are you sure you want to discard all scanned boxes in this session? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        activeSession?.let {
                            // In this implementation we will clear the scans locally
                            // (We could invoke a repository method, or viewModel can handle it)
                            showClearConfirm = false
                        }
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ScanRowItem(scan: FreshBoxScanEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Box: ${scan.boxBarcode}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Location: ${scan.locationBarcode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (scan.isSynced) {
                Icon(
                    Icons.Default.CloudDone,
                    contentDescription = "Synced",
                    tint = Color(0xFF4CAF50)
                )
            } else {
                Icon(
                    Icons.Default.CloudQueue,
                    contentDescription = "Pending Sync",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Entry point interface to resolve Hilt ScannerManager inside Composable
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface FreshBoxScreenEntryPoint {
    fun scannerManager(): ScannerManager
}
