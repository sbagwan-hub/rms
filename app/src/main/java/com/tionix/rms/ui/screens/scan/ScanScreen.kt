package com.tionix.rms.ui.screens.scan

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.core.network.dto.LookupData
import com.tionix.rms.scanner.CameraScannerView
import com.tionix.rms.scanner.ScannerAvailability

/**
 * SCAN SCREEN — the heart of the Honeywell workflow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // CAMERA FALLBACK — full-screen preview when no Honeywell imager exists.
    if (state.showCamera) {
        CameraScannerView(
            onBarcode = { viewModel.onCameraResult(it) },
            onClose = { viewModel.onCameraResult(null) },
        )
        return
    }

    LaunchedEffect(state.error) {
        state.error?.let { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // BIG SCAN BUTTON — software trigger.
            Button(
                onClick = { if (state.scanning) viewModel.stopBeam() else viewModel.triggerScan() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.scanning) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(top = 8.dp),
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Text(
                    when {
                        state.scanning -> "  SCANNING… point at barcode (tap to cancel)"
                        state.scannerMode == ScannerAvailability.Mode.CAMERA -> "  SCAN (camera)"
                        else -> "  SCAN"
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // Manual entry (fallback for damaged barcodes)
            var manual by remember { mutableStateOf("") }
            OutlinedTextField(
                value = manual,
                onValueChange = { manual = it },
                label = { Text("…or type barcode manually") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.onBarcode(manual)
                    manual = ""
                }),
                modifier = Modifier.fillMaxWidth(),
            )

            // Thin progress bar while lookup + record are in flight
            if (state.loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            // Backend errors: BARCODE_UNKNOWN, FORBIDDEN (other company), etc.
            state.error?.let { errorMsg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Lookup result — entity header + its CONTENTS
            state.result?.let { result ->
                EntityHeader(result, recorded = state.recorded)
                ContentsList(result)
            }
        }
    }
}

/** Card describing WHAT was scanned + WHERE it lives + recording status. */
@Composable
private fun EntityHeader(result: LookupData, recorded: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {

            // Breadcrumb: Company › Branch › WH › Site › Room › Rack › Shelf › Location
            if (result.path.isNotEmpty()) {
                Text(
                    result.path.joinToString(" › "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (result.entityType) {
                    "LOCATION" -> Icons.Filled.PinDrop
                    "BOX" -> Icons.Filled.Inventory2
                    else -> Icons.Filled.Folder
                }
                Icon(icon, contentDescription = result.entityType, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.padding(4.dp))
                Column {
                    Text(
                        result.entity.barcode,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        result.entityType + (result.entity.status?.let { " · $it" } ?: ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Location capacity gauge
            if (result.entityType == "LOCATION" && result.entity.capacity != null) {
                Text(
                    "Occupancy: ${result.entity.occupied ?: 0} / ${result.entity.capacity} boxes",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // "Recorded" = server persisted the scan
            if (recorded) {
                AssistChip(
                    onClick = {},
                    label = { Text("Recorded — visible in Admin Panel") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
            }
        }
    }
}

/** CONTENTS — the "what's inside" answer */
@Composable
private fun ContentsList(result: LookupData) {
    val title = when (result.entityType) {
        "LOCATION" -> "Boxes at this location (${result.contents.size})"
        "BOX" -> "Files in this box (${result.contents.size})"
        else -> null
    }
    title?.let { Text(it, style = MaterialTheme.typography.titleMedium) }

    if (result.contents.isEmpty() && result.entityType != "FILE") {
        Text(
            "Empty",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(result.contents, key = { it.id }) { item ->
            ListItem(
                headlineContent = {
                    Text(item.barcode, fontFamily = FontFamily.Monospace)
                },
                supportingContent = {
                    Text(listOfNotNull(item.label, item.status).joinToString(" · "))
                },
                trailingContent = {
                    item.fileCount?.let { Text("$it files", style = MaterialTheme.typography.labelMedium) }
                },
                leadingContent = {
                    Icon(
                        if (result.entityType == "LOCATION") Icons.Filled.Inventory2 else Icons.Filled.Folder,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}
