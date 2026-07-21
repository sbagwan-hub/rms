package com.tionix.rms.feature.auth.data.repository

import android.content.Context
import androidx.biometric.BiometricManager
import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.auth.data.remote.AuthApiService
import com.tionix.rms.feature.auth.data.remote.dto.toDomain
import com.tionix.rms.feature.auth.data.remote.dto.toDto
import com.tionix.rms.feature.auth.domain.model.AuthResult
import com.tionix.rms.feature.auth.domain.model.LoginRequest
import com.tionix.rms.feature.auth.domain.model.User
import com.tionix.rms.feature.auth.domain.model.UserRole
import com.tionix.rms.feature.auth.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val preferences: AuthPreferences,
    @ApplicationContext private val context: Context
) : AuthRepository {

    override suspend fun login(request: LoginRequest): AuthResult {
        return try {
            val response = apiService.login(request.toDto())
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                // Save to DataStore preferences
                preferences.saveAuthSession(
                    accessToken = loginResponse.accessToken,
                    refreshToken = loginResponse.refreshToken,
                    userId = loginResponse.user.id,
                    fullName = loginResponse.user.fullName,
                    email = loginResponse.user.email,
                    role = loginResponse.user.role
                )
                
                AuthResult.Success(
                    accessToken = loginResponse.accessToken,
                    refreshToken = loginResponse.refreshToken,
                    user = loginResponse.user.toDomain()
                )
            } else {
                val errorMsg = try {
                    val errorBodyStr = response.errorBody()?.string()
                    if (!errorBodyStr.isNullOrBlank()) {
                        val jsonObject = com.google.gson.JsonParser.parseString(errorBodyStr).asJsonObject
                        if (jsonObject.has("error")) {
                            jsonObject.getAsJsonObject("error").get("message").asString
                        } else {
                            "Login failed: ${response.message()}"
                        }
                    } else {
                        "Login failed: ${response.message()}"
                    }
                } catch (e: Exception) {
                    "Login failed: ${response.message()}"
                }
                AuthResult.Error(errorMsg, response.code().toString())
            }
        } catch (e: Exception) {
            AuthResult.Error(ErrorUtils.getFriendlyErrorMessage(e))
        }
    }

    override suspend fun loginWithBiometric(): AuthResult {
        return try {
            val isBiometricEnabled = preferences.isBiometricEnabled()
            val token = preferences.getAccessToken()
            
            if (isBiometricEnabled && token != null) {
                val userId = preferences.getUserId() ?: ""
                val fullName = preferences.getFullName() ?: ""
                val email = preferences.getEmail() ?: ""
                val roleStr = preferences.getRole() ?: "OPERATOR"
                val role = try {
                    UserRole.valueOf(roleStr)
                } catch (e: Exception) {
                    UserRole.OPERATOR
                }
                
                AuthResult.Success(
                    accessToken = token,
                    refreshToken = preferences.getRefreshToken() ?: "",
                    user = User(
                        id = userId,
                        fullName = fullName,
                        email = email,
                        role = role
                    )
                )
            } else {
                AuthResult.Error("Biometric login not available")
            }
        } catch (e: Exception) {
            AuthResult.Error(ErrorUtils.getFriendlyErrorMessage(e))
        }
    }

    override suspend fun logout(): AuthResult {
        return try {
            preferences.clear()
            AuthResult.Success("", "", User("", "", "", UserRole.OPERATOR))
        } catch (e: Exception) {
            AuthResult.Error(ErrorUtils.getFriendlyErrorMessage(e))
        }
    }

    override suspend fun refreshToken(): AuthResult {
        return try {
            val response = apiService.refreshToken()
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                preferences.setAccessToken(loginResponse.accessToken)
                preferences.setRefreshToken(loginResponse.refreshToken)
                AuthResult.Success(
                    accessToken = loginResponse.accessToken,
                    refreshToken = loginResponse.refreshToken,
                    user = loginResponse.user.toDomain()
                )
            } else {
                AuthResult.Error("Token refresh failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(ErrorUtils.getFriendlyErrorMessage(e))
        }
    }

    override suspend fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
