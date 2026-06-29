package com.tionix.rms.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import com.tionix.rms.ui.theme.ErrorColor
import com.tionix.rms.ui.theme.PrimaryColor
import com.tionix.rms.ui.theme.SuccessColor
import com.tionix.rms.ui.theme.WarningColor

object MockData {
    val stats = listOf(
        StatItem("Total Records", "14,203", Icons.Default.Inventory, PrimaryColor),
        StatItem("Pending Sync", "24", Icons.Default.Warning, WarningColor),
        StatItem("Dispatched", "852", Icons.Default.LocalShipping, SuccessColor),
        StatItem("Errors", "3", Icons.Default.Assignment, ErrorColor)
    )

    val pendingTasks = listOf(
        TaskItem("T-1001", "Verify Shipment #892", TaskPriority.High, "Today, 10:30 AM"),
        TaskItem("T-1002", "Inventory Count Aisle 4", TaskPriority.Medium, "Today, 1:00 PM"),
        TaskItem("T-1003", "Restock Returns", TaskPriority.Low, "Tomorrow")
    )

    val recentActivity = listOf(
        ActivityItem("Barcode scanned: X98234", "10 mins ago", "John D."),
        ActivityItem("Batch Sync Completed", "1 hr ago", "System"),
        ActivityItem("Record #4432 updated", "2 hrs ago", "Alice S.")
    )

    val deviceStatus = DeviceStatus(
        batteryLevel = 84,
        wifiStrength = 3,
        isCharging = false
    )
}
