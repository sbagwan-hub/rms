package com.tionix.rms.utils.scanner

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimulatedScanner @Inject constructor(
    @ApplicationContext private val context: Context
) : ScannerManager {

    private val _scanResults = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val scanResults: SharedFlow<String> = _scanResults.asSharedFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val testBarcodes = listOf(
        "LOC-A-1-01",
        "BOX-000001", 
        "FILE-000001",
        "FILE-000002",
        "FILE-000003"
    )
    private var currentIndex = 0

    override fun enable() {
        // Simulated scanner is always enabled
    }

    override fun disable() {
        _isScanning.value = false
    }

    override fun startScanTrigger() {
        _isScanning.value = true
        CoroutineScope(Dispatchers.Default).launch {
            // Simulate a scan after 1 second
            delay(1000)
            val barcode = testBarcodes[currentIndex % testBarcodes.size]
            _scanResults.emit(barcode)
            currentIndex++
            _isScanning.value = false
        }
    }

    override fun stopScanTrigger() {
        _isScanning.value = false
    }

    override fun setContinuousMode(enabled: Boolean) {
        // Not applicable for simulated scanner
    }

    override fun startCameraScan(context: Context, onScanResult: ((String) -> Unit)?) {
        // Simulate immediate scan result
        val barcode = testBarcodes[currentIndex % testBarcodes.size]
        CoroutineScope(Dispatchers.Default).launch {
            _scanResults.emit(barcode)
            onScanResult?.invoke(barcode)
            currentIndex++
        }
    }
}
