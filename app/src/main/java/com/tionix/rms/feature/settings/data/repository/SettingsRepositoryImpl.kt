package com.tionix.rms.feature.settings.data.repository

import android.content.Context
import com.tionix.rms.core.settings.AppSettingsStore
import com.tionix.rms.core.sync.data.local.LookupCacheDao
import com.tionix.rms.feature.settings.domain.model.Settings
import com.tionix.rms.feature.settings.domain.repository.SettingsRepository
import com.tionix.rms.feature.sync.data.SyncScheduler
import com.tionix.rms.scanner.ScannerAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val appSettingsStore: AppSettingsStore,
    private val lookupCacheDao: LookupCacheDao,
    private val syncScheduler: SyncScheduler,
    @ApplicationContext private val context: Context
) : SettingsRepository {

    override suspend fun getSettings(): Result<Settings> {
        val mode = ScannerAvailability.detect(context)
        return Result.success(
            Settings(
                syncOnCellular = appSettingsStore.isSyncOnCellular(),
                serverUrl = appSettingsStore.getServerUrl(),
                scannerModeLabel = if (mode == ScannerAvailability.Mode.HONEYWELL_IMAGER) {
                    "Honeywell Imager"
                } else {
                    "Camera (fallback)"
                }
            )
        )
    }

    override suspend fun updateSyncOnCellular(enabled: Boolean): Result<Unit> {
        appSettingsStore.setSyncOnCellular(enabled)
        return Result.success(Unit)
    }

    override suspend fun updateServerUrl(url: String): Result<Unit> {
        appSettingsStore.setServerUrl(url)
        return Result.success(Unit)
    }

    override suspend fun clearLookupCache(): Result<Unit> {
        lookupCacheDao.clearAll()
        return Result.success(Unit)
    }

    override suspend fun syncNow(): Result<Unit> {
        syncScheduler.scheduleImmediateSync()
        return Result.success(Unit)
    }
}
