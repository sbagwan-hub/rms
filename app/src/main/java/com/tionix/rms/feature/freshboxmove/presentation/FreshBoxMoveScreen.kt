package com.tionix.rms.feature.freshboxmove.presentation

import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
    val step by viewModel.step.collectAsStateWithLifecycle()
    val roomBarcode by viewModel.roomBarcode.collectAsStateWithLifecycle()
    val rackBarcode by viewModel.rackBarcode.collectAsStateWithLifecycle()
    val locationBarcode by viewModel.locationBarcode.collectAsStateWithLifecycle()
    val boxBarcode by viewModel.boxBarcode.collectAsStateWithLifecycle()
    val lockLocation by viewModel.lockLocation.collectAsStateWithLifecycle()

    val scans by viewModel.scansList.collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }

    // Retrieve ScannerManager via Hilt EntryPoint
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

    LaunchedEffect(uiState) {
        if (uiState is FreshBoxMoveUiState.Error) {
            Toast.makeText(context, (uiState as FreshBoxMoveUiState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fresh Box Intake", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Scan & Register Fresh Boxes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
                    // SESSION NOT STARTED HERO CARD VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MoveToInbox,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "No Active Intake Session",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Start a session to scan location hierarchy and register fresh boxes into warehouse locations.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                PrimaryButton(
                                    text = "Start Intake Session",
                                    onClick = {
                                        val deviceId = Settings.Secure.getString(
                                            context.contentResolver,
                                            Settings.Secure.ANDROID_ID
                                        )
                                        viewModel.startSession(deviceId)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                )
                            }
                        }
                    }
                }
                else -> {
                    // ACTIVE SCANNING SESSION VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        FreshBoxStepHeader(currentStep = step)

                        when (step) {
                            FreshBoxMoveViewModel.STEP_ROOM, FreshBoxMoveViewModel.STEP_RACK -> {
                                HierarchyScanCard(
                                    title = if (step == FreshBoxMoveViewModel.STEP_ROOM) "Scan Room" else "Scan Rack",
                                    hint = if (step == FreshBoxMoveViewModel.STEP_ROOM) {
                                        "Scan room barcode label, or skip if unavailable."
                                    } else {
                                        "Scan rack barcode label, or skip if unavailable."
                                    },
                                    scannedValue = if (step == FreshBoxMoveViewModel.STEP_ROOM) roomBarcode else rackBarcode,
                                    onSkip = { viewModel.skipStep() },
                                    onManualEntry = { viewModel.handleBarcodeScan(it) },
                                    onCameraScan = {
                                        scannerManager.startCameraScan(context) { barcode ->
                                            viewModel.handleBarcodeScan(barcode)
                                        }
                                    }
                                )
                            }
                            FreshBoxMoveViewModel.STEP_LOCATION -> {
                                HierarchyScanCard(
                                    title = "Scan Location",
                                    hint = "Scan the target warehouse location barcode.",
                                    scannedValue = locationBarcode.takeIf { it.isNotBlank() },
                                    onSkip = null,
                                    onManualEntry = { viewModel.handleBarcodeScan(it) },
                                    onCameraScan = {
                                        scannerManager.startCameraScan(context) { barcode ->
                                            viewModel.handleBarcodeScan(barcode)
                                        }
                                    }
                                )
                                roomBarcode?.let { BarcodeChip(label = "Room", value = it) }
                                rackBarcode?.let { BarcodeChip(label = "Rack", value = it) }
                            }
                            FreshBoxMoveViewModel.STEP_BOXES -> {
                                LocationSummaryCard(
                                    roomBarcode = roomBarcode,
                                    rackBarcode = rackBarcode,
                                    locationBarcode = locationBarcode,
                                    lockLocation = lockLocation,
                                    onLockLocationChanged = viewModel::onLockLocationChanged,
                                    onChangeLocation = viewModel::resetLocation
                                )
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
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (step) {
                                            FreshBoxMoveViewModel.STEP_ROOM -> "Awaiting room barcode scan"
                                            FreshBoxMoveViewModel.STEP_RACK -> "Awaiting rack barcode scan"
                                            FreshBoxMoveViewModel.STEP_LOCATION -> "Awaiting location barcode scan"
                                            else -> "No boxes scanned yet in this session"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                                .height(48.dp)
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
private fun FreshBoxStepHeader(currentStep: Int) {
    val steps = listOf("Room", "Rack", "Location", "Boxes")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            steps.forEachIndexed { index, label ->
                val active = index == currentStep
                val complete = index < currentStep
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        active -> MaterialTheme.colorScheme.primaryContainer
                        complete -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            active -> MaterialTheme.colorScheme.onPrimaryContainer
                            complete -> MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HierarchyScanCard(
    title: String,
    hint: String,
    scannedValue: String?,
    onSkip: (() -> Unit)?,
    onManualEntry: (String) -> Unit,
    onCameraScan: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (onSkip != null) {
                    TextButton(onClick = onSkip) { Text("Skip") }
                }
            }
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (scannedValue.isNullOrBlank()) {
                Button(
                    onClick = onCameraScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Trigger Scanner")
                }
            } else {
                BarcodeChip(label = title, value = scannedValue)
            }
        }
    }
}

@Composable
private fun LocationSummaryCard(
    roomBarcode: String?,
    rackBarcode: String?,
    locationBarcode: String,
    lockLocation: Boolean,
    onLockLocationChanged: (Boolean) -> Unit,
    onChangeLocation: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Target Warehouse Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = onChangeLocation) {
                    Text("Change", color = MaterialTheme.colorScheme.error)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                roomBarcode?.let { BarcodeChip(label = "Room", value = it) }
                rackBarcode?.let { BarcodeChip(label = "Rack", value = it) }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = locationBarcode,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lock location for all boxes", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = lockLocation, onCheckedChange = onLockLocationChanged)
            }
        }
    }
}

@Composable
private fun BarcodeChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    ) {
        Text(
            text = "$label: $value",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun ScanRowItem(scan: FreshBoxScanEntity) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = if (scan.isSynced) Color(0xFF16A34A).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (scan.isSynced) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = if (scan.isSynced) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (scan.isSynced) "Synced" else "Pending",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (scan.isSynced) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                    )
                }
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
