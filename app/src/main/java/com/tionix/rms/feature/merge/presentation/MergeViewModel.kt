package com.tionix.rms.feature.merge.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.core.scanner.domain.usecase.InitializeScannerUseCase
import com.tionix.rms.core.scanner.domain.usecase.StartScanningUseCase
import com.tionix.rms.core.scanner.domain.usecase.StopScanningUseCase
import com.tionix.rms.feature.merge.domain.model.Box
import com.tionix.rms.feature.merge.domain.model.MergeSession
import com.tionix.rms.feature.merge.domain.model.SessionStatus
import com.tionix.rms.feature.merge.domain.repository.MergeRepository
import com.tionix.rms.feature.merge.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MergeViewModel @Inject constructor(
    private val repository: MergeRepository,
    private val startMergeSessionUseCase: StartMergeSessionUseCase,
    private val scanDestinationBoxUseCase: ScanDestinationBoxUseCase,
    private val scanSourceBoxUseCase: ScanSourceBoxUseCase,
    private val removeSourceBoxUseCase: RemoveSourceBoxUseCase,
    private val submitMergeUseCase: SubmitMergeUseCase,
    val scannerRepository: ScannerRepository,
    private val initializeScannerUseCase: InitializeScannerUseCase,
    private val startScanningUseCase: StartScanningUseCase,
    private val stopScanningUseCase: StopScanningUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MergeUiState>(MergeUiState.Loading)
    val uiState: StateFlow<MergeUiState> = _uiState.asStateFlow()

    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()

    private val _currentSession = MutableStateFlow<MergeSession?>(null)
    val currentSession: StateFlow<MergeSession?> = _currentSession.asStateFlow()

    private val _destinationBox = MutableStateFlow<Box?>(null)
    val destinationBox: StateFlow<Box?> = _destinationBox.asStateFlow()

    private val _duplicateError = MutableStateFlow<String?>(null)
    val duplicateError: StateFlow<String?> = _duplicateError.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    init {
        loadAssignedMerges()
        
        // Collect scanner results for continuous scanning
        viewModelScope.launch {
            scannerRepository.scanResults.collect { result ->
                handleScannerResult(result.barcode)
            }
        }
    }

    private fun handleScannerResult(barcode: String) {
        val session = _currentSession.value ?: return
        when (session.status) {
            SessionStatus.SCANNING_DESTINATION -> {
                scanDestinationBox(barcode)
            }
            SessionStatus.SCANNING_SOURCES -> {
                scanSourceBox(barcode)
            }
            else -> {}
        }
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

    fun onScannedBarcodeChanged(value: String) {
        _scannedBarcode.value = value
    }

    fun startMerge() {
        viewModelScope.launch {
            val result = startMergeSessionUseCase()
            if (result.isSuccess) {
                _currentSession.value = result.getOrNull()
                _uiState.value = MergeUiState.SessionStarted
                // Start scanner for continuous scanning
                initializeScannerUseCase()
                startScanningUseCase()
            } else {
                _uiState.value = MergeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start session")
            }
        }
    }

    fun scanDestinationBox(barcode: String) {
        viewModelScope.launch {
            val result = scanDestinationBoxUseCase(barcode)
            if (result.isSuccess) {
                _destinationBox.value = result.getOrNull()
                _currentSession.value = _currentSession.value?.copy(
                    destinationBox = result.getOrNull()!!,
                    status = SessionStatus.SCANNING_SOURCES
                )
                _scannedBarcode.value = ""
            } else {
                _uiState.value = MergeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to scan destination box")
            }
        }
    }

    fun scanSourceBox(barcode: String) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            
            // Guard: destination cannot also be scanned as source
            if (session.destinationBox.barcode == barcode) {
                _duplicateError.value = "Destination box cannot also be scanned as source"
                _uiState.value = MergeUiState.DuplicateError("Destination box cannot also be scanned as source")
                return@launch
            }
            
            // Guard: prevent duplicate source boxes
            val alreadyScanned = session.sourceBoxes.any { it.barcode == barcode }
            if (alreadyScanned) {
                _duplicateError.value = "This box has already been scanned as a source"
                _uiState.value = MergeUiState.DuplicateError("This box has already been scanned as a source")
                return@launch
            }
            
            val result = scanSourceBoxUseCase(session.sessionId, barcode)
            if (result.isSuccess) {
                _currentSession.value = session.copy(
                    sourceBoxes = session.sourceBoxes + result.getOrNull()!!
                )
                _scannedBarcode.value = ""
                _duplicateError.value = null
            } else {
                _uiState.value = MergeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to scan source box")
            }
        }
    }

    fun removeSourceBox(boxBarcode: String) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            val result = removeSourceBoxUseCase(session.sessionId, boxBarcode)
            if (result.isSuccess) {
                _currentSession.value = session.copy(
                    sourceBoxes = session.sourceBoxes.filter { it.barcode != boxBarcode }
                )
            } else {
                _uiState.value = MergeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to remove source box")
            }
        }
    }

    fun clearDuplicateError() {
        _duplicateError.value = null
    }

    fun moveToConfirmation() {
        _currentSession.value = _currentSession.value?.copy(
            status = SessionStatus.CONFIRMING
        )
    }

    fun submitMerge() {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            
            val result = if (_isOffline.value) {
                // Show queued state if offline
                Result.success(Unit)
            } else {
                submitMergeUseCase(session.sessionId)
            }
            
            if (result.isSuccess) {
                _uiState.value = MergeUiState.MergeCompleted
                stopScanningUseCase()
                resetMerge()
            } else {
                _uiState.value = MergeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to submit merge")
            }
        }
    }

    fun completeMerge(mergeId: String) {
        viewModelScope.launch {
            _uiState.value = MergeUiState.Loading
            val result = repository.completeMerge(mergeId)
            if (result.isSuccess) {
                loadAssignedMerges()
            } else {
                _uiState.value = MergeUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to complete merge"
                )
            }
        }
    }

    fun resetMerge() {
        _currentSession.value = null
        _destinationBox.value = null
        _scannedBarcode.value = ""
        _duplicateError.value = null
        viewModelScope.launch { stopScanningUseCase() }
        loadAssignedMerges()
    }

    fun getTotalFileCount(): Int {
        val session = _currentSession.value ?: return 0
        val destinationCount = session.destinationBox.fileCount
        val sourceCount = session.sourceBoxes.sumOf { it.fileCount }
        return destinationCount + sourceCount
    }

    fun getDestinationCapacity(): Int? {
        return _destinationBox.value?.capacity
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { stopScanningUseCase() }
    }
}
