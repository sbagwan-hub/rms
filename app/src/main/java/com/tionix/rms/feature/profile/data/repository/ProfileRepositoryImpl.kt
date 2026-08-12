package com.tionix.rms.feature.profile.data.repository

import android.os.Build
import android.provider.Settings
import com.tionix.rms.BuildConfig
import com.tionix.rms.core.sync.data.local.PendingOperationDao
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.auth.data.remote.AuthApiService
import com.tionix.rms.feature.auth.data.remote.dto.MeResponseDto
import com.tionix.rms.feature.profile.domain.model.UserProfile
import com.tionix.rms.feature.profile.domain.repository.ProfileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authPreferences: AuthPreferences,
    private val pendingOperationDao: PendingOperationDao,
    @ApplicationContext private val context: Context
) : ProfileRepository {

    override suspend fun getProfile(): Result<UserProfile> {
        return try {
            val response = authApiService.getMe()
            if (response.isSuccessful && response.body() != null) {
                val me = response.body()!!
                me.role?.permissions?.let { permissions ->
                    authPreferences.saveAuthSession(
                        accessToken = authPreferences.getAccessToken().orEmpty(),
                        refreshToken = authPreferences.getRefreshToken().orEmpty(),
                        userId = me.id,
                        fullName = me.fullName,
                        email = me.email,
                        role = me.role.name ?: authPreferences.getRole().orEmpty(),
                        permissions = permissions.toSet()
                    )
                }
                Result.success(mapProfile(me))
            } else {
                Result.success(fallbackProfile())
            }
        } catch (_: Exception) {
            Result.success(fallbackProfile())
        }
    }

    override suspend fun getPendingSyncCount(): Result<Int> {
        return try {
            Result.success(pendingOperationDao.getPendingCount())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val refreshToken = authPreferences.getRefreshToken()
            if (!refreshToken.isNullOrBlank()) {
                runCatching { authApiService.logout(com.tionix.rms.feature.auth.data.remote.dto.LogoutRequestDto(refreshToken)) }
            }
            authPreferences.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            authPreferences.clear()
            Result.success(Unit)
        }
    }

    private suspend fun fallbackProfile(): UserProfile {
        return UserProfile(
            id = authPreferences.getUserId().orEmpty(),
            fullName = authPreferences.getFullName().orEmpty(),
            username = authPreferences.getEmail().orEmpty(),
            role = authPreferences.getRole().orEmpty(),
            roleLabel = formatRole(authPreferences.getRole().orEmpty()),
            warehouses = emptyList(),
            deviceSerial = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            deviceModel = Build.MODEL,
            appVersion = BuildConfig.VERSION_NAME
        )
    }

    private fun mapProfile(me: MeResponseDto): UserProfile {
        val roleName = me.role?.name.orEmpty()
        return UserProfile(
            id = me.id,
            fullName = me.fullName,
            username = me.employeeCode ?: me.email,
            role = roleName,
            roleLabel = me.role?.label ?: formatRole(roleName),
            warehouses = me.warehouses.orEmpty(),
            deviceSerial = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            deviceModel = Build.MODEL,
            appVersion = BuildConfig.VERSION_NAME
        )
    }

    private fun formatRole(role: String): String =
        role.split('_').joinToString(" ") { part ->
            part.lowercase().replaceFirstChar { it.titlecase() }
        }
}
