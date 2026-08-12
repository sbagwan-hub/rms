package com.tionix.rms.feature.settings.domain.usecase

import com.tionix.rms.feature.settings.domain.model.Settings
import com.tionix.rms.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Settings> = repository.getSettings()
}

class UpdateSyncOnCellularUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> =
        repository.updateSyncOnCellular(enabled)
}

class UpdateServerUrlUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(url: String): Result<Unit> =
        repository.updateServerUrl(url)
}

class ClearLookupCacheUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.clearLookupCache()
}

class SyncNowUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.syncNow()
}
