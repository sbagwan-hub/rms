package com.tionix.rms.feature.settings.domain.repository

import com.tionix.rms.feature.settings.domain.model.Settings

interface SettingsRepository {
    suspend fun getSettings(): Result<Settings>
    suspend fun updateSyncOnCellular(enabled: Boolean): Result<Unit>
    suspend fun updateSoundMuted(muted: Boolean): Result<Unit>
    suspend fun updateServerUrl(url: String): Result<Unit>
    suspend fun clearLookupCache(): Result<Unit>
    suspend fun syncNow(): Result<Unit>
}
