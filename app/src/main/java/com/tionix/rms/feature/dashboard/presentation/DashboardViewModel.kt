package com.tionix.rms.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.sync.data.local.PendingOperationDao
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.auth.domain.usecase.LogoutUseCase
import com.tionix.rms.feature.dashboard.domain.usecase.GetAssignedTasksUseCase
import com.tionix.rms.feature.dashboard.domain.usecase.GetDashboardStatsUseCase
import com.tionix.rms.feature.dashboard.domain.usecase.GetReportsSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val getAssignedTasksUseCase: GetAssignedTasksUseCase,
    private val getReportsSummaryUseCase: GetReportsSummaryUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authPreferences: AuthPreferences,
    pendingOperationDao: PendingOperationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _loggedOut = MutableSharedFlow<Unit>()
    val loggedOut: SharedFlow<Unit> = _loggedOut.asSharedFlow()

    val pendingSyncCount: StateFlow<Int> = pendingOperationDao.observePending()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>()
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

    init {
        loadDashboardData(isRefresh = false)
    }

    fun loadDashboardData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                if (_isRefreshing.value) return@launch
                _isRefreshing.value = true
                android.util.Log.d("APPBAR_REFRESH", "Screen: Dashboard\nAPI request started")
            } else if (_uiState.value !is DashboardUiState.Success) {
                _uiState.value = DashboardUiState.Loading
            }

            val canViewReports = canViewReports()
            val statsResult = getDashboardStatsUseCase()
            val tasksResult = getAssignedTasksUseCase()
            val reportsResult = if (canViewReports) getReportsSummaryUseCase() else null

            if (statsResult.isSuccess) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: Dashboard\nAPI response: 200\nState updated")
                _uiState.value = DashboardUiState.Success(
                    stats = statsResult.getOrNull()!!,
                    tasks = tasksResult.getOrNull() ?: emptyList(),
                    reportsSummary = reportsResult?.getOrNull(),
                    canViewReports = canViewReports
                )
            } else {
                val errorMsg = statsResult.exceptionOrNull()?.message
                    ?: tasksResult.exceptionOrNull()?.message
                    ?: "Failed to load dashboard data"
                if (_uiState.value !is DashboardUiState.Success) {
                    _uiState.value = DashboardUiState.Error(errorMsg)
                } else if (isRefresh) {
                    _refreshError.emit("Unable to refresh data. Please try again.")
                }
            }
            _isRefreshing.value = false
            if (isRefresh) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: Dashboard\nRefresh completed")
            }
        }
    }

    private suspend fun canViewReports(): Boolean {
        if (authPreferences.hasPermission("report:view")) return true
        val role = authPreferences.getRole().orEmpty()
        return role in MANAGER_ROLES
    }

    fun refresh() {
        loadDashboardData(isRefresh = true)
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _loggedOut.emit(Unit)
        }
    }

    companion object {
        private val MANAGER_ROLES = setOf(
            "WAREHOUSE_MANAGER",
            "COMPANY_ADMIN",
            "SUPER_ADMIN"
        )
    }
}
