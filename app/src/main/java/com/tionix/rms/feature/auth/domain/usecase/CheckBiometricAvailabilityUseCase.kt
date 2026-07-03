package com.tionix.rms.feature.auth.domain.usecase

import com.tionix.rms.feature.auth.domain.repository.AuthRepository

class CheckBiometricAvailabilityUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isBiometricAvailable()
    }
}
