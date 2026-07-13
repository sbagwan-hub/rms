package com.tionix.rms.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tionix.rms.utils.scanner.ScannerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ScannerEffect(
    scannerManager: ScannerManager,
    continuousMode: Boolean = true,
    onBarcodeScanned: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe scanned results
    DisposableEffect(scannerManager) {
        val scope = CoroutineScope(Dispatchers.Main)
        val job = scope.launch {
            scannerManager.scanResults.collect { barcode ->
                onBarcodeScanned(barcode)
            }
        }
        onDispose {
            job.cancel()
        }
    }

    // Bind scanner lifecycle and configure properties
    DisposableEffect(lifecycleOwner, scannerManager, continuousMode) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    scannerManager.setContinuousMode(continuousMode)
                    scannerManager.enable()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    scannerManager.disable()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            scannerManager.disable()
        }
    }
}
