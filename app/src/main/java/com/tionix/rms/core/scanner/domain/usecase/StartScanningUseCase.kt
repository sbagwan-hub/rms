package com.tionix.rms.core.scanner.domain.usecase

import com.tionix.rms.core.scanner.domain.repository.ScannerRepository

class InitializeScannerUseCase(private val repository: ScannerRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.initializeScanner()
    }
}

class StartScanningUseCase(private val repository: ScannerRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.startScanning()
    }
}

class StopScanningUseCase(private val repository: ScannerRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.stopScanning()
    }
}

class CheckScannerAvailabilityUseCase(private val repository: ScannerRepository) {
    suspend operator fun invoke(): Result<Boolean> {
        return repository.isScannerAvailable()
    }
}
