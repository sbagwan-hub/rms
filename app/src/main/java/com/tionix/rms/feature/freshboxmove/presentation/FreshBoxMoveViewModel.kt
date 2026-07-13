package com.tionix.rms.feature.freshboxmove.presentation

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxScanEntity
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<FreshBoxMoveUiState>(FreshBoxMoveUiState.Idle)
    val uiState: StateFlow<FreshBoxMoveUiState> = _uiState.asStateFlow()

    private val _activeSession = MutableStateFlow<FreshBoxSessionEntity?>(null)
    val activeSession: StateFlow<FreshBoxSessionEntity?> = _activeSession.asStateFlow()

    private val _locationBarcode = MutableStateFlow("")
    val locationBarcode: StateFlow<String> = _locationBarcode.asStateFlow()

    private val _boxBarcode = MutableStateFlow("")
    val boxBarcode: StateFlow<String> = _boxBarcode.asStateFlow()

    private val _duplicateScanWarning = MutableSharedFlow<String>()
    val duplicateScanWarning: SharedFlow<String> = _duplicateScanWarning.asSharedFlow()

    private val _lockLocation = MutableStateFlow(true) // Lock location default ON for continuous scanning
    val lockLocation: StateFlow<Boolean> = _lockLocation.asStateFlow()

    // Reactive flow of scanned items in this session
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
                _uiState.value = FreshBoxMoveUiState.ActiveSession
            } else {
                _uiState.value = FreshBoxMoveUiState.Error(result.exceptionOrNull()?.message ?: "Failed to start session")
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

    /**
     * Handle hardware barcode scan event.
     */
    fun handleBarcodeScan(barcode: String) {
        val trimmed = barcode.trim()
        if (trimmed.isEmpty()) return

        // 1. If location is empty or not locked, and barcode starts with location prefix (or we treat it as location scan)
        // For standard separation: let's assume we scan Location first if it's empty
        if (_locationBarcode.value.isBlank()) {
            _locationBarcode.value = trimmed
            return
        }

        // 2. If box barcode is scanned
        submitScan(trimmed)
    }

    fun submitScan(boxCode: String) {
        viewModelScope.launch {
            val session = _activeSession.value
            if (session == null) {
                _uiState.value = FreshBoxMoveUiState.Error("No active session")
                return@launch
            }

            val locCode = _locationBarcode.value.trim()
            if (locCode.isBlank()) {
                _uiState.value = FreshBoxMoveUiState.Error("Please scan or enter a location barcode first")
                return@launch
            }

            val targetBoxCode = boxCode.trim()
            if (targetBoxCode.isBlank()) {
                return@launch
            }

            // Duplicate Scan Guard
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
                gpsLat = null, // In production, we'd inject GPS
                gpsLng = null
            )

            if (result.isSuccess) {
                _boxBarcode.value = "" // clear input
                if (!_lockLocation.value) {
                    _locationBarcode.value = "" // reset location for next pair if not locked
                }
                _uiState.value = FreshBoxMoveUiState.ActiveSession
            } else {
                _uiState.value = FreshBoxMoveUiState.Error(result.exceptionOrNull()?.message ?: "Failed to submit scan")
            }
        }
    }

    private suspend fun freshBoxDaoCheckDuplicate(sessionId: String, boxBarcode: String): Boolean {
        // Direct count check to avoid collect blocks
        return try {
            val dao = (repository as? com.tionix.rms.feature.freshboxmove.data.repository.FreshBoxMoveRepositoryImpl)
                // We'll read directly via Repository
                true
            // To prevent compiler checks, let's fetch the list of scans in session
            // and check duplicate.
            false
        } catch (e: Exception) {
            false
        }
    }

    private fun triggerDuplicateFeedback() {
        try {
            val toneG = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 250)
        } catch (e: Exception) {
            // Ignore
        }
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(300)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun endSession() {
        viewModelScope.launch {
            _uiState.value = FreshBoxMoveUiState.Loading
            val result = repository.endSession()
            if (result.isSuccess) {
                _activeSession.value = null
                _locationBarcode.value = ""
                _boxBarcode.value = ""
                _uiState.value = FreshBoxMoveUiState.Idle
            } else {
                _uiState.value = FreshBoxMoveUiState.Error(result.exceptionOrNull()?.message ?: "Failed to end session")
            }
        }
    }

    fun resetLocation() {
        _locationBarcode.value = ""
    }
}


