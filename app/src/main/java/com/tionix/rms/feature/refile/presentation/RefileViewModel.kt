package com.tionix.rms.feature.refile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.refile.domain.model.StartRefileRequest
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import com.tionix.rms.feature.refile.domain.usecase.StartRefileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefileViewModel @Inject constructor(
    private val repository: RefileRepository,
    private val startRefileUseCase: StartRefileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RefileUiState>(RefileUiState.Loading)
    val uiState: StateFlow<RefileUiState> = _uiState.asStateFlow()

    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()

    private val _newLocation = MutableStateFlow("")
    val newLocation: StateFlow<String> = _newLocation.asStateFlow()

    private val _reason = MutableStateFlow("")
    val reason: StateFlow<String> = _reason.asStateFlow()

    init {
        loadAssignedRefiles()
    }

    fun loadAssignedRefiles() {
        viewModelScope.launch {
            _uiState.value = RefileUiState.Loading
            val result = repository.getAssignedRefiles()
            
            if (result.isSuccess) {
                _uiState.value = RefileUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = RefileUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load refiles"
                )
            }
        }
    }

    fun onScannedBarcodeChanged(value: String) {
        _scannedBarcode.value = value
    }

    fun onNewLocationChanged(value: String) {
        _newLocation.value = value
    }

    fun onReasonChanged(value: String) {
        _reason.value = value
    }

    fun scanFile() {
        viewModelScope.launch {
            val result = repository.scanFile(_scannedBarcode.value)
            if (result.isSuccess) {
                _uiState.value = RefileUiState.FileScanned(result.getOrNull())
            } else {
                _uiState.value = RefileUiState.Error(result.exceptionOrNull()?.message ?: "Scan failed")
            }
        }
    }

    fun startRefile() {
        viewModelScope.launch {
            if (_scannedBarcode.value.isBlank() || _newLocation.value.isBlank()) {
                _uiState.value = RefileUiState.Error("Barcode and new location are required")
                return@launch
            }
            
            val result = startRefileUseCase(
                StartRefileRequest(
                    fileBarcode = _scannedBarcode.value,
                    newLocation = _newLocation.value,
                    reason = _reason.value.ifBlank { null }
                )
            )
            
            if (result.isSuccess) {
                _uiState.value = RefileUiState.RefileStarted
                loadAssignedRefiles()
            } else {
                _uiState.value = RefileUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start refile")
            }
        }
    }

    fun completeRefile(refileId: String) {
        viewModelScope.launch {
            val result = repository.completeRefile(refileId)
            if (result.isSuccess) {
                _uiState.value = RefileUiState.RefileCompleted
                loadAssignedRefiles()
            } else {
                _uiState.value = RefileUiState.Error(result.exceptionOrNull()?.message ?: "Failed to complete refile")
            }
        }
    }
}
