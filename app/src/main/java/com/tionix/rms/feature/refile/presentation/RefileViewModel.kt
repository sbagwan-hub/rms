package com.tionix.rms.feature.refile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.core.scanner.domain.usecase.InitializeScannerUseCase
import com.tionix.rms.core.scanner.domain.usecase.StartScanningUseCase
import com.tionix.rms.core.scanner.domain.usecase.StopScanningUseCase
import com.tionix.rms.feature.refile.domain.model.FileRecord
import com.tionix.rms.feature.refile.domain.model.RefileAction
import com.tionix.rms.feature.refile.domain.model.RefileActionStatus
import com.tionix.rms.feature.refile.domain.model.RefileSession
import com.tionix.rms.feature.refile.domain.repository.RefileRepository
import com.tionix.rms.feature.refile.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefileViewModel @Inject constructor(
    private val repository: RefileRepository,
    private val scanFileUseCase: ScanFileUseCase,
    private val getHomeLocationUseCase: GetHomeLocationUseCase,
    private val confirmRefileUseCase: ConfirmRefileUseCase,
    private val overrideMismatchUseCase: OverrideMismatchUseCase,
    private val startSessionUseCase: StartSessionUseCase,
    private val endSessionUseCase: EndSessionUseCase,
    private val undoLastActionUseCase: UndoLastActionUseCase,
    private val scannerRepository: ScannerRepository,
    private val initializeScannerUseCase: InitializeScannerUseCase,
    private val startScanningUseCase: StartScanningUseCase,
    private val stopScanningUseCase: StopScanningUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RefileUiState>(RefileUiState.Loading)
    val uiState: StateFlow<RefileUiState> = _uiState.asStateFlow()

    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()

    private val _currentFile = MutableStateFlow<FileRecord?>(null)
    val currentFile: StateFlow<FileRecord?> = _currentFile.asStateFlow()

    private val _destinationBoxBarcode = MutableStateFlow("")
    val destinationBoxBarcode: StateFlow<String> = _destinationBoxBarcode.asStateFlow()

    private val _overrideReason = MutableStateFlow("")
    val overrideReason: StateFlow<String> = _overrideReason.asStateFlow()

    private val _currentSession = MutableStateFlow<RefileSession?>(null)
    val currentSession: StateFlow<RefileSession?> = _currentSession.asStateFlow()

    private val _sessionActions = MutableStateFlow<List<RefileAction>>(emptyList())
    val sessionActions: StateFlow<List<RefileAction>> = _sessionActions.asStateFlow()

    private val _showMismatchDialog = MutableStateFlow(false)
    val showMismatchDialog: StateFlow<Boolean> = _showMismatchDialog.asStateFlow()

    private val _showSessionSummary = MutableStateFlow(false)
    val showSessionSummary: StateFlow<Boolean> = _showSessionSummary.asStateFlow()

    private val _batchMode = MutableStateFlow(false)
    val batchMode: StateFlow<Boolean> = _batchMode.asStateFlow()

    private var scanStep = ScanStep.FILE // Track which step we're scanning in batch mode

    init {
        loadAssignedRefiles()
        
        // Collect scanner results for continuous scanning in batch mode
        viewModelScope.launch {
            scannerRepository.scanResults.collect { result ->
                if (_batchMode.value) {
                    handleScannerResult(result.barcode)
                }
            }
        }
    }

    private enum class ScanStep {
        FILE, DESTINATION_BOX
    }

    private fun handleScannerResult(barcode: String) {
        when (scanStep) {
            ScanStep.FILE -> {
                _scannedBarcode.value = barcode
                scanFile()
            }
            ScanStep.DESTINATION_BOX -> {
                _destinationBoxBarcode.value = barcode
                confirmRefile()
            }
        }
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

    fun onDestinationBoxBarcodeChanged(value: String) {
        _destinationBoxBarcode.value = value
    }

    fun onOverrideReasonChanged(value: String) {
        _overrideReason.value = value
    }

    fun toggleBatchMode() {
        _batchMode.value = !_batchMode.value
        if (_batchMode.value) {
            if (_currentSession.value == null) {
                startNewSession()
            }
            // Initialize and start scanner for continuous scanning
            viewModelScope.launch {
                initializeScannerUseCase()
                startScanningUseCase()
            }
            scanStep = ScanStep.FILE
        } else {
            // Stop scanner when exiting batch mode
            viewModelScope.launch {
                stopScanningUseCase()
            }
            if (_currentSession.value != null) {
                endSession()
            }
        }
    }

    private fun startNewSession() {
        viewModelScope.launch {
            val result = startSessionUseCase()
            if (result.isSuccess) {
                _currentSession.value = result.getOrNull()
                _sessionActions.value = emptyList()
            }
        }
    }

    fun scanFile() {
        viewModelScope.launch {
            val result = scanFileUseCase(_scannedBarcode.value)
            if (result.isSuccess) {
                _currentFile.value = result.getOrNull()
                _uiState.value = RefileUiState.FileScanned(result.getOrNull())
                
                // In batch mode, move to destination box scanning step
                if (_batchMode.value) {
                    scanStep = ScanStep.DESTINATION_BOX
                }
            } else {
                _uiState.value = RefileUiState.Error(result.exceptionOrNull()?.message ?: "Scan failed")
            }
        }
    }

    fun confirmRefile() {
        viewModelScope.launch {
            val fileBarcode = _currentFile.value?.barcode ?: return@launch
            val destinationBoxBarcode = _destinationBoxBarcode.value
            
            if (destinationBoxBarcode.isBlank()) {
                _uiState.value = RefileUiState.Error("Destination box barcode is required")
                return@launch
            }

            val result = confirmRefileUseCase(fileBarcode, destinationBoxBarcode)
            if (result.isSuccess) {
                val action = result.getOrNull()!!
                _sessionActions.value = _sessionActions.value + action
                
                if (_batchMode.value) {
                    // Clear for next scan in batch mode and reset to file scanning step
                    _currentFile.value = null
                    _scannedBarcode.value = ""
                    _destinationBoxBarcode.value = ""
                    scanStep = ScanStep.FILE
                } else {
                    _uiState.value = RefileUiState.RefileCompleted
                }
            } else {
                _showMismatchDialog.value = true
                _uiState.value = RefileUiState.Error(result.exceptionOrNull()?.message ?: "Refile failed")
            }
        }
    }

    fun overrideMismatch() {
        viewModelScope.launch {
            val fileBarcode = _currentFile.value?.barcode ?: return@launch
            val destinationBoxBarcode = _destinationBoxBarcode.value
            val reason = _overrideReason.value

            if (reason.isBlank()) {
                _uiState.value = RefileUiState.Error("Override reason is required")
                return@launch
            }

            val result = overrideMismatchUseCase(fileBarcode, destinationBoxBarcode, reason)
            if (result.isSuccess) {
                val action = result.getOrNull()!!
                _sessionActions.value = _sessionActions.value + action
                _showMismatchDialog.value = false
                _overrideReason.value = ""
                
                if (_batchMode.value) {
                    // Clear for next scan in batch mode and reset to file scanning step
                    _currentFile.value = null
                    _scannedBarcode.value = ""
                    _destinationBoxBarcode.value = ""
                    scanStep = ScanStep.FILE
                } else {
                    _uiState.value = RefileUiState.RefileCompleted
                }
            } else {
                _uiState.value = RefileUiState.Error(result.exceptionOrNull()?.message ?: "Override failed")
            }
        }
    }

    fun dismissMismatchDialog() {
        _showMismatchDialog.value = false
        _overrideReason.value = ""
        // In batch mode, reset to file scanning step after dismissing dialog
        if (_batchMode.value) {
            _currentFile.value = null
            _scannedBarcode.value = ""
            _destinationBoxBarcode.value = ""
            scanStep = ScanStep.FILE
        }
    }

    fun showSessionSummary() {
        _showSessionSummary.value = true
    }

    fun dismissSessionSummary() {
        _showSessionSummary.value = false
    }

    fun undoLastAction() {
        viewModelScope.launch {
            val sessionId = _currentSession.value?.sessionId ?: return@launch
            val result = undoLastActionUseCase(sessionId)
            if (result.isSuccess) {
                _sessionActions.value = _sessionActions.value.dropLast(1)
            }
        }
    }

    fun completeRefile(refileId: String) {
        viewModelScope.launch {
            _uiState.value = RefileUiState.Loading
            val result = repository.completeRefile(refileId)
            if (result.isSuccess) {
                loadAssignedRefiles()
            } else {
                _uiState.value = RefileUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to complete refile"
                )
            }
        }
    }

    fun endSession() {
        viewModelScope.launch {
            val sessionId = _currentSession.value?.sessionId ?: return@launch
            val result = endSessionUseCase(sessionId)
            if (result.isSuccess) {
                _currentSession.value = null
                _sessionActions.value = emptyList()
                _batchMode.value = false
                _showSessionSummary.value = false
                
                // Stop scanner when ending session
                stopScanningUseCase()
                scanStep = ScanStep.FILE
                
                loadAssignedRefiles()
            }
        }
    }

    fun getSessionSummary(): RefileSummary {
        val actions = _sessionActions.value
        return RefileSummary(
            totalRefiled = actions.size,
            successful = actions.count { it.status == RefileActionStatus.CONFIRMED },
            overridden = actions.count { it.status == RefileActionStatus.OVERRIDDEN },
            rejected = actions.count { it.status == RefileActionStatus.REJECTED_MISMATCH }
        )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { stopScanningUseCase() }
    }
}

data class RefileSummary(
    val totalRefiled: Int,
    val successful: Int,
    val overridden: Int,
    val rejected: Int
)
