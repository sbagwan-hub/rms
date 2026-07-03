package com.tionix.rms.core.sync.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.sync.domain.usecase.GetSyncStatusUseCase
import com.tionix.rms.core.sync.domain.usecase.RetryFailedOperationsUseCase
import com.tionix.rms.core.sync.domain.usecase.SyncNowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val getSyncStatusUseCase: GetSyncStatusUseCase,
    private val syncNowUseCase: SyncNowUseCase,
    private val retryFailedOperationsUseCase: RetryFailedOperationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SyncUiState>(SyncUiState.Loading)
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        loadSyncStatus()
    }

    fun loadSyncStatus() {
        viewModelScope.launch {
            _uiState.value = SyncUiState.Loading
            val result = getSyncStatusUseCase()
            
            if (result.isSuccess) {
                _uiState.value = SyncUiState.Success(result.getOrNull()!!)
            } else {
                _uiState.value = SyncUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load sync status")
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = SyncUiState.Syncing
            val result = syncNowUseCase()
            
            if (result.isSuccess) {
                _uiState.value = SyncUiState.SyncCompleted
                loadSyncStatus()
            } else {
                _uiState.value = SyncUiState.Error(result.exceptionOrNull()?.message ?: "Sync failed")
            }
        }
    }

    fun retryFailed() {
        viewModelScope.launch {
            _uiState.value = SyncUiState.Syncing
            val result = retryFailedOperationsUseCase()
            
            if (result.isSuccess) {
                _uiState.value = SyncUiState.SyncCompleted
                loadSyncStatus()
            } else {
                _uiState.value = SyncUiState.Error(result.exceptionOrNull()?.message ?: "Retry failed")
            }
        }
    }
}

sealed class SyncUiState {
    object Loading : SyncUiState()
    data class Success(val status: com.tionix.rms.core.sync.domain.model.SyncStatus) : SyncUiState()
    data class Error(val message: String) : SyncUiState()
    object Syncing : SyncUiState()
    object SyncCompleted : SyncUiState()
}
