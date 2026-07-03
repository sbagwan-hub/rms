package com.tionix.rms.core.sync.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SyncOperationEntity::class],
    version = 1
)
abstract class SyncDatabase : RoomDatabase() {
    abstract fun syncOperationDao(): SyncOperationDao
}
