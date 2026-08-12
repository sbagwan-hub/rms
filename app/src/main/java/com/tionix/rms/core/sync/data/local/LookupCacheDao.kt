package com.tionix.rms.core.sync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LookupCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LookupCacheEntity)

    @Query("DELETE FROM lookup_cache")
    suspend fun clearAll()
}
