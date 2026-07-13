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

@Singleton
class HoneywellScannerManager @Inject constructor(
    @ApplicationContext private val context: Context
) : ScannerManager {

    private val _scanResults = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val scanResults: SharedFlow<String> = _scanResults.asSharedFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var isRegistered = false
    private var continuousMode = false

    private val scannerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            
            // Extract the barcode string from Honeywell intent
            // "barcode_string" is Honeywell's default extra key for data.
            // Also checking "data" and "barcode" as common fallback keys.
            val barcode = intent.getStringExtra("barcode_string")
                ?: intent.getStringExtra("data")
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
                addAction("com.honeywell.decodeservice.RESULT_TO_APP")
                addAction("com.tionix.rms.ACTION_BARCODE_DATA")
                addAction("android.intent.action.BARCODE_RESULT")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(scannerReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(scannerReceiver, filter)
            }
            isRegistered = true
            
            // Claim Honeywell imager via broadcast API
            sendScannerCommand("com.honeywell.aidc.action.ACTION_CLAIM_SCANNER", claim = true)
            // Apply current continuous mode configuration
            applyProperties()
        }
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
            
            // Release Honeywell imager
            sendScannerCommand("com.honeywell.aidc.action.ACTION_RELEASE_SCANNER", claim = false)
        }
    }

    override fun startScanTrigger() {
        _isScanning.value = true
        // Action to trigger hardware scan programmatically
        // Honeywell intent for software trigger control
        val intent = Intent("com.honeywell.aidc.action.ACTION_CONTROL_SCANNER").apply {
            putExtra("com.honeywell.aidc.extra.EXTRA_CONTROL", true)
            putExtra("packageName", context.packageName)
        }
        context.sendBroadcast(intent)
    }

    override fun stopScanTrigger() {
        _isScanning.value = false
        val intent = Intent("com.honeywell.aidc.action.ACTION_CONTROL_SCANNER").apply {
            putExtra("com.honeywell.aidc.extra.EXTRA_CONTROL", false)
            putExtra("packageName", context.packageName)
        }
        context.sendBroadcast(intent)
    }

    override fun setContinuousMode(enabled: Boolean) {
        continuousMode = enabled
        applyProperties()
    }

    private fun sendScannerCommand(action: String, claim: Boolean) {
        val intent = Intent(action).apply {
            putExtra("packageName", context.packageName)
        }
        context.sendBroadcast(intent)
    }

    private fun applyProperties() {
        // Honeywell supports setting properties via intents:
        // Action: "com.honeywell.aidc.action.ACTION_SET_PROPERTIES"
        // Extras package: "properties" Bundle containing property key-value pairs.
        val properties = Bundle().apply {
            putBoolean("DPR_DATA_INTENT", true)
            putString("DPR_DATA_INTENT_ACTION", "com.tionix.rms.ACTION_BARCODE_DATA")
            if (continuousMode) {
                // trigger_control_mode: "disable" (0), "one_shot" (1), "continuous" (2), "auto_control" (3)
                putString("property_trigger_control_mode", "continuous")
                putInt("trigger_control_mode", 2)
            } else {
                putString("property_trigger_control_mode", "one_shot")
                putInt("trigger_control_mode", 1)
            }
        }

        val intent = Intent("com.honeywell.aidc.action.ACTION_SET_PROPERTIES").apply {
            putExtra("properties", properties)
            putExtra("packageName", context.packageName)
        }
        context.sendBroadcast(intent)
    }
}
