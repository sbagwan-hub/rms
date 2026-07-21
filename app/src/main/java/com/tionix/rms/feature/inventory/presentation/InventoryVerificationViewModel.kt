package com.tionix.rms.feature.inventory.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.audio.BeepPlayer
import com.tionix.rms.feature.inventory.domain.model.Box
import com.tionix.rms.feature.inventory.domain.model.BoxStatus
import com.tionix.rms.feature.inventory.domain.model.InventoryVerification
import com.tionix.rms.feature.inventory.domain.model.ScannedBox
import com.tionix.rms.feature.inventory.domain.model.ScanStatus
import com.tionix.rms.feature.inventory.domain.model.StartVerificationRequest
import com.tionix.rms.feature.inventory.domain.repository.InventoryVerificationRepository
import com.tionix.rms.feature.inventory.domain.usecase.CompleteVerificationUseCase
import com.tionix.rms.feature.inventory.domain.usecase.GetExpectedBoxesUseCase
import com.tionix.rms.feature.inventory.domain.usecase.StartVerificationUseCase
import com.tionix.rms.feature.inventory.domain.usecase.VerifyBoxUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryVerificationViewModel @Inject constructor(
    private val repository: InventoryVerificationRepository,
    private val startVerificationUseCase: StartVerificationUseCase,
    private val getExpectedBoxesUseCase: GetExpectedBoxesUseCase,
    private val verifyBoxUseCase: VerifyBoxUseCase,
    private val completeVerificationUseCase: CompleteVerificationUseCase,
    private val beepPlayer: BeepPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow<InventoryVerificationUiState>(InventoryVerificationUiState.Loading)
    val uiState: StateFlow<InventoryVerificationUiState> = _uiState.asStateFlow()

    private val _selectedLocationId = MutableStateFlow("")
    val selectedLocationId: StateFlow<String> = _selectedLocationId.asStateFlow()

    private val _scannedBarcode = MutableStateFlow("")
    val scannedBarcode: StateFlow<String> = _scannedBarcode.asStateFlow()

    private val _expectedBoxes = MutableStateFlow<List<Box>>(emptyList())
    val expectedBoxes: StateFlow<List<Box>> = _expectedBoxes.asStateFlow()

    private val _scannedBoxes = MutableStateFlow<List<ScannedBox>>(emptyList())
    val scannedBoxes: StateFlow<List<ScannedBox>> = _scannedBoxes.asStateFlow()

    private val _currentVerification = MutableStateFlow<InventoryVerification?>(null)
    val currentVerification: StateFlow<InventoryVerification?> = _currentVerification.asStateFlow()

    private val _showDiscrepancyDialog = MutableStateFlow(false)
    val showDiscrepancyDialog: StateFlow<Boolean> = _showDiscrepancyDialog.asStateFlow()

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
                _currentVerification.value = result.getOrNull()
                _uiState.value = InventoryVerificationUiState.VerificationStarted
                loadExpectedBoxes()
            } else {
                _uiState.value = InventoryVerificationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start verification")
            }
        }
    }

    private fun loadExpectedBoxes() {
        viewModelScope.launch {
            val locationId = _currentVerification.value?.locationId ?: _selectedLocationId.value
            val result = getExpectedBoxesUseCase(locationId)
            
            if (result.isSuccess) {
                _expectedBoxes.value = result.getOrNull() ?: emptyList()
            } else {
                _uiState.value = InventoryVerificationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to load expected boxes")
            }
        }
    }

    fun verifyBox(barcode: String) {
        viewModelScope.launch {
            val verificationId = _currentVerification.value?.id ?: return@launch
            
            val result = verifyBoxUseCase(barcode, verificationId)
            if (result.isSuccess) {
                val scannedBox = result.getOrNull()!!
                
                when (scannedBox.scanStatus) {
                    ScanStatus.VERIFIED -> {
                        _scannedBoxes.value = _scannedBoxes.value + scannedBox
                        _uiState.value = InventoryVerificationUiState.BoxScanned
                        beepPlayer.positive() // Verified item success
                    }
                    ScanStatus.UNEXPECTED -> {
                        _scannedBoxes.value = _scannedBoxes.value + scannedBox
                        _uiState.value = InventoryVerificationUiState.BoxScanned
                        beepPlayer.warning() // Unexpected item warning
                    }
                    ScanStatus.DUPLICATE -> {
                        beepPlayer.error() // Duplicate alert
                    }
                }
                
                // Update box status in expected boxes
                updateBoxStatus(barcode, scannedBox.scanStatus)
            } else {
                _uiState.value = InventoryVerificationUiState.Error(result.exceptionOrNull()?.message ?: "Scan failed")
                beepPlayer.error()
            }
        }
    }

    private fun updateBoxStatus(barcode: String, scanStatus: ScanStatus) {
        _expectedBoxes.value = _expectedBoxes.value.map { box ->
            if (box.barcode == barcode) {
                when (scanStatus) {
                    ScanStatus.VERIFIED -> box.copy(status = BoxStatus.VERIFIED)
                    ScanStatus.UNEXPECTED -> box.copy(status = BoxStatus.UNEXPECTED)
                    ScanStatus.DUPLICATE -> box
                }
            } else {
                box
            }
        }
    }

    fun prepareForSubmit() {
        _showDiscrepancyDialog.value = true
    }

    fun dismissDiscrepancyDialog() {
        _showDiscrepancyDialog.value = false
    }

    fun completeVerification() {
        viewModelScope.launch {
            val verificationId = _currentVerification.value?.id ?: return@launch
            
            val result = completeVerificationUseCase(verificationId)
            if (result.isSuccess) {
                _showDiscrepancyDialog.value = false
                _uiState.value = InventoryVerificationUiState.VerificationCompleted
                _currentVerification.value = null
                _expectedBoxes.value = emptyList()
                _scannedBoxes.value = emptyList()
                loadAssignedVerifications()
            } else {
                _uiState.value = InventoryVerificationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to complete verification")
            }
        }
    }

    fun completeVerification(verificationId: String) {
        viewModelScope.launch {
            _uiState.value = InventoryVerificationUiState.Loading
            val result = completeVerificationUseCase(verificationId)
            if (result.isSuccess) {
                loadAssignedVerifications()
            } else {
                _uiState.value = InventoryVerificationUiState.Error(result.exceptionOrNull()?.message ?: "Failed to complete verification")
            }
        }
    }

    fun resumeVerification(verification: InventoryVerification) {
        _currentVerification.value = verification
        _uiState.value = InventoryVerificationUiState.VerificationStarted
        loadExpectedBoxes()
    }

    fun exitVerification() {
        _currentVerification.value = null
        _expectedBoxes.value = emptyList()
        _scannedBoxes.value = emptyList()
        loadAssignedVerifications()
    }

    fun getProgress(): Int {
        val expectedCount = _expectedBoxes.value.size
        if (expectedCount == 0) return 0
        val verifiedCount = _expectedBoxes.value.count { it.status == BoxStatus.VERIFIED }
        return (verifiedCount * 100) / expectedCount
    }

    fun getVerifiedCount(): Int {
        return _expectedBoxes.value.count { it.status == BoxStatus.VERIFIED }
    }

    fun getMissingCount(): Int {
        return _expectedBoxes.value.count { it.status == BoxStatus.PENDING }
    }

    fun getUnexpectedCount(): Int {
        return _scannedBoxes.value.count { it.scanStatus == ScanStatus.UNEXPECTED }
    }
}

