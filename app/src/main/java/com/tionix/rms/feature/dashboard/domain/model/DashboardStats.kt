package com.tionix.rms.feature.dashboard.domain.model

data class DashboardStats(
    val totalTasks: Int,
    val pendingTasks: Int,
    val inProgressTasks: Int,
    val completedTasks: Int,
    val urgentTasks: Int,
    val boxesProcessedToday: Int,
    val filesScannedToday: Int
)
