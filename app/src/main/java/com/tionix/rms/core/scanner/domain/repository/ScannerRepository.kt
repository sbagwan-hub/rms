package com.tionix.rms.core.scanner.domain.repository

import com.tionix.rms.core.scanner.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

interface ScannerRepository {
    val scanResults: Flow<ScanResult>
    
    suspend fun initializeScanner(): Result<Unit>
    suspend fun startScanning(): Result<Unit>
    suspend fun stopScanning(): Result<Unit>
    suspend fun isScannerAvailable(): Result<Boolean>
    fun startCameraScan(context: android.content.Context, onScanResult: ((String) -> Unit)? = null)
    fun enableScanner()
    fun disableScanner()
}
