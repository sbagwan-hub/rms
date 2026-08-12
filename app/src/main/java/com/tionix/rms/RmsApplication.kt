package com.tionix.rms

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.tionix.rms.feature.sync.data.SyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RmsApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var syncScheduler: SyncScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        try {
            androidx.work.WorkManager.initialize(this, workManagerConfiguration)
            Log.d("RmsApplication", "WorkManager initialized with Hilt worker factory")
        } catch (e: Exception) {
            Log.e("RmsApplication", "WorkManager.initialize() failed", e)
        }
        syncScheduler.schedulePeriodicSync()
    }
}
