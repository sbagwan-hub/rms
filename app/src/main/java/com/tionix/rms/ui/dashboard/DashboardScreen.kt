package com.tionix.rms.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.tionix.rms.models.DeviceStatus
import com.tionix.rms.models.MockData
import com.tionix.rms.models.TaskPriority
import com.tionix.rms.ui.components.*
import com.tionix.rms.ui.theme.Dimens
import com.tionix.rms.ui.theme.ErrorColor
import com.tionix.rms.ui.theme.WarningColor

import androidx.lifecycle.viewmodel.compose.viewModel
import com.tionix.rms.ui.viewmodels.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    scannedCode: String? = null,
    onOpenScanner: () -> Unit
) {
    var currentRoute by remember { mutableStateOf(bottomNavItems.first().route) }
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            RmsTopAppBar(
                title = "Home",
                actions = {
                    IconButton(onClick = { /* Handle Notifications */ }, modifier = Modifier.size(Dimens.touchTargetMin)) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        },
        bottomBar = {
            RmsBottomNavigation(
                currentRoute = currentRoute,
                onItemSelected = { currentRoute = it }
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isTablet = maxWidth > 600.dp

            if (isTablet) {
                // Tablet layout: Two columns
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.spacingLarge),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingLarge)
                ) {
                    // Left Column
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = Dimens.spacingMedium)
                    ) {
                        item {
                            WelcomeSection(userName = "Alice Smith")
                            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                        }
                        item {
                            SearchAndScan(
                                searchQuery = searchQuery, 
                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                onScanClick = onOpenScanner
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                        }
                        if (scannedCode != null) {
                            item {
                                DashboardCard(title = "Last Scanned Code") {
                                    Text(
                                        text = scannedCode!!,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(Dimens.spacingSmall)
                                    )
                                }
                                Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                            }
                        }
                        item {
                            BarcodeScanCard(onScanClick = onOpenScanner)
                            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                        }
                        item {
                            SystemInfoSection(deviceStatus = MockData.deviceStatus)
                        }
                    }

                    // Right Column
                    LazyColumn(
                        modifier = Modifier.weight(1.5f),
                        contentPadding = PaddingValues(vertical = Dimens.spacingMedium)
                    ) {
                        item {
                            StatsSection(isTablet = true)
                            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                        }
                        item {
                            TasksSection()
                            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                        }
                        item {
                            ActivitySection()
                            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                        }
                    }
                }
            } else {
                // Handheld layout: Single column
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.spacingMedium),
                    contentPadding = PaddingValues(vertical = Dimens.spacingMedium)
                ) {
                    item {
                        WelcomeSection(userName = "Alice Smith")
                        Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                    }
                    item {
                        SearchAndScan(
                            searchQuery = searchQuery, 
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onScanClick = onOpenScanner
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                    }
                    if (scannedCode != null) {
                        item {
                            DashboardCard(title = "Last Scanned Code") {
                                Text(
                                    text = scannedCode!!,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(Dimens.spacingSmall)
                                )
                            }
                            Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                        }
                    }
                    item {
                        BarcodeScanCard(onScanClick = onOpenScanner)
                        Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                    }
                    item {
                        StatsSection(isTablet = false)
                        Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                    }
                    item {
                        TasksSection()
                        Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                    }
                    item {
                        ActivitySection()
                        Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                    }
                    item {
                        SystemInfoSection(deviceStatus = MockData.deviceStatus)
                        Spacer(modifier = Modifier.height(Dimens.spacingLarge))
                    }
                }
            }
            
        }
    }
}

@Composable
fun SearchAndScan(searchQuery: String, onQueryChange: (String) -> Unit, onScanClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RmsSearchBar(
            query = searchQuery,
            onQueryChange = onQueryChange,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(Dimens.spacingSmall))
        FilledIconButton(
            onClick = onScanClick,
            modifier = Modifier
                .size(56.dp)
                .defaultMinSize(minWidth = Dimens.touchTargetMin, minHeight = Dimens.touchTargetMin),
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan")
        }
    }
}

@Composable
fun StatsSection(isTablet: Boolean) {
    DashboardCard(title = "Today's Statistics") {
        val height = if (isTablet) 120.dp else 200.dp
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            modifier = Modifier.height(height),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            userScrollEnabled = false
        ) {
            items(MockData.stats) { stat ->
                StatisticsCard(stat = stat)
            }
        }
    }
}

@Composable
fun TasksSection() {
    DashboardCard(
        title = "Pending Tasks",
        actionIcon = Icons.Default.ListAlt,
        onActionClick = { /* View All */ }
    ) {
        Column {
            MockData.pendingTasks.forEach { task ->
                RmsListItem(
                    title = task.title,
                    subtitle = "${task.id} • ${task.date}",
                    trailingContent = {
                        val priorityColor = when(task.priority) {
                            TaskPriority.High -> ErrorColor
                            TaskPriority.Medium -> WarningColor
                            TaskPriority.Low -> MaterialTheme.colorScheme.primary
                        }
                        Badge(containerColor = priorityColor) {
                            Text(task.priority.name)
                        }
                    },
                    onClick = { /* Handle Task Click */ }
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@Composable
fun ActivitySection() {
    DashboardCard(
        title = "Recent Activity",
        actionIcon = Icons.Default.History,
        onActionClick = { /* View Activity */ }
    ) {
        Column {
            MockData.recentActivity.forEach { activity ->
                RmsListItem(
                    title = activity.title,
                    subtitle = "${activity.timestamp} • ${activity.user}",
                    onClick = { /* Handle Activity Click */ }
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}


@Composable
fun WelcomeSection(userName: String) {
    Column {
        Text(
            text = "Good Morning,",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SystemInfoSection(deviceStatus: DeviceStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevationSubtle)
    ) {
        Column(modifier = Modifier.padding(Dimens.spacingLarge)) {
            Text(
                text = "System Status",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacingMedium))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatusBadge(
                    icon = Icons.Default.CloudDone,
                    label = "Synced",
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(
                    icon = Icons.Default.BatteryFull,
                    label = "${deviceStatus.batteryLevel}%",
                    color = if (deviceStatus.batteryLevel > 20) MaterialTheme.colorScheme.primary else ErrorColor
                )
                StatusBadge(
                    icon = Icons.Default.Wifi,
                    label = "Strong",
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(
                    icon = Icons.Default.PersonOutline,
                    label = "Active",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun StatusBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
