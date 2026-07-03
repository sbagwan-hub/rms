package com.tionix.rms.feature.reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.reports.domain.model.ReportType
import com.tionix.rms.feature.reports.domain.usecase.DownloadReportUseCase
import com.tionix.rms.feature.reports.domain.usecase.GetActivityHistoryUseCase
import com.tionix.rms.feature.reports.domain.usecase.GetReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getReportsUseCase: GetReportsUseCase,
    private val getActivityHistoryUseCase: GetActivityHistoryUseCase,
    private val downloadReportUseCase: DownloadReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _selectedReportType = MutableStateFlow<ReportType?>(null)
    val selectedReportType: StateFlow<ReportType?> = _selectedReportType.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = ReportsUiState.Loading
            
            val reportsResult = getReportsUseCase(_selectedReportType.value)
            val historyResult = getActivityHistoryUseCase()
            
            if (reportsResult.isSuccess && historyResult.isSuccess) {
                _uiState.value = ReportsUiState.Success(
                    reports = reportsResult.getOrNull() ?: emptyList(),
                    activityHistory = historyResult.getOrNull() ?: emptyList()
                )
            } else {
                _uiState.value = ReportsUiState.Error(
                    reportsResult.exceptionOrNull()?.message ?: historyResult.exceptionOrNull()?.message ?: "Failed to load data"
                )
            }
        }
    }

    fun onReportTypeChanged(type: ReportType?) {
        _selectedReportType.value = type
        loadData()
    }

    fun downloadReport(reportId: String) {
        viewModelScope.launch {
            val result = downloadReportUseCase(reportId)
            if (result.isSuccess) {
                _uiState.value = ReportsUiState.ReportDownloaded
            } else {
                _uiState.value = ReportsUiState.Error(result.exceptionOrNull()?.message ?: "Download failed")
            }
        }
    }
}
