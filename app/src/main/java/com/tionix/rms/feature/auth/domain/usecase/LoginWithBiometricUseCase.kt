package com.tionix.rms.feature.auth.domain.usecase

import com.tionix.rms.feature.auth.domain.model.AuthResult
import com.tionix.rms.feature.auth.domain.repository.AuthRepository

class LoginWithBiometricUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AuthResult {
        return repository.loginWithBiometric()
    }
}
