package com.tionix.rms.feature.dashboard.presentation

import com.tionix.rms.feature.dashboard.domain.model.DashboardStats
import com.tionix.rms.feature.dashboard.domain.model.Task

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val stats: DashboardStats,
        val tasks: List<Task>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
