package com.tionix.rms.feature.freshboxmove.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fresh_box_sessions")
data class FreshBoxSessionEntity(
    @PrimaryKey val clientSessionId: String,
    val serverSessionId: String?,
    val operatorId: String,
    val startedAt: Long,
    val endedAt: Long?,
    val deviceId: String?
)

@Entity(tableName = "fresh_box_scans")
data class FreshBoxScanEntity(
    @PrimaryKey val clientEventId: String,
    val clientSessionId: String,
    val boxBarcode: String,
    val locationBarcode: String,
    val scannedAt: Long,
    val gpsLat: Double?,
    val gpsLng: Double?,
    val isSynced: Boolean
)
