package com.tionix.rms.feature.inventory.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.feature.inventory.domain.model.BoxStatus
import com.tionix.rms.feature.inventory.domain.model.VerificationStatus
import com.tionix.rms.ui.components.ScannerEffect
import com.tionix.rms.utils.scanner.ScannerManager

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
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.refreshError.collect { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
        }
    }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (activeVerification != null)
                                "Verify: ${activeVerification.verificationCode}"
                            else
                                "Inventory Verification",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            if (activeVerification != null)
                                "Scan Box & Verify Files"
                            else
                                "Audit & Verification Workflows",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                        com.tionix.rms.ui.components.RMSRefreshIconButton(
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.loadAssignedVerifications(isRefresh = true) }
                        )
                    } else {
                        IconButton(onClick = { viewModel.exitVerification() }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (activeVerification != null) {
            // ACTIVE VERIFICATION SCANNING & PROGRESS SCREEN
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Header Details & Progress Card
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Target Box: ${activeVerification.locationName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

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
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$progress%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        LinearProgressIndicator(
                            progress = if (totalCount > 0) verifiedCount.toFloat() / totalCount.toFloat() else 0f,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                    }
                }

                // 2. Summary Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val stats = listOf(
                        "Verified" to viewModel.getVerifiedCount() to Color(0xFF16A34A),
                        "Missing" to viewModel.getMissingCount() to Color(0xFFDC2626),
                        "Unexpected" to viewModel.getUnexpectedCount() to Color(0xFFF59E0B)
                    )

                    stats.forEach { (labelCount, color) ->
                        val (label, count) = labelCount
                        OutlinedCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
                            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
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
                                    color = color,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // 3. Barcode Scanner Controls
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Scan File Barcode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                scannerManager.startCameraScan(context) { barcode ->
                                    val clean = barcode.trim().uppercase()
                                    viewModel.onScannedBarcodeChanged(clean)
                                    viewModel.verifyBox(clean)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trigger Scanner")
                        }

                        if (scannedBarcode.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Last Scanned: $scannedBarcode",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                // 4. Expected & Scanned Boxes Lists
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (scannedBoxes.isNotEmpty()) {
                        item {
                            VerificationSectionHeader(title = "Scanned Files (${scannedBoxes.size})", icon = Icons.Default.CheckCircle)
                        }
                        items(scannedBoxes) { box ->
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
                                    Surface(
                                        color = if (box.scanStatus.name == "VERIFIED") Color(0xFF16A34A).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = box.scanStatus.name,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (box.scanStatus.name == "VERIFIED") Color(0xFF16A34A) else Color(0xFFF59E0B)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val remaining = expectedBoxes.filter { it.status == BoxStatus.PENDING }
                    if (remaining.isNotEmpty()) {
                        item {
                            VerificationSectionHeader(title = "Remaining Expected Files (${remaining.size})", icon = Icons.Default.Schedule)
                        }
                        items(remaining) { box ->
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
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

                // 5. Complete Verification Action Button
                Button(
                    onClick = { viewModel.prepareForSubmit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Verification", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
            // ASSIGNED VERIFICATIONS LIST VIEW
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start New Verification Form Card
                item {
                    VerificationSectionHeader(title = "New Audit Session", icon = Icons.Default.PlayArrow)
                }
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedLocationId,
                                onValueChange = viewModel::onLocationIdChanged,
                                label = { Text("Target Box Barcode") },
                                leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Button(
                                onClick = { viewModel.startVerification() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Verification")
                            }
                        }
                    }
                }

                // Assigned Verifications Section
                item {
                    VerificationSectionHeader(title = "Assigned Verifications", icon = Icons.Default.FactCheck)
                }

                when (val state = uiState) {
                    is InventoryVerificationUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    is InventoryVerificationUiState.Success -> {
                        if (state.verifications.isEmpty()) {
                            item {
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No assigned verifications",
                                            style = MaterialTheme.typography.bodyMedium,
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
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
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
private fun VerificationSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun VerificationCard(
    verification: com.tionix.rms.feature.inventory.domain.model.InventoryVerification,
    onScanBox: () -> Unit,
    onComplete: () -> Unit
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
                Text(
                    text = verification.verificationCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(verification.status)
            }

            Text(
                text = "Target: ${verification.locationName}",
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
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LinearProgressIndicator(
                progress = if (verification.totalBoxes > 0) verification.verifiedBoxes.toFloat() / verification.totalBoxes.toFloat() else 0f,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            if (verification.status == VerificationStatus.IN_PROGRESS) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onScanBox,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan Box")
                    }
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
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
        VerificationStatus.COMPLETED -> Color(0xFF16A34A) to "Completed"
        VerificationStatus.FAILED -> MaterialTheme.colorScheme.error to "Failed"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

// Entry point interface to resolve Hilt ScannerManager inside Composable
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface InventoryVerificationScreenEntryPoint {
    fun scannerManager(): ScannerManager
}
