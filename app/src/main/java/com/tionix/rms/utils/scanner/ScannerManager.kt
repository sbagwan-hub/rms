package com.tionix.rms.utils.scanner

import kotlinx.coroutines.flow.Flow

interface ScannerManager {
    /**
     * Flow of barcode strings emitted by the Honeywell scanner.
     */
    val scanResults: Flow<String>

    /**
     * Flow representing the active trigger/scanning state of the hardware imager.
     */
    val isScanning: Flow<Boolean>

    /**
     * Enable the Honeywell scanner imager.
     * Registers the broadcast receiver for Honeywell decoder events and claims the scanner.
     */
    fun enable()

    /**
     * Disable the Honeywell scanner imager.
     * Unregisters the broadcast receiver and releases the scanner.
     */
    fun disable()

    /**
     * Manually trigger the scanner imager (software trigger).
     */
    fun startScanTrigger()

    /**
     * Stop the manual scan trigger.
     */
    fun stopScanTrigger()

    /**
     * Set continuous scan mode.
     * @param enabled True to scan continuously without resetting trigger.
     */
    fun setContinuousMode(enabled: Boolean)

    /**
     * Start camera-based QR/barcode scanning using Google Play Services Code Scanner.
     */
    fun startCameraScan(context: android.content.Context, onScanResult: ((String) -> Unit)? = null)
}
