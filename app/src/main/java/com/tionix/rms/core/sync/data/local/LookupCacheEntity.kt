package com.tionix.rms.core.sync.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lookup_cache")
data class LookupCacheEntity(
    @PrimaryKey val barcode: String,
    val payloadJson: String,
    val cachedAt: Long
)
