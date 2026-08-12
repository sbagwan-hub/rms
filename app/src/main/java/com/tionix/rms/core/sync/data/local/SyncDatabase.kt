package com.tionix.rms.core.sync.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SyncOperationEntity::class, PendingOperationEntity::class, LookupCacheEntity::class],
    version = 3
)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun syncOperationDao(): SyncOperationDao
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun lookupCacheDao(): LookupCacheDao
}
