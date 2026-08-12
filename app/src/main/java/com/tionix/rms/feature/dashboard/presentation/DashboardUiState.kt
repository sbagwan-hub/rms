package com.tionix.rms.feature.dashboard.presentation

import com.tionix.rms.feature.dashboard.domain.model.DashboardStats
import com.tionix.rms.feature.dashboard.domain.model.ReportsSummary
import com.tionix.rms.feature.dashboard.domain.model.Task

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val stats: DashboardStats,
        val tasks: List<Task>,
        val reportsSummary: ReportsSummary? = null,
        val canViewReports: Boolean = false
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
