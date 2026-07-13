package com.tionix.rms.core.scanner.data.repository

import android.content.Context
import com.tionix.rms.core.scanner.domain.model.ScanResult
import com.tionix.rms.core.scanner.domain.model.ScanType
import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import com.tionix.rms.utils.scanner.ScannerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scannerManager: ScannerManager
) : ScannerRepository {

    override val scanResults: Flow<ScanResult> = scannerManager.scanResults.map { barcode ->
        ScanResult(
            barcode = barcode,
            scanType = ScanType.BARCODE,
            timestamp = System.currentTimeMillis(),
            rawData = barcode
        )
    }

    override suspend fun initializeScanner(): Result<Unit> {
        scannerManager.enable()
        return Result.success(Unit)
    }

    override suspend fun startScanning(): Result<Unit> {
        scannerManager.startScanTrigger()
        return Result.success(Unit)
    }

    override suspend fun stopScanning(): Result<Unit> {
        scannerManager.stopScanTrigger()
        return Result.success(Unit)
    }

    override suspend fun isScannerAvailable(): Result<Boolean> {
        return Result.success(true)
    }
}
