package com.tionix.rms.ui.screens.scan

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.core.audio.BeepPlayer
import com.tionix.rms.core.network.dto.LookupData
import com.tionix.rms.data.repository.ScanRepository
import com.tionix.rms.utils.scanner.ScannerManager
import com.tionix.rms.scanner.ScannerAvailability
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ScanViewModel
 * =============
 * Coordinates barcode scanning, resolves lookups via [ScanRepository], and triggers audio feedback.
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    val scanner: ScannerManager,
    private val scanRepository: ScanRepository,
    private val beepPlayer: BeepPlayer,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    companion object {
        /** Beam auto-off if nothing is decoded (battery + safety). */
        private const val BEAM_TIMEOUT_MS = 5_000L
    }

    data class UiState(
        val scannerMode: ScannerAvailability.Mode = ScannerAvailability.Mode.CAMERA,
        val showCamera: Boolean = false,    // camera fallback overlay visible
        val scanning: Boolean = false,      // imager beam currently ON (SCAN button pressed)
        val lastBarcode: String? = null,
        val loading: Boolean = false,
        val result: LookupData? = null,     // entity + contents + breadcrumb path
        val error: String? = null,          // e.g. "Barcode not registered"
        val recorded: Boolean = false,      // scan persisted → admin panel has it
        val history: List<String> = emptyList(), // this session's scans (newest first)
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var beamTimeoutJob: Job? = null

    init {
        // Detect once: real Honeywell imager present, or camera fallback?
        val mode = ScannerAvailability.detect(appContext)
        _state.update { it.copy(scannerMode = mode) }

        // Claim the Honeywell imager for this screen's lifetime — without this,
        // the physical/software trigger fires but the decoder rejects it
        // ("Scanner is not claimed").
        if (mode == ScannerAvailability.Mode.HONEYWELL_IMAGER) {
            scanner.enable()
        }

        // CONTINUOUS SCAN: physical trigger OR software trigger — both deliver
        // decodes through this single flow. A decode also ends software-scan mode.
        viewModelScope.launch {
            scanner.scanResults.collect { barcode ->
                stopBeam() // decode received → beam handled; reset UI state
                onBarcode(barcode)
            }
        }
    }

    /**
     * ON-SCREEN SCAN BUTTON → activates the Honeywell imager immediately
     * (same beam the physical trigger fires). Auto-off after
     * [BEAM_TIMEOUT_MS] if no barcode was read.
     */
    fun triggerScan() {
        // NO Honeywell service (normal phone / emulator) → open the CAMERA
        // scanner overlay instead of firing imager intents into the void.
        if (_state.value.scannerMode == ScannerAvailability.Mode.CAMERA) {
            _state.update { it.copy(showCamera = true, error = null) }
            return
        }
        if (_state.value.scanning) return // already scanning — ignore double-tap
        _state.update { it.copy(scanning = true, error = null) }
        scanner.startScanTrigger()

        beamTimeoutJob?.cancel()
        beamTimeoutJob = viewModelScope.launch {
            delay(BEAM_TIMEOUT_MS)
            stopBeam()
        }
    }

    /** Camera overlay decoded a barcode (or user cancelled). */
    fun onCameraResult(barcode: String?) {
        _state.update { it.copy(showCamera = false) }
        if (!barcode.isNullOrBlank()) onBarcode(barcode)
    }

    /** Beam OFF + clear scanning flag (decode arrived / timeout / user cancel). */
    fun stopBeam() {
        beamTimeoutJob?.cancel()
        beamTimeoutJob = null
        if (_state.value.scanning) {
            scanner.stopScanTrigger()
            _state.update { it.copy(scanning = false) }
        }
    }

    override fun onCleared() {
        // Screen destroyed while beam on → make sure the imager is released
        stopBeam()
        if (_state.value.scannerMode == ScannerAvailability.Mode.HONEYWELL_IMAGER) {
            scanner.disable()
        }
        super.onCleared()
    }

    /** Also called by the manual-entry field (damaged/unreadable labels). */
    fun onBarcode(barcode: String) {
        val code = barcode.trim()
        if (code.isEmpty()) return

        _state.update {
            it.copy(
                lastBarcode = code,
                loading = true,
                error = null,
                recorded = false,
                history = (listOf(code) + it.history).take(20),
            )
        }

        viewModelScope.launch {
            // 1) What is it + what's inside?
            scanRepository.lookup(code)
                .onSuccess { data -> _state.update { it.copy(loading = false, result = data) } }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, result = null, error = e.message) }
                    return@launch // unknown barcode → nothing to record
                }

            // 2) Persist the scan (fire-and-report). GPS is stamped by the
            //    backend Operation; coordinates wired in the GPS module.
            scanRepository.record(code, lat = null, lng = null)
                .onSuccess { _state.update { it.copy(recorded = true) } }
                .onFailure { e ->
                    // Lookup succeeded but persistence failed (e.g. network blip):
                    // surface it — the offline queue (Module 10) will remove
                    // this failure mode entirely by queueing locally.
                    _state.update { it.copy(error = "Saved locally failed: ${e.message}") }
                }
        }
    }
}
