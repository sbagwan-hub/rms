package com.tionix.rms.feature.settings.data.repository

import com.tionix.rms.feature.settings.domain.model.Settings
import com.tionix.rms.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor() : SettingsRepository {
    private var currentSettings = Settings()

    override suspend fun getSettings(): Result<Settings> {
        return Result.success(currentSettings)
    }

    override suspend fun updateSettings(settings: Settings): Result<Unit> {
        currentSettings = settings
        return Result.success(Unit)
    }

    override suspend fun syncNow(): Result<Unit> {
        return Result.success(Unit)
    }
}
