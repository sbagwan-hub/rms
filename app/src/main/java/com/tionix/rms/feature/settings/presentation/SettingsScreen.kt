package com.tionix.rms.feature.settings.presentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.feature.settings.domain.model.ThemeMode
import com.tionix.rms.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = remember { SnackbarHostState() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = settingsState) {
                is SettingsState.Loading -> {
                    LoadingState()
                }
                is SettingsState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Scanner Settings
                        item {
                            Text(
                                text = "Scanner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SettingToggle(
                                        label = "Continuous Mode",
                                        description = "Keep scanner active for continuous scanning",
                                        checked = state.settings.scannerContinuousMode,
                                        onCheckedChange = { viewModel.updateScannerContinuousMode(it) }
                                    )
                                    
                                    HorizontalDivider()
                                    
                                    SettingToggle(
                                        label = "Scan Beep",
                                        description = "Play sound on successful scan",
                                        checked = state.settings.scannerBeep,
                                        onCheckedChange = { viewModel.updateScannerBeep(it) }
                                    )
                                    
                                    HorizontalDivider()
                                    
                                    SettingToggle(
                                        label = "Haptic Feedback",
                                        description = "Vibrate on successful scan",
                                        checked = state.settings.scannerHaptic,
                                        onCheckedChange = { viewModel.updateScannerHaptic(it) }
                                    )
                                }
                            }
                        }
                        
                        // Sync Settings
                        item {
                            Text(
                                text = "Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SettingToggle(
                                        label = "Auto Sync",
                                        description = "Automatically sync data in background",
                                        checked = state.settings.syncAutoSync,
                                        onCheckedChange = { viewModel.updateSyncAutoSync(it) }
                                    )
                                    
                                    HorizontalDivider()
                                    
                                    SettingToggle(
                                        label = "WiFi Only",
                                        description = "Only sync when connected to WiFi",
                                        checked = state.settings.syncWifiOnly,
                                        onCheckedChange = { viewModel.updateSyncWifiOnly(it) }
                                    )
                                    
                                    HorizontalDivider()
                                    
                                    Button(
                                        onClick = {
                                            viewModel.syncNow()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sync Now")
                                    }
                                }
                            }
                        }
                        
                        // Display Settings
                        item {
                            Text(
                                text = "Display",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Theme Mode",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    ThemeModeSelector(
                                        selectedMode = state.settings.themeMode,
                                        onModeSelected = { viewModel.updateThemeMode(it) }
                                    )
                                }
                            }
                        }
                        
                        // App Info
                        item {
                            Text(
                                text = "About",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    InfoItem("App Version", "1.0.0") // TODO: Get from BuildConfig
                                    InfoItem("Environment", "Production") // TODO: Get from BuildConfig
                                }
                            }
                        }
                    }
                }
                is SettingsState.Error -> {
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
                SettingsState.SyncCompleted -> {
                    viewModel.loadSettings()
                }
            }
        }
    }
}

@Composable
private fun SettingToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeModeOption(
            label = "Follow System",
            mode = ThemeMode.FOLLOW_SYSTEM,
            selectedMode = selectedMode,
            onSelected = onModeSelected
        )
        
        ThemeModeOption(
            label = "Light",
            mode = ThemeMode.LIGHT,
            selectedMode = selectedMode,
            onSelected = onModeSelected
        )
        
        ThemeModeOption(
            label = "Dark",
            mode = ThemeMode.DARK,
            selectedMode = selectedMode,
            onSelected = onModeSelected
        )
    }
}

@Composable
private fun ThemeModeOption(
    label: String,
    mode: ThemeMode,
    selectedMode: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selectedMode == mode,
            onClick = { onSelected(mode) }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
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
