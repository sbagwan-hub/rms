package com.tionix.rms.feature.auth.domain.repository

import com.tionix.rms.feature.auth.domain.model.AuthResult
import com.tionix.rms.feature.auth.domain.model.AuthSession
import com.tionix.rms.feature.auth.domain.model.LoginRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthResult
    suspend fun loginWithBiometric(): AuthResult
    suspend fun logout(): AuthResult
    suspend fun refreshToken(refreshToken: String): AuthResult
    suspend fun hydrateSessionFromMe(): Result<AuthSession>
    suspend fun switchWarehouse(warehouseId: String): AuthResult
    suspend fun switchBranch(branchId: String): AuthResult
    suspend fun getCachedSession(): AuthSession?
    suspend fun isBiometricAvailable(): Boolean
}
