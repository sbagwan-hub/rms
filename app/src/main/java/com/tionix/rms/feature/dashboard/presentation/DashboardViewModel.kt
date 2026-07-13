package com.tionix.rms.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.auth.domain.usecase.LogoutUseCase
import com.tionix.rms.feature.dashboard.domain.usecase.GetAssignedTasksUseCase
import com.tionix.rms.feature.dashboard.domain.usecase.GetDashboardStatsUseCase
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
class DashboardViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val getAssignedTasksUseCase: GetAssignedTasksUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _loggedOut = MutableSharedFlow<Unit>()
    val loggedOut: SharedFlow<Unit> = _loggedOut.asSharedFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            val statsResult = getDashboardStatsUseCase()
            val tasksResult = getAssignedTasksUseCase()
            
            if (statsResult.isSuccess && tasksResult.isSuccess) {
                _uiState.value = DashboardUiState.Success(
                    stats = statsResult.getOrNull()!!,
                    tasks = tasksResult.getOrNull() ?: emptyList()
                )
            } else {
                _uiState.value = DashboardUiState.Error(
                    statsResult.exceptionOrNull()?.message ?: tasksResult.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _loggedOut.emit(Unit)
        }
    }
}
