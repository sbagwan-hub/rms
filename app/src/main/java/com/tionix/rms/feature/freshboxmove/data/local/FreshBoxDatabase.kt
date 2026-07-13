package com.tionix.rms.feature.freshboxmove.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FreshBoxSessionEntity::class, FreshBoxScanEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FreshBoxDatabase : RoomDatabase() {
    abstract fun freshBoxDao(): FreshBoxDao
}
