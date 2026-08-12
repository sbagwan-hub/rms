package com.tionix.rms.feature.freshboxmove.presentation

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.audio.BeepPlayer
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxSessionEntity
import com.tionix.rms.feature.freshboxmove.domain.repository.FreshBoxMoveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class FreshBoxMoveViewModel @Inject constructor(
    private val repository: FreshBoxMoveRepository,
    private val beepPlayer: BeepPlayer,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        const val STEP_ROOM = 0
        const val STEP_RACK = 1
        const val STEP_LOCATION = 2
        const val STEP_BOXES = 3
    }

    private val _uiState = MutableStateFlow<FreshBoxMoveUiState>(FreshBoxMoveUiState.Idle)
    val uiState: StateFlow<FreshBoxMoveUiState> = _uiState.asStateFlow()

    private val _activeSession = MutableStateFlow<FreshBoxSessionEntity?>(null)
    val activeSession: StateFlow<FreshBoxSessionEntity?> = _activeSession.asStateFlow()

    /** 0=room, 1=rack, 2=location, 3=boxes */
    private val _step = MutableStateFlow(STEP_ROOM)
    val step: StateFlow<Int> = _step.asStateFlow()

    private val _roomBarcode = MutableStateFlow<String?>(null)
    val roomBarcode: StateFlow<String?> = _roomBarcode.asStateFlow()

    private val _rackBarcode = MutableStateFlow<String?>(null)
    val rackBarcode: StateFlow<String?> = _rackBarcode.asStateFlow()

    private val _locationBarcode = MutableStateFlow("")
    val locationBarcode: StateFlow<String> = _locationBarcode.asStateFlow()

    private val _boxBarcode = MutableStateFlow("")
    val boxBarcode: StateFlow<String> = _boxBarcode.asStateFlow()

    private val _duplicateScanWarning = MutableSharedFlow<String>()
    val duplicateScanWarning: SharedFlow<String> = _duplicateScanWarning.asSharedFlow()

    private val _lockLocation = MutableStateFlow(true)
    val lockLocation: StateFlow<Boolean> = _lockLocation.asStateFlow()

    val scansList = _activeSession.flatMapLatest { session ->
        if (session != null) {
            repository.getScansForSessionFlow(session.clientSessionId)
        } else {
            flowOf(emptyList())
        }
    }

    init {
        checkActiveSession()
    }

    private fun checkActiveSession() {
        viewModelScope.launch {
            val session = repository.getActiveSession()
            if (session != null) {
                _activeSession.value = session
                _step.value = STEP_ROOM
                _uiState.value = FreshBoxMoveUiState.ActiveSession
            } else {
                _uiState.value = FreshBoxMoveUiState.Idle
            }
        }
    }

    fun startSession(deviceId: String?) {
        viewModelScope.launch {
            _uiState.value = FreshBoxMoveUiState.Loading
            val result = repository.startSession(deviceId)
            if (result.isSuccess) {
                val session = result.getOrNull()
                _activeSession.value = session
                resetScanContext()
                _uiState.value = FreshBoxMoveUiState.ActiveSession
            } else {
                _uiState.value = FreshBoxMoveUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to start session"
                )
            }
        }
    }

    fun onLocationBarcodeChanged(value: String) {
        _locationBarcode.value = value
    }

    fun onBoxBarcodeChanged(value: String) {
        _boxBarcode.value = value
    }

    fun onLockLocationChanged(value: Boolean) {
        _lockLocation.value = value
    }

    fun skipStep() {
        when (_step.value) {
            STEP_ROOM -> _step.value = STEP_RACK
            STEP_RACK -> _step.value = STEP_LOCATION
        }
    }

    fun handleBarcodeScan(barcode: String) {
        val trimmed = barcode.trim()
        if (trimmed.isEmpty()) return

        if (_activeSession.value == null) return

        when (_step.value) {
            STEP_ROOM -> {
                _roomBarcode.value = trimmed
                _step.value = STEP_RACK
                beepPlayer.positive()
            }
            STEP_RACK -> {
                if (trimmed == _roomBarcode.value) return
                _rackBarcode.value = trimmed
                _step.value = STEP_LOCATION
                beepPlayer.positive()
            }
            STEP_LOCATION -> {
                if (trimmed == _rackBarcode.value || trimmed == _roomBarcode.value) return
                _locationBarcode.value = trimmed
                _step.value = STEP_BOXES
                beepPlayer.positive()
            }
            STEP_BOXES -> submitScan(trimmed)
        }
    }

    fun submitScan(boxCode: String) {
        viewModelScope.launch {
            val session = _activeSession.value
            if (session == null) {
                _uiState.value = FreshBoxMoveUiState.Error("No active session")
                beepPlayer.error()
                return@launch
            }

            if (_step.value != STEP_BOXES) {
                beepPlayer.error()
                return@launch
            }

            val locCode = _locationBarcode.value.trim()
            if (locCode.isBlank()) {
                _uiState.value = FreshBoxMoveUiState.Error("Please scan or enter a location barcode first")
                beepPlayer.error()
                return@launch
            }

            val targetBoxCode = boxCode.trim()
            if (targetBoxCode.isBlank()) {
                return@launch
            }

            val currentScans = repository.getScansForSessionFlow(session.clientSessionId).first()
            val isDuplicate = currentScans.any { it.boxBarcode == targetBoxCode }
            if (isDuplicate) {
                triggerDuplicateFeedback()
                _duplicateScanWarning.emit("Box '$targetBoxCode' already scanned in this session")
                return@launch
            }

            _uiState.value = FreshBoxMoveUiState.Loading
            val result = repository.submitScan(
                boxBarcode = targetBoxCode,
                locationBarcode = locCode,
                roomBarcode = _roomBarcode.value,
                rackBarcode = _rackBarcode.value,
                gpsLat = null,
                gpsLng = null
            )

            if (result.isSuccess) {
                _boxBarcode.value = ""
                if (!_lockLocation.value) {
                    _locationBarcode.value = ""
                    _step.value = STEP_LOCATION
                }
                _uiState.value = FreshBoxMoveUiState.ActiveSession

                val boxesAtLocationCount = currentScans.count { it.locationBarcode == locCode }
                if (boxesAtLocationCount >= 9) {
                    beepPlayer.warning()
                } else {
                    beepPlayer.positive()
                }
            } else {
                _uiState.value = FreshBoxMoveUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to submit scan"
                )
                beepPlayer.error()
            }
        }
    }

    private fun triggerDuplicateFeedback() {
        beepPlayer.error()
        try {
            context.getSystemService(Vibrator::class.java)?.let { vibrator ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    fun endSession() {
        viewModelScope.launch {
            _uiState.value = FreshBoxMoveUiState.Loading
            val result = repository.endSession()
            if (result.isSuccess) {
                _activeSession.value = null
                resetScanContext()
                _uiState.value = FreshBoxMoveUiState.Idle
            } else {
                _uiState.value = FreshBoxMoveUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to end session"
                )
            }
        }
    }

    fun resetLocation() {
        _locationBarcode.value = ""
        _step.value = STEP_LOCATION
    }

    private fun resetScanContext() {
        _step.value = STEP_ROOM
        _roomBarcode.value = null
        _rackBarcode.value = null
        _locationBarcode.value = ""
        _boxBarcode.value = ""
    }
}
