package com.tionix.rms.feature.reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.reports.domain.model.ReportType
import com.tionix.rms.feature.reports.domain.usecase.DownloadReportUseCase
import com.tionix.rms.feature.reports.domain.usecase.GetActivityHistoryUseCase
import com.tionix.rms.feature.reports.domain.usecase.GetReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val refreshError: kotlinx.coroutines.flow.SharedFlow<String> = _refreshError.asSharedFlow()

    init {
        loadData()
    }

    fun loadData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                if (_isRefreshing.value) return@launch
                _isRefreshing.value = true
                android.util.Log.d("APPBAR_REFRESH", "Screen: Reports\nAPI request started")
            } else {
                _uiState.value = ReportsUiState.Loading
            }
            
            val reportsResult = getReportsUseCase(_selectedReportType.value)
            val historyResult = getActivityHistoryUseCase()
            
            if (reportsResult.isSuccess && historyResult.isSuccess) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: Reports\nAPI response: 200\nState updated")
                _uiState.value = ReportsUiState.Success(
                    reports = reportsResult.getOrNull() ?: emptyList(),
                    activityHistory = historyResult.getOrNull() ?: emptyList()
                )
            } else {
                if (isRefresh && _uiState.value is ReportsUiState.Success) {
                    _refreshError.emit("Unable to refresh data. Please try again.")
                } else {
                    _uiState.value = ReportsUiState.Error(
                        reportsResult.exceptionOrNull()?.message ?: historyResult.exceptionOrNull()?.message ?: "Failed to load data"
                    )
                }
            }
            _isRefreshing.value = false
            if (isRefresh) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: Reports\nRefresh completed")
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
