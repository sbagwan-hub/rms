package com.tionix.rms.feature.settings.domain.repository

import com.tionix.rms.feature.settings.domain.model.Settings

interface SettingsRepository {
    suspend fun getSettings(): Result<Settings>
    suspend fun updateSettings(settings: Settings): Result<Unit>
    suspend fun syncNow(): Result<Unit>
}
