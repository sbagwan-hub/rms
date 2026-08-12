package com.tionix.rms.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.profile.domain.usecase.GetPendingSyncCountUseCase
import com.tionix.rms.feature.profile.domain.usecase.GetProfileUseCase
import com.tionix.rms.feature.profile.domain.usecase.LogoutUseCase
import com.tionix.rms.feature.sync.data.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getPendingSyncCountUseCase: GetPendingSyncCountUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _profile = MutableStateFlow<com.tionix.rms.feature.profile.domain.model.UserProfile?>(null)
    val profile: StateFlow<com.tionix.rms.feature.profile.domain.model.UserProfile?> = _profile.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val profileResult = getProfileUseCase()
            val pendingResult = getPendingSyncCountUseCase()
            if (profileResult.isSuccess) {
                _profile.value = profileResult.getOrNull()
                _pendingSyncCount.value = pendingResult.getOrNull() ?: 0
                _uiState.value = ProfileUiState.Success
            } else {
                _uiState.value = ProfileUiState.Error(
                    profileResult.exceptionOrNull()?.message ?: "Failed to load profile"
                )
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
