package com.tionix.rms

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tionix.rms.feature.sync.data.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class RmsApplication : Application(), Configuration.Provider {

    // Without this, WorkManager uses its default reflection-based
    // WorkerFactory, which cannot construct @HiltWorker classes like
    // SyncWorker (they need injected dependencies via Hilt's assisted
    // injection). Every enqueued SyncWorker run was silently failing to
    // even instantiate — nothing was ever actually reaching the network.
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Manual init required now that the manifest's auto-initializer is
        // disabled (see AndroidManifest.xml) — this guarantees the
        // Hilt-aware factory is installed before anything can call
        // WorkManager.getInstance().
        WorkManager.initialize(this, workManagerConfiguration)
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
