package com.tionix.rms.feature.transfer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.transfer.domain.model.StartTransferRequest
import com.tionix.rms.feature.transfer.domain.repository.TransferRepository
import com.tionix.rms.feature.transfer.domain.usecase.StartTransferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val repository: TransferRepository,
    private val startTransferUseCase: StartTransferUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransferUiState>(TransferUiState.Loading)
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()

    private val _destinationLocation = MutableStateFlow("")
    val destinationLocation: StateFlow<String> = _destinationLocation.asStateFlow()

    private val _reason = MutableStateFlow("")
    val reason: StateFlow<String> = _reason.asStateFlow()

    init {
        loadAssignedTransfers()
    }

    fun loadAssignedTransfers() {
        viewModelScope.launch {
            _uiState.value = TransferUiState.Loading
            val result = repository.getAssignedTransfers()
            
            if (result.isSuccess) {
                _uiState.value = TransferUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = TransferUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load transfers"
                )
            }
        }
    }

    fun onScannedBarcodeChanged(value: String) {
        _scannedBarcode.value = value
    }

    fun onDestinationLocationChanged(value: String) {
        _destinationLocation.value = value
    }

    fun onReasonChanged(value: String) {
        _reason.value = value
    }

    fun scanBox() {
        viewModelScope.launch {
            val result = repository.scanBox(_scannedBarcode.value)
            if (result.isSuccess) {
                _uiState.value = TransferUiState.BoxScanned(result.getOrNull())
            } else {
                _uiState.value = TransferUiState.Error(result.exceptionOrNull()?.message ?: "Scan failed")
            }
        }
    }

    fun startTransfer() {
        viewModelScope.launch {
            if (_scannedBarcode.value.isBlank() || _destinationLocation.value.isBlank()) {
                _uiState.value = TransferUiState.Error("Barcode and destination location are required")
                return@launch
            }
            
            val result = startTransferUseCase(
                StartTransferRequest(
                    boxBarcode = _scannedBarcode.value,
                    destinationLocation = _destinationLocation.value,
                    reason = _reason.value.ifBlank { null }
                )
            )
            
            if (result.isSuccess) {
                _uiState.value = TransferUiState.TransferStarted
                loadAssignedTransfers()
            } else {
                _uiState.value = TransferUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start transfer")
            }
        }
    }

    fun completeTransfer(transferId: String) {
        viewModelScope.launch {
            val result = repository.completeTransfer(transferId)
            if (result.isSuccess) {
                _uiState.value = TransferUiState.TransferCompleted
                loadAssignedTransfers()
            } else {
                _uiState.value = TransferUiState.Error(result.exceptionOrNull()?.message ?: "Failed to complete transfer")
            }
        }
    }
}
