package com.tionix.rms.core.sync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingOperationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(operation: PendingOperationEntity)

    @Query(
        """
        SELECT * FROM pending_operations
        WHERE state IN ('QUEUED', 'FAILED')
        ORDER BY createdAt ASC
        """
    )
    suspend fun getPending(): List<PendingOperationEntity>

    @Query("DELETE FROM pending_operations WHERE clientOpId = :clientOpId")
    suspend fun delete(clientOpId: String)

    @Query(
        """
        UPDATE pending_operations
        SET state = :state, lastError = :error, attemptCount = :attemptCount
        WHERE clientOpId = :clientOpId
        """
    )
    suspend fun updateState(
        clientOpId: String,
        state: String,
        error: String?,
        attemptCount: Int
    )

    @Query("SELECT COUNT(*) FROM pending_operations WHERE state IN ('QUEUED', 'FAILED')")
    suspend fun getPendingCount(): Int

    @Query(
        """
        SELECT * FROM pending_operations
        WHERE state IN ('QUEUED', 'FAILED', 'SENDING')
        ORDER BY createdAt ASC
        """
    )
    fun observePending(): kotlinx.coroutines.flow.Flow<List<PendingOperationEntity>>
}
