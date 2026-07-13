package com.tionix.rms.feature.freshboxmove.domain.repository

import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxScanEntity
import com.tionix.rms.feature.freshboxmove.data.local.FreshBoxSessionEntity
import kotlinx.coroutines.flow.Flow

interface FreshBoxMoveRepository {
    /**
     * Start a new Fresh Box Move Session.
     */
    suspend fun startSession(deviceId: String?): Result<FreshBoxSessionEntity>

    /**
     * Scan and move a Box to a Location, saving locally to Room and enqueuing in Pending Sync queue.
     */
    suspend fun submitScan(
        boxBarcode: String,
        locationBarcode: String,
        gpsLat: Double?,
        gpsLng: Double?
    ): Result<FreshBoxScanEntity>

    /**
     * End the current active session.
     */
    suspend fun endSession(): Result<Unit>

    /**
     * Get the active session if one exists.
     */
    suspend fun getActiveSession(): FreshBoxSessionEntity?

    /**
     * Retrieve a reactive stream of scans in the specified session.
     */
    fun getScansForSessionFlow(sessionId: String): Flow<List<FreshBoxScanEntity>>
}
