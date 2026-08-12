package com.tionix.rms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.feature.profile.presentation.ProfileUiState
import com.tionix.rms.feature.profile.presentation.ProfileViewModel
import com.tionix.rms.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPendingSyncWarning by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            ProfileUiState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text((uiState as ProfileUiState.Error).message)
                }
            }
            ProfileUiState.Success -> {
                profile?.let { user ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = MaterialTheme.shapes.extraLarge,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(72.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = user.fullName.split(" ")
                                                    .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
                                                    .take(2)
                                                    .joinToString(""),
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(user.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                    Text(user.username, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(user.roleLabel, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Assigned warehouses", fontWeight = FontWeight.Bold)
                                    if (user.warehouses.isEmpty()) {
                                        Text("No warehouse assignments", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    } else {
                                        user.warehouses.forEach { Text("• $it") }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    ProfileRow("Device serial", user.deviceSerial ?: "—")
                                    ProfileRow("Device model", user.deviceModel ?: "—")
                                    ProfileRow("App version", user.appVersion)
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    if (pendingSyncCount > 0) showPendingSyncWarning = true
                                    else showLogoutDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Sign out")
                            }
                        }
                    }
                }
            }
            ProfileUiState.LoggedOut -> Unit
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign out?") },
            text = { Text("You will need to sign in again to continue.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout(onLogout)
                }) { Text("Sign out", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showPendingSyncWarning) {
        AlertDialog(
            onDismissRequest = { showPendingSyncWarning = false },
            title = { Text("Pending sync items") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("You have $pendingSyncCount pending sync items. Sync first or they may be lost.")
                    TextButton(onClick = {
                        showPendingSyncWarning = false
                        viewModel.syncAndLogout(onLogout)
                    }) { Text("Sync and logout") }
                    TextButton(onClick = {
                        showPendingSyncWarning = false
                        viewModel.logout(onLogout)
                    }) { Text("Logout anyway", color = MaterialTheme.colorScheme.error) }
                    TextButton(onClick = { showPendingSyncWarning = false }) { Text("Cancel") }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
