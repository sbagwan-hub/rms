package com.tionix.rms.di

import android.content.Context
import androidx.room.Room
import com.tionix.rms.core.sync.data.local.SyncDatabase
import com.tionix.rms.core.sync.data.local.SyncOperationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSyncDatabase(@ApplicationContext context: Context): SyncDatabase =
        Room.databaseBuilder(context, SyncDatabase::class.java, "rms_sync.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideSyncOperationDao(database: SyncDatabase): SyncOperationDao =
        database.syncOperationDao()

    @Provides
    @Singleton
    fun provideFreshBoxDatabase(@ApplicationContext context: Context): com.tionix.rms.feature.freshboxmove.data.local.FreshBoxDatabase =
        Room.databaseBuilder(context, com.tionix.rms.feature.freshboxmove.data.local.FreshBoxDatabase::class.java, "rms_fresh_box.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFreshBoxDao(database: com.tionix.rms.feature.freshboxmove.data.local.FreshBoxDatabase): com.tionix.rms.feature.freshboxmove.data.local.FreshBoxDao =
        database.freshBoxDao()
}
