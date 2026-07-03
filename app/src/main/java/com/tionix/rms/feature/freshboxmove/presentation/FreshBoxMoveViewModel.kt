package com.tionix.rms.feature.freshboxmove.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.freshboxmove.domain.model.StartMoveRequest
import com.tionix.rms.feature.freshboxmove.domain.usecase.ScanBoxUseCase
import com.tionix.rms.feature.freshboxmove.domain.usecase.StartMoveUseCase
import com.tionix.rms.feature.freshboxmove.domain.repository.FreshBoxMoveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FreshBoxMoveViewModel @Inject constructor(
    private val repository: FreshBoxMoveRepository,
    private val startMoveUseCase: StartMoveUseCase,
    private val scanBoxUseCase: ScanBoxUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FreshBoxMoveUiState>(FreshBoxMoveUiState.Loading)
    val uiState: StateFlow<FreshBoxMoveUiState> = _uiState.asStateFlow()

    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()

    private val _destinationLocation = MutableStateFlow("")
    val destinationLocation: StateFlow<String> = _destinationLocation.asStateFlow()

    init {
        loadAssignedMoves()
    }

    fun loadAssignedMoves() {
        viewModelScope.launch {
            _uiState.value = FreshBoxMoveUiState.Loading
            val result = repository.getAssignedMoves()
            
            if (result.isSuccess) {
                _uiState.value = FreshBoxMoveUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                _uiState.value = FreshBoxMoveUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to load moves"
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

    fun scanBox() {
        viewModelScope.launch {
            val result = scanBoxUseCase(_scannedBarcode.value)
            if (result.isSuccess) {
                _uiState.value = FreshBoxMoveUiState.BoxScanned(result.getOrNull())
            } else {
                _uiState.value = FreshBoxMoveUiState.Error(result.exceptionOrNull()?.message ?: "Scan failed")
            }
        }
    }

    fun startMove() {
        viewModelScope.launch {
            if (_scannedBarcode.value.isBlank() || _destinationLocation.value.isBlank()) {
                _uiState.value = FreshBoxMoveUiState.Error("Barcode and destination location are required")
                return@launch
            }
            
            val result = startMoveUseCase(
                StartMoveRequest(
                    boxBarcode = _scannedBarcode.value,
                    destinationLocation = _destinationLocation.value
                )
            )
            
            if (result.isSuccess) {
                _uiState.value = FreshBoxMoveUiState.MoveStarted
                loadAssignedMoves()
            } else {
                _uiState.value = FreshBoxMoveUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start move")
            }
        }
    }

    fun completeMove(moveId: String) {
        viewModelScope.launch {
            val result = repository.completeMove(moveId)
            if (result.isSuccess) {
                _uiState.value = FreshBoxMoveUiState.MoveCompleted
                loadAssignedMoves()
            } else {
                _uiState.value = FreshBoxMoveUiState.Error(result.exceptionOrNull()?.message ?: "Failed to complete move")
            }
        }
    }
}
