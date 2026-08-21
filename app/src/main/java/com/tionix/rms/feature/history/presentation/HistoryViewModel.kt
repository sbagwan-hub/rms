package com.tionix.rms.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.history.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.observePendingOperations().collect { pending ->
                _uiState.update { it.copy(pendingOps = pending) }
            }
        }
        loadSynced()
    }

    fun selectTab(tab: HistoryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == HistoryTab.SYNCED) {
            loadSynced()
        }
    }

    fun loadSynced() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingSynced = true, syncedError = null) }
            val result = historyRepository.getSyncedOperations()
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        loadingSynced = false,
                        syncedOps = result.getOrNull().orEmpty(),
                        syncedError = null
                    )
                } else {
                    it.copy(
                        loadingSynced = false,
                        syncedError = result.exceptionOrNull()?.message ?: "Failed to load synced operations"
                    )
                }
            }
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val refreshError: kotlinx.coroutines.flow.SharedFlow<String> = _refreshError.asSharedFlow()

    fun manualSync() {
        viewModelScope.launch {
            historyRepository.triggerManualSync()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            android.util.Log.d("APPBAR_REFRESH", "Screen: History\nAPI request started")
            val result = historyRepository.getSyncedOperations()
            if (result.isSuccess) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: History\nAPI response: 200\nState updated")
                _uiState.update {
                    it.copy(
                        syncedOps = result.getOrNull().orEmpty(),
                        syncedError = null
                    )
                }
            } else {
                _refreshError.emit("Unable to refresh data. Please try again.")
            }
            _isRefreshing.value = false
            android.util.Log.d("APPBAR_REFRESH", "Screen: History\nRefresh completed")
        }
    }
}
