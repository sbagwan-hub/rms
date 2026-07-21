package com.tionix.rms.utils.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

@Singleton
class HoneywellScannerManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ScannerManager {

    companion object {
        // Honeywell Android Data Collection Intent API — action + extra key
        // constants below are taken verbatim from Honeywell's published Intent
        // API guide. Using anything else (e.g. a made-up ACTION_SET_PROPERTIES,
        // or an EXTRA_CONTROL key) is silently ignored by the on-device
        // DCS-IntentApi receiver — it does not error, it just does nothing.
        private const val ACTION_CLAIM_SCANNER = "com.honeywell.aidc.action.ACTION_CLAIM_SCANNER"
        private const val ACTION_RELEASE_SCANNER = "com.honeywell.aidc.action.ACTION_RELEASE_SCANNER"
        private const val ACTION_CONTROL_SCANNER = "com.honeywell.aidc.action.ACTION_CONTROL_SCANNER"

        private const val EXTRA_PROPERTIES = "com.honeywell.aidc.extra.EXTRA_PROPERTIES"
        private const val EXTRA_SCAN = "com.honeywell.aidc.extra.EXTRA_SCAN"
        private const val EXTRA_AIM = "com.honeywell.aidc.extra.EXTRA_AIM"
        private const val EXTRA_LIGHT = "com.honeywell.aidc.extra.EXTRA_LIGHT"
        private const val EXTRA_DECODE = "com.honeywell.aidc.extra.EXTRA_DECODE"

        /** Our own action — configured into the scanner via DPR_DATA_INTENT_ACTION below. */
        private const val ACTION_BARCODE_DATA = "com.tionix.rms.ACTION_BARCODE_DATA"
    }

    // Deliberately implicit (no setPackage): `dumpsys package` on the EDA52 test
    // unit lists the IntentApiReceiver under com.honeywell.tools.scanner.edge,
    // but the process that actually logs DCS-IntentApi and handles the broadcast
    // is com.intermec.datacollectionservice (they share a process/UID on this
    // firmware). Restricting to either package name by Intent.setPackage caused
    // the broadcast to silently fail to resolve. Implicit delivery — despite an
    // accompanying "Background execution not allowed" warning — was confirmed
    // working end-to-end (claim succeeds, aim/light/decode engage).
    private fun honeywellIntent(action: String) = Intent(action)

    private val _scanResults = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val scanResults: SharedFlow<String> = _scanResults.asSharedFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var isRegistered = false
    private var isClaimed = false
    private var continuousMode = false

    private val scannerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            // Extract the barcode string from the configured data intent.
            // "data" is Honeywell's documented extra key; the others are
            // kept as fallbacks for older/alternate firmware configurations.
            val barcode = intent.getStringExtra("data")
                ?: intent.getStringExtra("barcode_string")
                ?: intent.getStringExtra("barcode")
                ?: intent.getStringExtra("value")

            if (barcode != null) {
                CoroutineScope(Dispatchers.Default).launch {
                    _scanResults.emit(barcode.trim())
                }
            }
        }
    }

    override fun enable() {
        if (!isRegistered) {
            val filter = IntentFilter().apply {
                addAction(ACTION_BARCODE_DATA)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(scannerReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(scannerReceiver, filter)
            }
            isRegistered = true
        }
        claimScanner()
    }

    override fun disable() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(scannerReceiver)
            } catch (e: IllegalArgumentException) {
                // Ignore receiver not registered errors
            }
            isRegistered = false
            _isScanning.value = false
        }
        if (isClaimed) {
            context.sendBroadcast(honeywellIntent(ACTION_RELEASE_SCANNER))
            isClaimed = false
        }
    }

    override fun startScanTrigger() {
        _isScanning.value = true
        // Set EXTRA_AIM/LIGHT/DECODE explicitly rather than relying on their
        // documented "defaults to EXTRA_SCAN" behavior — observed on-device
        // logs (DCS-IntentApi) show them read as false when omitted, even
        // with EXTRA_SCAN=true.
        val intent = honeywellIntent(ACTION_CONTROL_SCANNER).apply {
            putExtra(EXTRA_SCAN, true)
            putExtra(EXTRA_AIM, true)
            putExtra(EXTRA_LIGHT, true)
            putExtra(EXTRA_DECODE, true)
        }
        context.sendBroadcast(intent)
    }

    override fun stopScanTrigger() {
        _isScanning.value = false
        val intent = honeywellIntent(ACTION_CONTROL_SCANNER).apply {
            putExtra(EXTRA_SCAN, false)
            putExtra(EXTRA_AIM, false)
            putExtra(EXTRA_LIGHT, false)
            putExtra(EXTRA_DECODE, false)
        }
        context.sendBroadcast(intent)
    }

    override fun setContinuousMode(enabled: Boolean) {
        continuousMode = enabled
        // Properties only take effect via a (re-)claim, not a standalone action.
        if (isClaimed) claimScanner()
    }

    /**
     * Claims the imager and, in the same intent, configures it to deliver
     * decode results via [ACTION_BARCODE_DATA] rather than the default
     * "Data Processing Result" toast/notification. Per Honeywell's Intent
     * API, EXTRA_PROPERTIES must be attached to ACTION_CLAIM_SCANNER itself
     * — there is no separate "set properties" action.
     */
    private fun claimScanner() {
        val properties = Bundle().apply {
            putBoolean("DPR_DATA_INTENT", true)
            putString("DPR_DATA_INTENT_ACTION", ACTION_BARCODE_DATA)
            if (continuousMode) {
                putString("property_trigger_control_mode", "continuous")
            } else {
                putString("property_trigger_control_mode", "one_shot")
            }
        }
        val intent = honeywellIntent(ACTION_CLAIM_SCANNER).apply {
            putExtra(EXTRA_PROPERTIES, properties)
        }
        context.sendBroadcast(intent)
        isClaimed = true
    }

    override fun startCameraScan(context: Context, onScanResult: ((String) -> Unit)?) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .enableAutoZoom()
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)

        scanner.startScan()
            .addOnSuccessListener { barcode: com.google.mlkit.vision.barcode.common.Barcode ->
                val rawValue = barcode.rawValue
                if (rawValue != null) {
                    val trimmed = rawValue.trim()
                    CoroutineScope(Dispatchers.Default).launch {
                        _scanResults.emit(trimmed)
                    }
                    onScanResult?.invoke(trimmed)
                }
            }
            .addOnFailureListener { e ->
                // Handle/log error if necessary
            }
    }
}
