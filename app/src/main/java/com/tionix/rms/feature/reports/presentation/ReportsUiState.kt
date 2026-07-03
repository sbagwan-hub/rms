package com.tionix.rms.feature.reports.presentation

import com.tionix.rms.feature.reports.domain.model.ActivityHistory
import com.tionix.rms.feature.reports.domain.model.Report

sealed class ReportsUiState {
    object Loading : ReportsUiState()
    data class Success(
        val reports: List<Report>,
        val activityHistory: List<ActivityHistory>
    ) : ReportsUiState()
    data class Error(val message: String) : ReportsUiState()
    object ReportDownloaded : ReportsUiState()
}
