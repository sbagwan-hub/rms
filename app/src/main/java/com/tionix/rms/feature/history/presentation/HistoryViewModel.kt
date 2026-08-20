package com.tionix.rms.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.history.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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

    fun manualSync() {
        viewModelScope.launch {
            historyRepository.triggerManualSync()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = historyRepository.getSyncedOperations()
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        syncedOps = result.getOrNull().orEmpty(),
                        syncedError = null
                    )
                } else {
                    it
                }
            }
            _isRefreshing.value = false
        }
    }
}
