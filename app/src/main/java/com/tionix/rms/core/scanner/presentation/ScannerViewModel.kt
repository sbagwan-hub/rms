package com.tionix.rms.core.scanner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.core.scanner.domain.usecase.CheckScannerAvailabilityUseCase
import com.tionix.rms.core.scanner.domain.usecase.InitializeScannerUseCase
import com.tionix.rms.core.scanner.domain.usecase.StartScanningUseCase
import com.tionix.rms.core.scanner.domain.usecase.StopScanningUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scannerRepository: ScannerRepository,
    private val initializeScannerUseCase: InitializeScannerUseCase,
    private val startScanningUseCase: StartScanningUseCase,
    private val stopScanningUseCase: StopScanningUseCase,
    private val checkScannerAvailabilityUseCase: CheckScannerAvailabilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Idle)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        // Collect scan results
        viewModelScope.launch {
            scannerRepository.scanResults.collect { result ->
                _uiState.value = ScannerUiState.ScanResult(result)
            }
        }
        
        checkAvailability()
    }

    fun initialize() {
        viewModelScope.launch {
            _uiState.value = ScannerUiState.Loading
            val result = initializeScannerUseCase()
            
            if (result.isSuccess) {
                _uiState.value = ScannerUiState.Initialized
            } else {
                _uiState.value = ScannerUiState.Error(result.exceptionOrNull()?.message ?: "Failed to initialize scanner")
            }
        }
    }

    fun startScanning() {
        viewModelScope.launch {
            val result = startScanningUseCase()
            
            if (result.isSuccess) {
                _uiState.value = ScannerUiState.Scanning
            } else {
                _uiState.value = ScannerUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start scanning")
            }
        }
    }

    fun stopScanning() {
        viewModelScope.launch {
            val result = stopScanningUseCase()
            
            if (result.isSuccess) {
                _uiState.value = ScannerUiState.Stopped
            } else {
                _uiState.value = ScannerUiState.Error(result.exceptionOrNull()?.message ?: "Failed to stop scanning")
            }
        }
    }

    private fun checkAvailability() {
        viewModelScope.launch {
            val result = checkScannerAvailabilityUseCase()
            if (result.isSuccess) {
                _uiState.value = ScannerUiState.AvailabilityCheck(result.getOrNull() ?: false)
            }
        }
    }
}

sealed class ScannerUiState {
    object Idle : ScannerUiState()
    object Loading : ScannerUiState()
    object Initialized : ScannerUiState()
    object Scanning : ScannerUiState()
    object Stopped : ScannerUiState()
    data class ScanResult(val result: com.tionix.rms.core.scanner.domain.model.ScanResult) : ScannerUiState()
    data class AvailabilityCheck(val isAvailable: Boolean) : ScannerUiState()
    data class Error(val message: String) : ScannerUiState()
}
