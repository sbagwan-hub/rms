package com.tionix.rms.feature.segregation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.audio.BeepPlayer
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.core.scanner.domain.usecase.InitializeScannerUseCase
import com.tionix.rms.core.scanner.domain.usecase.StartScanningUseCase
import com.tionix.rms.core.scanner.domain.usecase.StopScanningUseCase
import com.tionix.rms.feature.segregation.domain.model.Box
import com.tionix.rms.feature.segregation.domain.model.FileRecord
import com.tionix.rms.feature.segregation.domain.model.SegregationSession
import com.tionix.rms.feature.segregation.domain.model.SessionStatus
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import com.tionix.rms.feature.segregation.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SegregationViewModel @Inject constructor(
    private val repository: SegregationRepository,
    private val startSegregationSessionUseCase: StartSegregationSessionUseCase,
    private val scanSourceBoxUseCase: ScanSourceBoxUseCase,
    private val scanTargetBoxUseCase: ScanTargetBoxUseCase,
    private val moveFileUseCase: MoveFileUseCase,
    private val completeSegregationSessionUseCase: CompleteSegregationSessionUseCase,
    val scannerRepository: ScannerRepository,
    private val initializeScannerUseCase: InitializeScannerUseCase,
    private val startScanningUseCase: StartScanningUseCase,
    private val stopScanningUseCase: StopScanningUseCase,
    private val beepPlayer: BeepPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow<SegregationUiState>(SegregationUiState.Loading)
    val uiState: StateFlow<SegregationUiState> = _uiState.asStateFlow()

    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()

    private val _currentSession = MutableStateFlow<SegregationSession?>(null)
    val currentSession: StateFlow<SegregationSession?> = _currentSession.asStateFlow()

    private val _sourceBox = MutableStateFlow<Box?>(null)
    val sourceBox: StateFlow<Box?> = _sourceBox.asStateFlow()

    private val _targetBox = MutableStateFlow<Box?>(null)
    val targetBox: StateFlow<Box?> = _targetBox.asStateFlow()

    private val _validationError = MutableStateFlow<FileRecord?>(null)
    val validationError: StateFlow<FileRecord?> = _validationError.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    init {
        loadAssignedSegregations()
        
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
            SessionStatus.SCANNING_SOURCE -> {
                scanSourceBox(barcode)
            }
            SessionStatus.SCANNING_TARGET -> {
                scanTargetBox(barcode)
            }
            SessionStatus.MOVING_FILES -> {
                moveFile(barcode)
            }
            else -> {}
        }
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

    fun startSegregation() {
        viewModelScope.launch {
            val result = startSegregationSessionUseCase()
            if (result.isSuccess) {
                _currentSession.value = result.getOrNull()
                _uiState.value = SegregationUiState.SessionStarted
                // Start scanner for continuous scanning
                initializeScannerUseCase()
                startScanningUseCase()
            } else {
                _uiState.value = SegregationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start session")
            }
        }
    }

    fun scanSourceBox(barcode: String) {
        viewModelScope.launch {
            val result = scanSourceBoxUseCase(barcode)
            if (result.isSuccess) {
                _sourceBox.value = result.getOrNull()
                _currentSession.value = _currentSession.value?.copy(
                    sourceBox = result.getOrNull()!!,
                    status = SessionStatus.SCANNING_TARGET
                )
                _scannedBarcode.value = ""
                beepPlayer.positive()
            } else {
                _uiState.value = SegregationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to scan source box")
                beepPlayer.error()
            }
        }
    }

    fun scanTargetBox(barcode: String) {
        viewModelScope.launch {
            val sourceBarcode = _sourceBox.value?.barcode
            if (barcode.trim() == sourceBarcode?.trim()) {
                _uiState.value = SegregationUiState.ValidationError("Target box cannot be the same as source box")
                beepPlayer.error()
                return@launch
            }

            val result = scanTargetBoxUseCase(barcode)
            if (result.isSuccess) {
                _targetBox.value = result.getOrNull()
                _currentSession.value = _currentSession.value?.copy(
                    targetBox = result.getOrNull(),
                    status = SessionStatus.MOVING_FILES
                )
                _scannedBarcode.value = ""
                beepPlayer.positive()
            } else {
                _uiState.value = SegregationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to scan target box")
                beepPlayer.error()
            }
        }
    }

    fun moveFile(fileBarcode: String) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            val sourceBoxBarcode = session.sourceBox.barcode
            
            // Duplicate Scan Guard
            val isDuplicate = session.movedFiles.any { it.barcode == fileBarcode }
            if (isDuplicate) {
                _uiState.value = SegregationUiState.ValidationError("File already scanned/moved")
                beepPlayer.error()
                return@launch
            }

            // Validate file belongs to source box
            val fileInSource = session.sourceFiles.any { it.barcode == fileBarcode }
            if (!fileInSource) {
                // Find the file to show validation error
                val invalidFile = session.sourceFiles.firstOrNull { it.barcode == fileBarcode } 
                    ?: FileRecord("", fileBarcode, "Unknown File", sourceBoxBarcode)
                _validationError.value = invalidFile
                _uiState.value = SegregationUiState.ValidationError("File does not belong to source box")
                beepPlayer.error() // Error beep for Out file / mismatch
                return@launch
            }
            
            val result = moveFileUseCase(fileBarcode)
            if (result.isSuccess) {
                val movedFile = result.getOrNull()!!
                _currentSession.value = session.copy(
                    sourceFiles = session.sourceFiles.filter { it.barcode != fileBarcode },
                    movedFiles = session.movedFiles + movedFile
                )
                _scannedBarcode.value = ""
                _validationError.value = null
                beepPlayer.positive() // Positive beep for In file
            } else {
                _uiState.value = SegregationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to move file")
                beepPlayer.error()
            }
        }
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    fun completeAssignedSegregation(segregationId: String) {
        viewModelScope.launch {
            _uiState.value = SegregationUiState.Loading
            val result = repository.completeSegregation(segregationId)
            if (result.isSuccess) {
                loadAssignedSegregations()
            } else {
                _uiState.value = SegregationUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to complete segregation"
                )
            }
        }
    }

    fun completeSegregation() {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            
            val result = if (_isOffline.value) {
                // Show queued state if offline
                Result.success(Unit)
            } else {
                completeSegregationSessionUseCase(session.sessionId)
            }
            
            if (result.isSuccess) {
                _uiState.value = SegregationUiState.SegregationCompleted
                stopScanningUseCase()
                resetSegregation()
            } else {
                _uiState.value = SegregationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to complete segregation")
            }
        }
    }

    fun resetSegregation() {
        _currentSession.value = null
        _sourceBox.value = null
        _targetBox.value = null
        _scannedBarcode.value = ""
        _validationError.value = null
        viewModelScope.launch { stopScanningUseCase() }
        loadAssignedSegregations()
    }

    fun getRemainingCount(): Int {
        return _currentSession.value?.sourceFiles?.size ?: 0
    }

    fun getMovedCount(): Int {
        return _currentSession.value?.movedFiles?.size ?: 0
    }

    fun getTotalCount(): Int {
        val session = _currentSession.value ?: return 0
        return session.sourceFiles.size + session.movedFiles.size
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { stopScanningUseCase() }
    }
}
