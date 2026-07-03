package com.tionix.rms.core.sync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOperationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: SyncOperationEntity)
    
    @Query("SELECT * FROM sync_operations WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<SyncOperationEntity>
    
    @Query("SELECT * FROM sync_operations WHERE status = 'FAILED' ORDER BY createdAt ASC")
    suspend fun getFailedOperations(): List<SyncOperationEntity>
    
    @Query("SELECT * FROM sync_operations")
    suspend fun getAllOperations(): List<SyncOperationEntity>
    
    @Query("UPDATE sync_operations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
    
    @Query("UPDATE sync_operations SET retryCount = retryCount + 1, errorMessage = :error WHERE id = :id")
    suspend fun incrementRetryCount(id: String, error: String?)
    
    @Query("DELETE FROM sync_operations WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()
    
    @Query("DELETE FROM sync_operations WHERE id = :id")
    suspend fun delete(id: String)
    
    @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int
    
    @Query("SELECT COUNT(*) FROM sync_operations WHERE status = 'FAILED'")
    suspend fun getFailedCount(): Int
}
