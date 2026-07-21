package com.tionix.rms.feature.inventory.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_verification_sessions")
data class InventoryVerificationSessionEntity(
    @PrimaryKey val clientSessionId: String,
    val serverSessionId: String?,
    val boxId: String,
    val boxBarcode: String,
    val operatorId: String,
    val startedAt: Long,
    val endedAt: Long?,
    val missingFileCount: Int,
    val unexpectedFileCount: Int
)

@Entity(tableName = "inventory_verification_scans")
data class InventoryVerificationScanEntity(
    @PrimaryKey val clientEventId: String,
    val clientSessionId: String,
    val fileBarcode: String,
    val scannedAt: Long,
    val isExpected: Boolean,
    val isSynced: Boolean
)

@Entity(tableName = "inventory_expected_files")
data class InventoryExpectedFileEntity(
    @PrimaryKey val id: String,
    val boxId: String,
    val barcode: String,
    val title: String,
    val status: String
)
