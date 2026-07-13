package com.tionix.rms.feature.transfer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.core.scanner.domain.usecase.InitializeScannerUseCase
import com.tionix.rms.core.scanner.domain.usecase.StartScanningUseCase
import com.tionix.rms.core.scanner.domain.usecase.StopScanningUseCase
import com.tionix.rms.feature.transfer.domain.model.SessionStatus
import com.tionix.rms.feature.transfer.domain.model.TransferItem
import com.tionix.rms.feature.transfer.domain.model.TransferSession
import com.tionix.rms.feature.transfer.domain.model.TransferType
import com.tionix.rms.feature.transfer.domain.repository.TransferRepository
import com.tionix.rms.feature.transfer.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val repository: TransferRepository,
    private val startTransferSessionUseCase: StartTransferSessionUseCase,
    private val addTransferItemUseCase: AddTransferItemUseCase,
    private val removeTransferItemUseCase: RemoveTransferItemUseCase,
    private val setDestinationUseCase: SetDestinationUseCase,
    private val submitTransferUseCase: SubmitTransferUseCase,
    private val scannerRepository: ScannerRepository,
    private val initializeScannerUseCase: InitializeScannerUseCase,
    private val startScanningUseCase: StartScanningUseCase,
    private val stopScanningUseCase: StopScanningUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransferUiState>(TransferUiState.Loading)
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()

    private val _currentSession = MutableStateFlow<TransferSession?>(null)
    val currentSession: StateFlow<TransferSession?> = _currentSession.asStateFlow()

    private val _selectedTransferType = MutableStateFlow<TransferType?>(null)
    val selectedTransferType: StateFlow<TransferType?> = _selectedTransferType.asStateFlow()

    private val _destination = MutableStateFlow("")
    val destination: StateFlow<String> = _destination.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private val steps = listOf(
        "Select Type",
        "Scan Source",
        "Select Destination",
        "Review",
        "Submit"
    )

    init {
        loadAssignedTransfers()
        
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
                addItem(barcode)
            }
            SessionStatus.SELECTING_DESTINATION -> {
                _destination.value = barcode
                setDestination(barcode)
            }
            else -> {}
        }
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

    fun onDestinationChanged(value: String) {
        _destination.value = value
    }

    fun selectTransferType(type: TransferType) {
        _selectedTransferType.value = type
        _currentStep.value = 1
        startSession(type)
    }

    private fun startSession(type: TransferType) {
        viewModelScope.launch {
            val result = startTransferSessionUseCase(type)
            if (result.isSuccess) {
                _currentSession.value = result.getOrNull()
                _uiState.value = TransferUiState.SessionStarted
                // Start scanner for continuous scanning
                initializeScannerUseCase()
                startScanningUseCase()
            } else {
                _uiState.value = TransferUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start session")
            }
        }
    }

    fun addItem(barcode: String) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            val result = addTransferItemUseCase(session.sessionId, barcode)
            if (result.isSuccess) {
                _currentSession.value = session.copy(
                    sourceItems = session.sourceItems + result.getOrNull()!!
                )
                _scannedBarcode.value = ""
            } else {
                _uiState.value = TransferUiState.Error(result.exceptionOrNull()?.message ?: "Failed to add item")
            }
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            val result = removeTransferItemUseCase(session.sessionId, itemId)
            if (result.isSuccess) {
                _currentSession.value = session.copy(
                    sourceItems = session.sourceItems.filter { it.id != itemId }
                )
            } else {
                _uiState.value = TransferUiState.Error(result.exceptionOrNull()?.message ?: "Failed to remove item")
            }
        }
    }

    fun moveToDestinationStep() {
        _currentStep.value = 2
        _currentSession.value = _currentSession.value?.copy(
            status = SessionStatus.SELECTING_DESTINATION
        )
    }

    fun setDestination(destination: String) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            val result = setDestinationUseCase(session.sessionId, destination)
            if (result.isSuccess) {
                _currentSession.value = session.copy(
                    destination = destination,
                    status = SessionStatus.REVIEWING
                )
                _currentStep.value = 3
            } else {
                _uiState.value = TransferUiState.Error(result.exceptionOrNull()?.message ?: "Failed to set destination")
            }
        }
    }

    fun moveToReviewStep() {
        _currentStep.value = 3
        _currentSession.value = _currentSession.value?.copy(
            status = SessionStatus.REVIEWING
        )
    }

    fun completeTransfer(transferId: String) {
        viewModelScope.launch {
            _uiState.value = TransferUiState.Loading
            val result = repository.completeTransfer(transferId)
            if (result.isSuccess) {
                loadAssignedTransfers()
            } else {
                _uiState.value = TransferUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to complete transfer"
                )
            }
        }
    }

    fun submitTransfer() {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch
            
            val result = if (_isOffline.value) {
                // Show queued state if offline
                _currentSession.value = session.copy(
                    status = SessionStatus.QUEUED_OFFLINE
                )
                Result.success(Unit)
            } else {
                submitTransferUseCase(session.sessionId)
            }
            
            if (result.isSuccess) {
                _currentStep.value = 4
                _uiState.value = TransferUiState.TransferSubmitted
                stopScanningUseCase()
            } else {
                _uiState.value = TransferUiState.Error(result.exceptionOrNull()?.message ?: "Failed to submit transfer")
            }
        }
    }

    fun resetTransfer() {
        _currentSession.value = null
        _selectedTransferType.value = null
        _destination.value = ""
        _scannedBarcode.value = ""
        _currentStep.value = 0
        viewModelScope.launch { stopScanningUseCase() }
        loadAssignedTransfers()
    }

    fun getCurrentStep(): Int {
        return _currentStep.value
    }

    fun getTotalSteps(): Int {
        return steps.size
    }

    fun getSteps(): List<String> {
        return steps
    }
}
