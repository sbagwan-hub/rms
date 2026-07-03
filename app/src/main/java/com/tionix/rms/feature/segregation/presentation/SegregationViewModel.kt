package com.tionix.rms.feature.segregation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.segregation.domain.model.StartSegregationRequest
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import com.tionix.rms.feature.segregation.domain.usecase.StartSegregationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SegregationViewModel @Inject constructor(
    private val repository: SegregationRepository,
    private val startSegregationUseCase: StartSegregationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SegregationUiState>(SegregationUiState.Loading)
    val uiState: StateFlow<SegregationUiState> = _uiState.asStateFlow()

    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()

    private val _reasonCode = MutableStateFlow("")
    val reasonCode: StateFlow<String> = _reasonCode.asStateFlow()

    private val _reason = MutableStateFlow("")
    val reason: StateFlow<String> = _reason.asStateFlow()

    init {
        loadAssignedSegregations()
    }

    fun loadAssignedSegregations() {
        viewModelScope.launch {
            _uiState.value = SegregationUiState.Loading
            val result = repository.getAssignedSegregations()
            
            if (result.isSuccess) {
                _uiState.value = SegregationUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = SegregationUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load segregations"
                )
            }
        }
    }

    fun onScannedBarcodeChanged(value: String) {
        _scannedBarcode.value = value
    }

    fun onReasonCodeChanged(value: String) {
        _reasonCode.value = value
    }

    fun onReasonChanged(value: String) {
        _reason.value = value
    }

    fun scanBox() {
        viewModelScope.launch {
            val result = repository.scanBox(_scannedBarcode.value)
            if (result.isSuccess) {
                _uiState.value = SegregationUiState.BoxScanned(result.getOrNull())
            } else {
                _uiState.value = SegregationUiState.Error(result.exceptionOrNull()?.message ?: "Scan failed")
            }
        }
    }

    fun startSegregation() {
        viewModelScope.launch {
            if (_scannedBarcode.value.isBlank() || _reasonCode.value.isBlank()) {
                _uiState.value = SegregationUiState.Error("Barcode and reason code are required")
                return@launch
            }
            
            val result = startSegregationUseCase(
                StartSegregationRequest(
                    boxBarcode = _scannedBarcode.value,
                    reasonCode = _reasonCode.value,
                    reason = _reason.value.ifBlank { null }
                )
            )
            
            if (result.isSuccess) {
                _uiState.value = SegregationUiState.SegregationStarted
                loadAssignedSegregations()
            } else {
                _uiState.value = SegregationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start segregation")
            }
        }
    }

    fun completeSegregation(segregationId: String) {
        viewModelScope.launch {
            val result = repository.completeSegregation(segregationId)
            if (result.isSuccess) {
                _uiState.value = SegregationUiState.SegregationCompleted
                loadAssignedSegregations()
            } else {
                _uiState.value = SegregationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to complete segregation")
            }
        }
    }
}
