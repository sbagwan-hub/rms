package com.tionix.rms.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.profile.domain.usecase.GetDailyStatsUseCase
import com.tionix.rms.feature.profile.domain.usecase.GetPendingSyncCountUseCase
import com.tionix.rms.feature.profile.domain.usecase.GetProfileUseCase
import com.tionix.rms.feature.profile.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getDailyStatsUseCase: GetDailyStatsUseCase,
    private val getPendingSyncCountUseCase: GetPendingSyncCountUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _profile = MutableStateFlow<com.tionix.rms.feature.profile.domain.model.UserProfile?>(null)
    val profile: StateFlow<com.tionix.rms.feature.profile.domain.model.UserProfile?> = _profile.asStateFlow()

    private val _dailyStats = MutableStateFlow<com.tionix.rms.feature.profile.domain.model.DailyStats?>(null)
    val dailyStats: StateFlow<com.tionix.rms.feature.profile.domain.model.DailyStats?> = _dailyStats.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    init {
        loadProfile()
        loadDailyStats()
        loadPendingSyncCount()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = getProfileUseCase()
            
            if (result.isSuccess) {
                _profile.value = result.getOrNull()
                _uiState.value = ProfileUiState.Success
            } else {
                _uiState.value = ProfileUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load profile")
            }
        }
    }

    fun loadDailyStats() {
        viewModelScope.launch {
            val result = getDailyStatsUseCase()
            if (result.isSuccess) {
                _dailyStats.value = result.getOrNull()
            }
        }
    }

    fun loadPendingSyncCount() {
        viewModelScope.launch {
            val result = getPendingSyncCountUseCase()
            if (result.isSuccess) {
                _pendingSyncCount.value = result.getOrNull() ?: 0
            }
        }
    }

    fun logout(): Result<Unit> {
        return runCatching {
            viewModelScope.launch {
                val result = logoutUseCase()
                if (result.isSuccess) {
                    _uiState.value = ProfileUiState.LoggedOut
                } else {
                    _uiState.value = ProfileUiState.Error(result.exceptionOrNull()?.message ?: "Logout failed")
                }
            }
            Result.success(Unit)
        }
    }
}
