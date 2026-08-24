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
import com.tionix.rms.feature.segregation.domain.model.Segregation
import com.tionix.rms.feature.segregation.domain.model.SegregationSession
import com.tionix.rms.feature.segregation.domain.model.SessionStatus
import com.tionix.rms.feature.segregation.domain.repository.SegregationRepository
import com.tionix.rms.feature.segregation.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = MutableSharedFlow<String>()
    val refreshError: SharedFlow<String> = _refreshError.asSharedFlow()

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
        val cleanBarcode = barcode.trim().replace("\r", "").replace("\n", "").uppercase()
        if (cleanBarcode.isBlank()) return

        val session = _currentSession.value ?: return
        when (session.status) {
            SessionStatus.SCANNING_SOURCE -> {
                scanSourceBox(cleanBarcode)
            }
            SessionStatus.SCANNING_TARGET -> {
                scanTargetBox(cleanBarcode)
            }
            SessionStatus.MOVING_FILES -> {
                moveFile(cleanBarcode)
            }
            else -> {}
        }
    }

    fun loadAssignedSegregations(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                if (_isRefreshing.value) return@launch
                _isRefreshing.value = true
                android.util.Log.d("APPBAR_REFRESH", "Screen: Segregation\nAPI request started")
            } else {
                _uiState.value = SegregationUiState.Loading
            }

            val result = repository.getAssignedSegregations()

            if (result.isSuccess) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: Segregation\nAPI response: 200\nState updated")
                _uiState.value = SegregationUiState.Success(result.getOrNull() ?: emptyList())
            } else {
                if (isRefresh && _uiState.value is SegregationUiState.Success) {
                    _refreshError.emit("Unable to refresh data. Please try again.")
                } else {
                    _uiState.value = SegregationUiState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to load segregations"
                    )
                }
            }
            _isRefreshing.value = false
            if (isRefresh) {
                android.util.Log.d("APPBAR_REFRESH", "Screen: Segregation\nRefresh completed")
            }
        }
    }

    fun onScannedBarcodeChanged(value: String) {
        _scannedBarcode.value = value
    }

    fun openAssignedSegregation(segregation: Segregation) {
        viewModelScope.launch {
            _currentSession.value = null
            _sourceBox.value = null
            _targetBox.value = null
            _validationError.value = null
            _statusMessage.value = null
            _uiState.value = SegregationUiState.Loading

            android.util.Log.d("SEGREGATION_FLOW", "Opening segregation: ${segregation.id} (${segregation.segregationCode})")
            val result = repository.getSegregationDetails(segregation.id)
            if (result.isSuccess) {
                val session = result.getOrNull()!!
                android.util.Log.d(
                    "SEGREGATION_FLOW",
                    "Loaded session ${session.id}: Expected Old Box = ${session.sourceBox.barcode} (${session.sourceBox.id}), New Box = ${session.targetBox?.barcode} (${session.targetBox?.id})"
                )
                _currentSession.value = session.copy(status = SessionStatus.SCANNING_SOURCE)
                _sourceBox.value = session.sourceBox
                _targetBox.value = session.targetBox
                _uiState.value = SegregationUiState.SessionStarted
                _statusMessage.value = "Please scan Old Box: ${session.sourceBox.barcode}"

                initializeScannerUseCase()
                startScanningUseCase()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to open segregation session"
                android.util.Log.e("SEGREGATION_FLOW", "Failed to load session details: $err")
                _uiState.value = SegregationUiState.Error(err)
            }
        }
    }

    fun scanSourceBox(barcode: String) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            val cleanBarcode = barcode.trim().replace("\r", "").replace("\n", "").uppercase()
            android.util.Log.d("SEGREGATION_FLOW", "Scanning Old Box: input='$cleanBarcode', expected='${session.sourceBox.barcode}'")

            val result = scanSourceBoxUseCase(session.id, cleanBarcode)
            if (result.isSuccess) {
                _sourceBox.value = result.getOrNull()
                _currentSession.value = session.copy(
                    sourceBox = result.getOrNull()!!,
                    status = SessionStatus.SCANNING_TARGET
                )
                _scannedBarcode.value = ""
                _statusMessage.value = "Old Box verified. Please scan Destination Box: ${session.targetBox?.barcode ?: ""}"
                beepPlayer.positive()
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Wrong old box. Please scan ${session.sourceBox.barcode}."
                _statusMessage.value = errMsg
                beepPlayer.error()
            }
        }
    }

    fun scanTargetBox(barcode: String) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            val cleanBarcode = barcode.trim().replace("\r", "").replace("\n", "").uppercase()

            val result = scanTargetBoxUseCase(session.id, cleanBarcode)
            if (result.isSuccess) {
                _targetBox.value = result.getOrNull()
                _currentSession.value = session.copy(
                    targetBox = result.getOrNull(),
                    status = SessionStatus.MOVING_FILES
                )
                _scannedBarcode.value = ""
                _statusMessage.value = "Boxes verified! Ready to scan files."
                beepPlayer.positive()
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Wrong destination box. Please scan ${session.targetBox?.barcode ?: ""}."
                _statusMessage.value = errMsg
                beepPlayer.error()
            }
        }
    }

    fun moveFile(fileBarcode: String) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            val cleanBarcode = fileBarcode.trim().replace("\r", "").replace("\n", "").uppercase()

            // Duplicate Scan Guard
            val isDuplicate = session.movedFiles.any { it.barcode == cleanBarcode }
            if (isDuplicate) {
                _statusMessage.value = "File $cleanBarcode has already been moved."
                beepPlayer.error()
                return@launch
            }

            val result = moveFileUseCase(session.id, cleanBarcode)
            if (result.isSuccess) {
                val movedFile = result.getOrNull()!!
                val newMovedList = session.movedFiles + movedFile
                _currentSession.value = session.copy(
                    movedFiles = newMovedList,
                    movedCount = newMovedList.size
                )
                _scannedBarcode.value = ""
                _validationError.value = null
                _statusMessage.value = "File $cleanBarcode successfully moved to ${session.targetBox?.barcode ?: ""}."
                beepPlayer.positive()
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to move file $cleanBarcode"
                _statusMessage.value = errorMsg
                beepPlayer.error()
            }
        }
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    fun completeSegregation() {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            _uiState.value = SegregationUiState.Loading

            val result = completeSegregationSessionUseCase(session.id)
            if (result.isSuccess) {
                _uiState.value = SegregationUiState.SegregationCompleted
                stopScanningUseCase()
                resetSegregation()
            } else {
                _uiState.value = SegregationUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to complete segregation"
                )
            }
        }
    }

    fun resetSegregation() {
        _currentSession.value = null
        _sourceBox.value = null
        _targetBox.value = null
        _scannedBarcode.value = ""
        _validationError.value = null
        _statusMessage.value = null
        viewModelScope.launch { stopScanningUseCase() }
        loadAssignedSegregations()
    }

    fun getRemainingCount(): Int {
        val session = _currentSession.value ?: return 0
        return maxOf(0, session.totalFiles - session.movedFiles.size)
    }

    fun getMovedCount(): Int {
        return _currentSession.value?.movedFiles?.size ?: 0
    }

    fun getTotalCount(): Int {
        val session = _currentSession.value ?: return 0
        return maxOf(session.totalFiles, session.movedFiles.size)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { stopScanningUseCase() }
    }
}
