package com.tionix.rms.core.scanner.domain.usecase

import com.tionix.rms.core.scanner.domain.repository.ScannerRepository
import javax.inject.Inject

class InitializeScannerUseCase @Inject constructor(private val repository: ScannerRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.initializeScanner()
    }
}

class StartScanningUseCase @Inject constructor(private val repository: ScannerRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.startScanning()
    }
}

class StopScanningUseCase @Inject constructor(private val repository: ScannerRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.stopScanning()
    }
}

class CheckScannerAvailabilityUseCase @Inject constructor(private val repository: ScannerRepository) {
    suspend operator fun invoke(): Result<Boolean> {
        return repository.isScannerAvailable()
    }
}
