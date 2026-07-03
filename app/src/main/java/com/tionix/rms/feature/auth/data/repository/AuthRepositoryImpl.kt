package com.tionix.rms.feature.auth.data.repository

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.auth.data.remote.AuthApiService
import com.tionix.rms.feature.auth.data.remote.dto.LoginRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.toDomain
import com.tionix.rms.feature.auth.domain.model.AuthResult
import com.tionix.rms.feature.auth.domain.model.LoginRequest
import com.tionix.rms.feature.auth.domain.model.toDto
import com.tionix.rms.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val preferences: AuthPreferences,
    private val context: Context
) : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResult {
        return try {
            val response = apiService.login(request.toDto())
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                // Save to preferences
                preferences.accessToken = loginResponse.accessToken
                preferences.refreshToken = loginResponse.refreshToken
                preferences.userId = loginResponse.user.id
                preferences.username = loginResponse.user.username
                preferences.email = loginResponse.user.email
                preferences.companyId = loginResponse.user.companyId
                preferences.deviceId = loginResponse.user.deviceId
                
                AuthResult.Success(loginResponse.accessToken, loginResponse.refreshToken, loginResponse.user.toDomain())
            } else {
                AuthResult.Error("Login failed: ${response.message()}", response.code().toString())
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun loginWithBiometric(): AuthResult {
        // Biometric login requires UI context, so this is a simplified version
        // In production, this would need to be called from the UI layer with BiometricPrompt
        return if (preferences.isBiometricEnabled && preferences.accessToken != null) {
            AuthResult.Success(
                preferences.accessToken!!,
                preferences.refreshToken ?: "",
                com.tionix.rms.feature.auth.domain.model.User(
                    preferences.userId ?: "",
                    preferences.username ?: "",
                    preferences.email ?: "",
                    preferences.companyId ?: "",
                    preferences.deviceId ?: ""
                )
            )
        } else {
            AuthResult.Error("Biometric login not available")
        }
    }

    override suspend fun logout(): AuthResult {
        return try {
            preferences.clear()
            AuthResult.Success("", "", com.tionix.rms.feature.auth.domain.model.User("", "", "", "", ""))
        } catch (e: Exception) {
            AuthResult.Error("Logout failed: ${e.message}")
        }
    }

    override suspend fun refreshToken(): AuthResult {
        return try {
            val response = apiService.refreshToken()
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                preferences.accessToken = loginResponse.accessToken
                preferences.refreshToken = loginResponse.refreshToken
                AuthResult.Success(loginResponse.accessToken, loginResponse.refreshToken, loginResponse.user.toDomain())
            } else {
                AuthResult.Error("Token refresh failed")
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
