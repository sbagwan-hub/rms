package com.tionix.rms.ui.models

import androidx.compose.ui.graphics.vector.ImageVector

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

data class Statistic(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val trend: String? = null
)

data class RecentActivity(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val icon: ImageVector,
    val status: ActivityStatus = ActivityStatus.INFO
)

enum class ActivityStatus {
    SUCCESS, WARNING, ERROR, INFO
}

data class DeviceStatus(
    val batteryLevel: Int,
    val isOnline: Boolean,
    val syncStatus: String
)

data class UserProfile(
    val name: String,
    val role: String,
    val location: String
)
