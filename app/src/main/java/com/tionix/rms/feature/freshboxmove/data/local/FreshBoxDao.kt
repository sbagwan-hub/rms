package com.tionix.rms.feature.freshboxmove.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FreshBoxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FreshBoxSessionEntity)

    @Query("SELECT * FROM fresh_box_sessions WHERE clientSessionId = :clientSessionId")
    suspend fun getSession(clientSessionId: String): FreshBoxSessionEntity?

    @Query("SELECT * FROM fresh_box_sessions WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSession(): FreshBoxSessionEntity?

    @Query("UPDATE fresh_box_sessions SET endedAt = :endedAt WHERE clientSessionId = :clientSessionId")
    suspend fun endSession(clientSessionId: String, endedAt: Long)

    @Query("UPDATE fresh_box_sessions SET serverSessionId = :serverId WHERE clientSessionId = :clientSessionId")
    suspend fun updateServerSessionId(clientSessionId: String, serverId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: FreshBoxScanEntity)

    @Query("SELECT * FROM fresh_box_scans WHERE clientSessionId = :clientSessionId ORDER BY scannedAt DESC")
    fun getScansForSessionFlow(clientSessionId: String): Flow<List<FreshBoxScanEntity>>

    @Query("SELECT * FROM fresh_box_scans WHERE clientSessionId = :clientSessionId ORDER BY scannedAt DESC")
    suspend fun getScansForSession(clientSessionId: String): List<FreshBoxScanEntity>

    @Query("SELECT * FROM fresh_box_scans WHERE isSynced = 0")
    suspend fun getUnsyncedScans(): List<FreshBoxScanEntity>

    @Query("UPDATE fresh_box_scans SET isSynced = 1 WHERE clientEventId = :clientEventId")
    suspend fun markScanAsSynced(clientEventId: String)

    @Query("SELECT COUNT(*) FROM fresh_box_scans WHERE clientSessionId = :clientSessionId AND boxBarcode = :boxBarcode")
    suspend fun getBoxCountInSession(clientSessionId: String, boxBarcode: String): Int

    @Query("DELETE FROM fresh_box_scans WHERE clientSessionId = :clientSessionId")
    suspend fun clearScansForSession(clientSessionId: String)
}
