package com.tionix.rms.feature.auth.data.repository

import android.content.Context
import androidx.biometric.BiometricManager
import com.tionix.rms.core.network.ErrorUtils
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.auth.data.remote.AuthApiService
import com.tionix.rms.feature.auth.data.remote.dto.LogoutRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.RefreshRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.SwitchBranchRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.SwitchWarehouseRequestDto
import com.tionix.rms.feature.auth.data.remote.dto.persistFields
import com.tionix.rms.feature.auth.data.remote.dto.persistSessionPayload
import com.tionix.rms.feature.auth.data.remote.dto.toDto
import com.tionix.rms.feature.auth.data.remote.dto.toDomain
import com.tionix.rms.feature.auth.data.remote.dto.toSession
import com.tionix.rms.feature.auth.domain.model.AuthResult
import com.tionix.rms.feature.auth.domain.model.AuthSession
import com.tionix.rms.feature.auth.domain.model.EntityRef
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
                val session = response.body()!!.toSession()
                preferences.persistSessionPayload(session.persistFields())
                AuthResult.Success(session.accessToken, session.refreshToken, session.user)
            } else {
                AuthResult.Error(parseError(response), response.code().toString())
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
                val cached = getCachedSession()
                if (cached != null) {
                    AuthResult.Success(cached.accessToken, cached.refreshToken, cached.user)
                } else {
                    AuthResult.Error("Biometric login not available")
                }
            } else {
                AuthResult.Error("Biometric login not available")
            }
        } catch (e: Exception) {
            AuthResult.Error(ErrorUtils.getFriendlyErrorMessage(e))
        }
    }

    override suspend fun logout(): AuthResult {
        return try {
            val refreshToken = preferences.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                runCatching { apiService.logout(LogoutRequestDto(refreshToken)) }
            }
            preferences.clear()
            AuthResult.Success("", "", User("", "", "", UserRole.OPERATOR))
        } catch (e: Exception) {
            preferences.clear()
            AuthResult.Success("", "", User("", "", "", UserRole.OPERATOR))
        }
    }

    override suspend fun refreshToken(refreshToken: String): AuthResult {
        return try {
            val response = apiService.refreshToken(RefreshRequestDto(refreshToken))
            if (response.isSuccessful && response.body() != null) {
                val existing = getCachedSession()
                val session = response.body()!!.toSession(existing)
                preferences.persistSessionPayload(session.persistFields())
                AuthResult.Success(session.accessToken, session.refreshToken, session.user)
            } else {
                AuthResult.Error("Token refresh failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(ErrorUtils.getFriendlyErrorMessage(e))
        }
    }

    override suspend fun hydrateSessionFromMe(): Result<AuthSession> {
        return try {
            val accessToken = preferences.getAccessToken().orEmpty()
            val refreshToken = preferences.getRefreshToken().orEmpty()
            val response = apiService.getMe()
            if (response.isSuccessful && response.body() != null) {
                val existing = getCachedSession()
                val session = response.body()!!.toSession(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    existingWarehouses = existing?.availableWarehouses.orEmpty(),
                    existingBranches = existing?.availableBranches.orEmpty(),
                    existingCompanies = existing?.availableCompanies.orEmpty()
                )
                preferences.persistSessionPayload(session.persistFields())
                Result.success(session)
            } else {
                Result.failure(Exception("Failed to load session profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun switchWarehouse(warehouseId: String): AuthResult {
        return try {
            val response = apiService.switchWarehouse(SwitchWarehouseRequestDto(warehouseId))
            if (response.isSuccessful && response.body() != null) {
                val session = response.body()!!.toSession()
                preferences.persistSessionPayload(session.persistFields())
                AuthResult.Success(session.accessToken, session.refreshToken, session.user)
            } else {
                AuthResult.Error(parseError(response), response.code().toString())
            }
        } catch (e: Exception) {
            AuthResult.Error(ErrorUtils.getFriendlyErrorMessage(e))
        }
    }

    override suspend fun switchBranch(branchId: String): AuthResult {
        return try {
            val response = apiService.switchBranch(SwitchBranchRequestDto(branchId))
            if (response.isSuccessful && response.body() != null) {
                val session = response.body()!!.toSession()
                preferences.persistSessionPayload(session.persistFields())
                AuthResult.Success(session.accessToken, session.refreshToken, session.user)
            } else {
                AuthResult.Error(parseError(response), response.code().toString())
            }
        } catch (e: Exception) {
            AuthResult.Error(ErrorUtils.getFriendlyErrorMessage(e))
        }
    }

    override suspend fun getCachedSession(): AuthSession? {
        val accessToken = preferences.getAccessToken() ?: return null
        val userId = preferences.getUserId() ?: return null
        val roleStr = preferences.getRole() ?: "OPERATOR"
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            UserRole.OPERATOR
        }

        val companyId = preferences.getCompanyId()
        val company = if (companyId != null) {
            EntityRef(companyId, preferences.getCompanyName().orEmpty())
        } else null

        val branchId = preferences.getBranchId()
        val branch = if (branchId != null) {
            EntityRef(branchId, preferences.getBranchName().orEmpty())
        } else null

        val warehouseId = preferences.getWarehouseId()
        val warehouse = if (warehouseId != null) {
            EntityRef(
                warehouseId,
                preferences.getWarehouseName().orEmpty(),
                preferences.getWarehouseCode()
            )
        } else null

        return AuthSession(
            accessToken = accessToken,
            refreshToken = preferences.getRefreshToken().orEmpty(),
            expiresAt = preferences.getExpiresAt(),
            user = User(
                id = userId,
                fullName = preferences.getFullName().orEmpty(),
                email = preferences.getEmail().orEmpty(),
                role = role
            ),
            company = company,
            branch = branch,
            warehouse = warehouse,
            permissions = preferences.getPermissions(),
            availableWarehouses = preferences.getAvailableWarehouses(),
            availableBranches = preferences.getAvailableBranches(),
            availableCompanies = preferences.getAvailableCompanies()
        )
    }

    override suspend fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorBodyStr = response.errorBody()?.string()
            if (!errorBodyStr.isNullOrBlank()) {
                val jsonObject = com.google.gson.JsonParser.parseString(errorBodyStr).asJsonObject
                if (jsonObject.has("error")) {
                    jsonObject.getAsJsonObject("error").get("message").asString
                } else {
                    "Request failed: ${response.message()}"
                }
            } else {
                "Request failed: ${response.message()}"
            }
        } catch (e: Exception) {
            "Request failed: ${response.message()}"
        }
    }
}
