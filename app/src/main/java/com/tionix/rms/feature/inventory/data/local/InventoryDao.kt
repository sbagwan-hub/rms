package com.tionix.rms.feature.inventory.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: InventoryVerificationSessionEntity)

    @Query("SELECT * FROM inventory_verification_sessions WHERE clientSessionId = :clientSessionId")
    suspend fun getSession(clientSessionId: String): InventoryVerificationSessionEntity?

    @Query("SELECT * FROM inventory_verification_sessions WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSession(): InventoryVerificationSessionEntity?

    @Query("UPDATE inventory_verification_sessions SET endedAt = :endedAt, missingFileCount = :missingCount, unexpectedFileCount = :unexpectedCount WHERE clientSessionId = :clientSessionId")
    suspend fun endSession(clientSessionId: String, endedAt: Long, missingCount: Int, unexpectedCount: Int)

    @Query("UPDATE inventory_verification_sessions SET serverSessionId = :serverId WHERE clientSessionId = :clientSessionId")
    suspend fun updateServerSessionId(clientSessionId: String, serverId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: InventoryVerificationScanEntity)

    @Query("SELECT * FROM inventory_verification_scans WHERE clientSessionId = :clientSessionId ORDER BY scannedAt DESC")
    fun getScansForSessionFlow(clientSessionId: String): Flow<List<InventoryVerificationScanEntity>>

    @Query("SELECT * FROM inventory_verification_scans WHERE clientSessionId = :clientSessionId ORDER BY scannedAt DESC")
    suspend fun getScansForSession(clientSessionId: String): List<InventoryVerificationScanEntity>

    @Query("SELECT * FROM inventory_verification_scans WHERE isSynced = 0")
    suspend fun getUnsyncedScans(): List<InventoryVerificationScanEntity>

    @Query("UPDATE inventory_verification_scans SET isSynced = 1 WHERE clientEventId = :clientEventId")
    suspend fun markScanAsSynced(clientEventId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpectedFiles(files: List<InventoryExpectedFileEntity>)

    @Query("SELECT * FROM inventory_expected_files WHERE boxId = :boxId")
    suspend fun getExpectedFiles(boxId: String): List<InventoryExpectedFileEntity>

    @Query("DELETE FROM inventory_expected_files WHERE boxId = :boxId")
    suspend fun clearExpectedFiles(boxId: String)
}
