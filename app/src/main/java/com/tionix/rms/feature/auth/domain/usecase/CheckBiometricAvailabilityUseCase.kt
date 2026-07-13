package com.tionix.rms.feature.auth.domain.usecase

import com.tionix.rms.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class CheckBiometricAvailabilityUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(): Boolean {
        return repository.isBiometricAvailable()
    }
}
