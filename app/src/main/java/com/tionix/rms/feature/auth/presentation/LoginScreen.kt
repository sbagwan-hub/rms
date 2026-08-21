package com.tionix.rms.feature.auth.presentation

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tionix.rms.ui.components.PrimaryButton
import com.tionix.rms.ui.components.SecondaryButton
import com.tionix.rms.ui.components.RMSTextField

import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Dns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val usernameError by viewModel.usernameError.collectAsStateWithLifecycle()
    val passwordError by viewModel.passwordError.collectAsStateWithLifecycle()
    val sites by viewModel.sites.collectAsStateWithLifecycle()
    val selectedSite by viewModel.selectedSite.collectAsStateWithLifecycle()
    val siteError by viewModel.siteError.collectAsStateWithLifecycle()
    val sitesLoading by viewModel.sitesLoading.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    var siteDropdownExpanded by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var editServerUrl by remember(serverUrl) { mutableStateOf(serverUrl) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val deviceId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: "unknown"

    // Bind scanner enable/disable to screen lifecycle: ON_RESUME / ON_PAUSE
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.enableScanner()
                Lifecycle.Event.ON_PAUSE -> viewModel.disableScanner()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.disableScanner()
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                android.widget.Toast.makeText(
                    context,
                    (uiState as LoginUiState.Success).message,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                onLoginSuccess()
            }
            is LoginUiState.Error -> {
                android.widget.Toast.makeText(
                    context,
                    (uiState as LoginUiState.Error).message,
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Branding Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Glow logo container
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "RECORDS SYSTEM",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Enterprise Records Management Suite",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Form container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    RMSTextField(
                        value = username,
                        onValueChange = viewModel::onUsernameChanged,
                        label = "Username / Employee ID",
                        isError = usernameError != null,
                        errorMessage = usernameError,
                        modifier = Modifier.heightIn(min = 56.dp)
                    )

                    RMSTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChanged,
                        label = "Password",
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.size(48.dp) // Large target size
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = passwordError != null,
                        errorMessage = passwordError,
                        singleLine = true,
                        modifier = Modifier.heightIn(min = 56.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Site Picker Dropdown ──────────────────────────────────
                    ExposedDropdownMenuBox(
                        expanded = siteDropdownExpanded,
                        onExpandedChange = { siteDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSite?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text(
                                    text = if (sitesLoading) "Loading sites..." else "Select Site",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (sitesLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = siteDropdownExpanded)
                                }
                            },
                            isError = siteError != null,
                            supportingText = if (siteError != null) {
                                { Text(siteError!!, color = MaterialTheme.colorScheme.error) }
                            } else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = siteDropdownExpanded && sites.isNotEmpty(),
                            onDismissRequest = { siteDropdownExpanded = false }
                        ) {
                            if (sites.isEmpty() && !sitesLoading) {
                                DropdownMenuItem(
                                    text = { Text("No sites available", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { siteDropdownExpanded = false }
                                )
                            } else {
                                sites.forEach { site ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = site.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = site.code,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.onSiteSelected(site)
                                            siteDropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Business,
                                                contentDescription = null,
                                                tint = if (selectedSite?.id == site.id)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    // ────────────────────────────────────────────────────────

                    Spacer(modifier = Modifier.height(8.dp))

                    val isFormValid = username.isNotBlank() && password.isNotBlank()
                    PrimaryButton(
                        text = "Login",
                        onClick = { viewModel.login(deviceId) },
                        enabled = isFormValid,
                        isLoading = uiState is LoginUiState.Loading,
                        modifier = Modifier.heightIn(min = 56.dp) // Large target size
                    )

                    val errorState = uiState
                    if (errorState is LoginUiState.Error) {
                        Text(
                            text = errorState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Footer info & Server Config
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Device ID: $deviceId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )

                TextButton(
                    onClick = {
                        editServerUrl = serverUrl
                        showServerConfigDialog = true
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Server Settings",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Server Config",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showServerConfigDialog) {
        AlertDialog(
            onDismissRequest = { showServerConfigDialog = false },
            title = {
                Text("Backend Server URL", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Configure the API base URL for this device (e.g. your Mac IP for physical devices or 10.0.2.2 for emulator):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = editServerUrl,
                        onValueChange = { editServerUrl = it },
                        label = { Text("Server URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        "Quick Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SuggestionChip(
                            onClick = { editServerUrl = "http://192.168.1.7:3002/api/v1/mobile/" },
                            label = { Text("Mac Wi-Fi IP (192.168.1.7:3002)", style = MaterialTheme.typography.bodySmall) }
                        )
                        SuggestionChip(
                            onClick = { editServerUrl = "http://10.0.2.2:3002/api/v1/mobile/" },
                            label = { Text("Android Emulator (10.0.2.2:3002)", style = MaterialTheme.typography.bodySmall) }
                        )
                        SuggestionChip(
                            onClick = { editServerUrl = "http://127.0.0.1:3002/api/v1/mobile/" },
                            label = { Text("USB Reverse (127.0.0.1:3002)", style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateServerUrl(editServerUrl)
                        showServerConfigDialog = false
                    }
                ) {
                    Text("Save & Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerConfigDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
