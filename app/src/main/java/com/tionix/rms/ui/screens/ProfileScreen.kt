package com.tionix.rms.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val switchMessage by viewModel.switchMessage.collectAsStateWithLifecycle()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPendingSyncWarning by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "User Profile",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Account & Session Information",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when (uiState) {
            ProfileUiState.Loading -> LoadingState(modifier = Modifier.padding(padding))
            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (uiState as ProfileUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            ProfileUiState.Success -> {
                profile?.let { user ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (switchMessage != null) {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = switchMessage!!,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }

                        // SECTION 1: PROFILE HEADER CARD (Matched to System Info style)
                        item {
                            ProfileSectionHeader(title = "User Details", icon = Icons.Default.Person)
                        }
                        item {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                ),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Avatar Header Banner
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(56.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = user.fullName.split(" ")
                                                        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
                                                        .take(2)
                                                        .joinToString(""),
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = user.fullName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (user.username.contains("@")) user.username else "@${user.username}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = user.roleLabel,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    // Detailed Information Rows
                                    ProfileInfoTile(
                                        icon = Icons.Default.Person,
                                        label = "Employee Name",
                                        value = user.fullName
                                    )

                                    ProfileInfoTile(
                                        icon = Icons.Default.Email,
                                        label = "Email / Username",
                                        value = user.username
                                    )

                                    if (user.id.isNotBlank()) {
                                        ProfileInfoTile(
                                            icon = Icons.Default.Badge,
                                            label = "Employee ID",
                                            value = user.id
                                        )
                                    }

                                    ProfileInfoTile(
                                        icon = Icons.Default.Security,
                                        label = "Role",
                                        value = user.roleLabel
                                    )

                                    if (!user.warehouseName.isNullOrBlank()) {
                                        ProfileInfoTile(
                                            icon = Icons.Default.Warehouse,
                                            label = "Warehouse",
                                            value = user.warehouseName
                                        )
                                    } else if (!user.companyName.isNullOrBlank()) {
                                        ProfileInfoTile(
                                            icon = Icons.Default.Business,
                                            label = "Company",
                                            value = user.companyName
                                        )
                                    }
                                }
                            }
                        }

                        // SECTION 2: ACTIVE SESSION & WAREHOUSE SWITCHER
                        item {
                            ProfileSectionHeader(title = "Active Session", icon = Icons.Default.Business)
                        }
                        item {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                ),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ProfileInfoTile(
                                        icon = Icons.Default.Business,
                                        label = "Company",
                                        value = user.companyName ?: "—"
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ProfileInfoTile(
                                        icon = Icons.Default.Store,
                                        label = "Branch",
                                        value = user.branchName ?: "—"
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ProfileInfoTile(
                                        icon = Icons.Default.Warehouse,
                                        label = "Active Warehouse",
                                        value = user.warehouseName ?: "—"
                                    )

                                    if (user.availableWarehouses.size > 1) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        var expanded by remember { mutableStateOf(false) }
                                        val activeLabel = user.availableWarehouses
                                            .firstOrNull { it.id == user.activeWarehouseId }
                                            ?.name ?: user.warehouseName ?: "Select warehouse"
                                        Text(
                                            "Switch Active Warehouse",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        ExposedDropdownMenuBox(
                                            expanded = expanded,
                                            onExpandedChange = { expanded = !expanded }
                                        ) {
                                            OutlinedTextField(
                                                value = activeLabel,
                                                onValueChange = {},
                                                readOnly = true,
                                                modifier = Modifier
                                                    .menuAnchor()
                                                    .fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                user.availableWarehouses.forEach { warehouse ->
                                                    DropdownMenuItem(
                                                        text = { Text(warehouse.name) },
                                                        onClick = {
                                                            expanded = false
                                                            if (warehouse.id != user.activeWarehouseId) {
                                                                viewModel.switchWarehouse(warehouse.id)
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // SECTION 3: SYSTEM INFO (Exact match to SettingsScreen style)
                        item {
                            ProfileSectionHeader(title = "System Info", icon = Icons.Default.Info)
                        }
                        item {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                ),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    ProfileInfoTile(
                                        icon = Icons.Default.Code,
                                        label = "Build Environment",
                                        value = "DEBUG"
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ProfileInfoTile(
                                        icon = Icons.Default.Smartphone,
                                        label = "Device Hardware",
                                        value = "${user.deviceModel ?: "Honeywell Scanner"} (${user.deviceSerial ?: "EDA52"})"
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ProfileInfoTile(
                                        icon = Icons.Default.Info,
                                        label = "App Version",
                                        value = user.appVersion
                                    )
                                }
                            }
                        }

                        // SECTION 4: ACCOUNT ACTIONS (Sign Out Button)
                        item {
                            Button(
                                onClick = {
                                    if (pendingSyncCount > 0) showPendingSyncWarning = true
                                    else showLogoutDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (pendingSyncCount > 0) "Sign out ($pendingSyncCount pending sync)" else "Sign out",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        item {
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
            ProfileUiState.LoggedOut -> Unit
        }
    }

    // Centered ConfirmDialog for Logout confirmation adhering to workspace rules
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign out?") },
            text = { Text("You will need to sign in again to continue using the application.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout(onLogout)
                }) { Text("Sign out", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
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
                    Text("You have $pendingSyncCount pending sync items. Please sync first or unsaved offline changes may be lost.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPendingSyncWarning = false
                    viewModel.syncAndLogout(onLogout)
                }) { Text("Sync & Sign out") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPendingSyncWarning = false
                    viewModel.logout(onLogout)
                }) { Text("Sign out anyway", color = MaterialTheme.colorScheme.error) }
            }
        )
    }
}

@Composable
private fun ProfileSectionHeader(title: String, icon: ImageVector) {
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
private fun ProfileInfoTile(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(top = 2.dp)
                .size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = true
            )
        }
    }
}
