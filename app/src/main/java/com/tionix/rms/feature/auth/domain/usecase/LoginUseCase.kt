package com.tionix.rms.feature.auth.domain.usecase

import com.tionix.rms.feature.auth.domain.model.AuthResult
import com.tionix.rms.feature.auth.domain.model.LoginRequest
import com.tionix.rms.feature.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(request: LoginRequest): AuthResult {
        return repository.login(request)
    }
}
