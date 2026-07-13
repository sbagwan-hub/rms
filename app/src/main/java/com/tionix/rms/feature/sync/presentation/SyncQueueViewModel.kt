package com.tionix.rms.feature.sync.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.sync.domain.model.PendingSyncQueue
import com.tionix.rms.feature.sync.domain.usecase.DeleteAllFailedItemsUseCase
import com.tionix.rms.feature.sync.domain.usecase.DeleteFailedItemUseCase
import com.tionix.rms.feature.sync.domain.usecase.GetPendingSyncQueueUseCase
import com.tionix.rms.feature.sync.domain.usecase.RetryAllFailedItemsUseCase
import com.tionix.rms.feature.sync.domain.usecase.RetrySyncItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncQueueViewModel @Inject constructor(
    private val getPendingSyncQueueUseCase: GetPendingSyncQueueUseCase,
    private val retrySyncItemUseCase: RetrySyncItemUseCase,
    private val retryAllFailedItemsUseCase: RetryAllFailedItemsUseCase,
    private val deleteFailedItemUseCase: DeleteFailedItemUseCase,
    private val deleteAllFailedItemsUseCase: DeleteAllFailedItemsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SyncQueueUiState>(SyncQueueUiState.Loading)
    val uiState: StateFlow<SyncQueueUiState> = _uiState.asStateFlow()

    private val _syncQueue = MutableStateFlow<PendingSyncQueue?>(null)
    val syncQueue: StateFlow<PendingSyncQueue?> = _syncQueue.asStateFlow()

    init {
        observeSyncQueue()
    }

    fun loadSyncQueue() {
        viewModelScope.launch {
            _uiState.value = SyncQueueUiState.Loading
            val result = getPendingSyncQueueUseCase()
            
            if (result.isSuccess) {
                _syncQueue.value = result.getOrNull()
                _uiState.value = SyncQueueUiState.Success
            } else {
                _uiState.value = SyncQueueUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load sync queue")
            }
        }
    }

    private fun observeSyncQueue() {
        viewModelScope.launch {
            getPendingSyncQueueUseCase.observePendingSyncQueue().collect { queue ->
                _syncQueue.value = queue
            }
        }
    }

    fun retrySyncItem(itemId: String) {
        viewModelScope.launch {
            val result = retrySyncItemUseCase(itemId)
            if (result.isFailure) {
                _uiState.value = SyncQueueUiState.Error(result.exceptionOrNull()?.message ?: "Failed to retry sync item")
            }
            loadSyncQueue()
        }
    }

    fun retryAllFailedItems() {
        viewModelScope.launch {
            val result = retryAllFailedItemsUseCase()
            if (result.isFailure) {
                _uiState.value = SyncQueueUiState.Error(result.exceptionOrNull()?.message ?: "Failed to retry all items")
            }
            loadSyncQueue()
        }
    }

    fun deleteFailedItem(itemId: String) {
        viewModelScope.launch {
            val result = deleteFailedItemUseCase(itemId)
            if (result.isFailure) {
                _uiState.value = SyncQueueUiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete item")
            }
            loadSyncQueue()
        }
    }

    fun deleteAllFailedItems() {
        viewModelScope.launch {
            val result = deleteAllFailedItemsUseCase()
            if (result.isFailure) {
                _uiState.value = SyncQueueUiState.Error(result.exceptionOrNull()?.message ?: "Failed to delete all items")
            }
            loadSyncQueue()
        }
    }
}

sealed class SyncQueueUiState {
    object Loading : SyncQueueUiState()
    object Success : SyncQueueUiState()
    data class Error(val message: String) : SyncQueueUiState()
}
