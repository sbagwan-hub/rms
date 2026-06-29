package com.tionix.rms.models

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class StatItem(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

data class TaskItem(
    val id: String,
    val title: String,
    val priority: TaskPriority,
    val date: String
)

enum class TaskPriority {
    High, Medium, Low
}

data class ActivityItem(
    val title: String,
    val timestamp: String,
    val user: String
)

data class DeviceStatus(
    val batteryLevel: Int,
    val wifiStrength: Int,
    val isCharging: Boolean
)
