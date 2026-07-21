package com.tionix.rms.feature.inventory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        InventoryVerificationSessionEntity::class,
        InventoryVerificationScanEntity::class,
        InventoryExpectedFileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
}
