package com.tionix.rms

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tionix.rms.feature.sync.data.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class RmsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicSync()
    }

    /**
     * Reliable fallback for the offline-sync queue: without this, nothing
     * ever enqueues [SyncWorker] — the only other trigger
     * (NetworkMonitor.observeNetworkStatus) is a cold Flow nobody collects.
     * 15 minutes is WorkManager's minimum period for periodic work.
     */
    private fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
