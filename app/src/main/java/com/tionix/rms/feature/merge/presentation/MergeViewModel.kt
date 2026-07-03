package com.tionix.rms.feature.merge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.merge.domain.model.StartMergeRequest
import com.tionix.rms.feature.merge.domain.repository.MergeRepository
import com.tionix.rms.feature.merge.domain.usecase.StartMergeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MergeViewModel @Inject constructor(
    private val repository: MergeRepository,
    private val startMergeUseCase: StartMergeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MergeUiState>(MergeUiState.Loading)
    val uiState: StateFlow<MergeUiState> = _uiState.asStateFlow()

    private val _sourceBoxBarcode = MutableStateFlow("")
    val sourceBoxBarcode: StateFlow<String> = _sourceBoxBarcode.asStateFlow()

    private val _destinationBoxBarcode = MutableStateFlow("")
    val destinationBoxBarcode: StateFlow<String> = _destinationBoxBarcode.asStateFlow()

    private val _reason = MutableStateFlow("")
    val reason: StateFlow<String> = _reason.asStateFlow()

    init {
        loadAssignedMerges()
    }

    fun loadAssignedMerges() {
        viewModelScope.launch {
            _uiState.value = MergeUiState.Loading
            val result = repository.getAssignedMerges()
            
            if (result.isSuccess) {
                _uiState.value = MergeUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = MergeUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load merges"
                )
            }
        }
    }

    fun onSourceBoxBarcodeChanged(value: String) {
        _sourceBoxBarcode.value = value
    }

    fun onDestinationBoxBarcodeChanged(value: String) {
        _destinationBoxBarcode.value = value
    }

    fun onReasonChanged(value: String) {
        _reason.value = value
    }

    fun scanBox() {
        viewModelScope.launch {
            val result = repository.scanBox(_sourceBoxBarcode.value)
            if (result.isSuccess) {
                _uiState.value = MergeUiState.BoxScanned(result.getOrNull())
            } else {
                _uiState.value = MergeUiState.Error(result.exceptionOrNull()?.message ?: "Scan failed")
            }
        }
    }

    fun startMerge() {
        viewModelScope.launch {
            if (_sourceBoxBarcode.value.isBlank() || _destinationBoxBarcode.value.isBlank()) {
                _uiState.value = MergeUiState.Error("Source and destination box barcodes are required")
                return@launch
            }
            
            val result = startMergeUseCase(
                StartMergeRequest(
                    sourceBoxBarcode = _sourceBoxBarcode.value,
                    destinationBoxBarcode = _destinationBoxBarcode.value,
                    reason = _reason.value.ifBlank { null }
                )
            )
            
            if (result.isSuccess) {
                _uiState.value = MergeUiState.MergeStarted
                loadAssignedMerges()
            } else {
                _uiState.value = MergeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start merge")
            }
        }
    }

    fun completeMerge(mergeId: String) {
        viewModelScope.launch {
            val result = repository.completeMerge(mergeId)
            if (result.isSuccess) {
                _uiState.value = MergeUiState.MergeCompleted
                loadAssignedMerges()
            } else {
                _uiState.value = MergeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to complete merge")
            }
        }
    }
}
