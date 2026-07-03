package com.tionix.rms.feature.dashboard.data.remote.dto

data class DashboardStatsDto(
    val totalTasks: Int,
    val pendingTasks: Int,
    val inProgressTasks: Int,
    val completedTasks: Int,
    val urgentTasks: Int,
    val boxesProcessedToday: Int,
    val filesScannedToday: Int
)
