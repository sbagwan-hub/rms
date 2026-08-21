package com.tionix.rms.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.auth.domain.repository.AuthRepository
import com.tionix.rms.feature.profile.domain.usecase.GetPendingSyncCountUseCase
import com.tionix.rms.feature.profile.domain.usecase.GetProfileUseCase
import com.tionix.rms.feature.profile.domain.usecase.LogoutUseCase
import com.tionix.rms.feature.sync.data.SyncScheduler
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
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getPendingSyncCountUseCase: GetPendingSyncCountUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val syncScheduler: SyncScheduler,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _profile = MutableStateFlow<com.tionix.rms.feature.profile.domain.model.UserProfile?>(null)
    val profile: StateFlow<com.tionix.rms.feature.profile.domain.model.UserProfile?> = _profile.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    private val _switchMessage = MutableStateFlow<String?>(null)
    val switchMessage: StateFlow<String?> = _switchMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val refreshError: kotlinx.coroutines.flow.SharedFlow<String> = _refreshError.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            if (_profile.value != null) {
                if (_isRefreshing.value) return@launch
                _isRefreshing.value = true
                android.util.Log.d("APPBAR_REFRESH", "Screen: Profile\nAPI request started")
            } else {
                _uiState.value = ProfileUiState.Loading
            }

            val profileResult = getProfileUseCase()
            val pendingResult = getPendingSyncCountUseCase()
            if (profileResult.isSuccess) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: Profile\nAPI response: 200\nState updated")
                _profile.value = profileResult.getOrNull()
                _pendingSyncCount.value = pendingResult.getOrNull() ?: 0
                _uiState.value = ProfileUiState.Success
            } else {
                if (_profile.value == null) {
                    _uiState.value = ProfileUiState.Error(
                        profileResult.exceptionOrNull()?.message ?: "Failed to load profile"
                    )
                } else {
                    _refreshError.emit("Unable to refresh data. Please try again.")
                }
            }
            _isRefreshing.value = false
            if (_profile.value != null) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: Profile\nRefresh completed")
            }
        }
    }

    fun switchWarehouse(warehouseId: String) {
        viewModelScope.launch {
            _switchMessage.value = null
            val result = authRepository.switchWarehouse(warehouseId)
            if (result is com.tionix.rms.feature.auth.domain.model.AuthResult.Success) {
                refresh()
            } else if (result is com.tionix.rms.feature.auth.domain.model.AuthResult.Error) {
                _switchMessage.value = result.message
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = ProfileUiState.LoggedOut
            onComplete()
        }
    }

    fun syncAndLogout(onComplete: () -> Unit) {
        viewModelScope.launch {
            syncScheduler.scheduleImmediateSync()
            logoutUseCase()
            _uiState.value = ProfileUiState.LoggedOut
            onComplete()
        }
    }
}
