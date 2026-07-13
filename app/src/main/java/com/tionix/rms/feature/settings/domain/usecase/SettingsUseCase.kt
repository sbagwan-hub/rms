package com.tionix.rms.feature.settings.domain.usecase

import com.tionix.rms.feature.settings.domain.model.Settings
import com.tionix.rms.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Settings> {
        // TODO: BACKEND ENDPOINT PENDING - DataStore only for now
        return repository.getSettings()
    }
}

class UpdateSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(settings: Settings): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING - DataStore only for now
        return repository.updateSettings(settings)
    }
}

class SyncNowUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING - WorkManager enqueue only for now
        return repository.syncNow()
    }
}
