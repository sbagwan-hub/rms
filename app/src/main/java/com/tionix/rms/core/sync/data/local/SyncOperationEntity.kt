package com.tionix.rms.core.sync.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_operations")
data class SyncOperationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val endpoint: String,
    val payload: String,
    val status: String,
    val retryCount: Int,
    val createdAt: String,
    val errorMessage: String?
)
