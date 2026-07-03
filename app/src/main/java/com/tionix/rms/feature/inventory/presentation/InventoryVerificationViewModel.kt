package com.tionix.rms.feature.inventory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.inventory.domain.model.StartVerificationRequest
import com.tionix.rms.feature.inventory.domain.repository.InventoryVerificationRepository
import com.tionix.rms.feature.inventory.domain.usecase.StartVerificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryVerificationViewModel @Inject constructor(
    private val repository: InventoryVerificationRepository,
    private val startVerificationUseCase: StartVerificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InventoryVerificationUiState>(InventoryVerificationUiState.Loading)
    val uiState: StateFlow<InventoryVerificationUiState> = _uiState.asStateFlow()

    private val _selectedLocationId = MutableStateFlow("")
    val selectedLocationId: StateFlow<String> = _selectedLocationId.asStateFlow()

    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()

    init {
        loadAssignedVerifications()
    }

    fun loadAssignedVerifications() {
        viewModelScope.launch {
            _uiState.value = InventoryVerificationUiState.Loading
            val result = repository.getAssignedVerifications()
            
            if (result.isSuccess) {
                _uiState.value = InventoryVerificationUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = InventoryVerificationUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load verifications"
                )
            }
        }
    }

    fun onLocationIdChanged(value: String) {
        _selectedLocationId.value = value
    }

    fun onScannedBarcodeChanged(value: String) {
        _scannedBarcode.value = value
    }

    fun startVerification() {
        viewModelScope.launch {
            if (_selectedLocationId.value.isBlank()) {
                _uiState.value = InventoryVerificationUiState.Error("Location is required")
                return@launch
            }
            
            val result = startVerificationUseCase(
                StartVerificationRequest(locationId = _selectedLocationId.value)
            )
            
            if (result.isSuccess) {
                _uiState.value = InventoryVerificationUiState.VerificationStarted
                loadAssignedVerifications()
            } else {
                _uiState.value = InventoryVerificationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start verification")
            }
        }
    }

    fun scanBox(verificationId: String) {
        viewModelScope.launch {
            val result = repository.scanBox(_scannedBarcode.value, verificationId)
            if (result.isSuccess) {
                _uiState.value = InventoryVerificationUiState.BoxScanned
            } else {
                _uiState.value = InventoryVerificationUiState.Error(result.exceptionOrNull()?.message ?: "Scan failed")
            }
        }
    }

    fun completeVerification(verificationId: String) {
        viewModelScope.launch {
            val result = repository.completeVerification(verificationId)
            if (result.isSuccess) {
                _uiState.value = InventoryVerificationUiState.VerificationCompleted
                loadAssignedVerifications()
            } else {
                _uiState.value = InventoryVerificationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to complete verification")
            }
        }
    }
}
