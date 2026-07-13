package com.tionix.rms.feature.history.domain.repository

import com.tionix.rms.feature.history.domain.model.ActionFilter
import com.tionix.rms.feature.history.domain.model.DateFilter
import com.tionix.rms.feature.history.domain.model.HistoryItem

interface HistoryRepository {
    suspend fun getHistory(
        actionFilter: ActionFilter,
        dateFilter: DateFilter,
        userId: String? = null // If null, get all (for SUPERVISOR/MANAGER)
    ): Result<List<HistoryItem>>
    
    suspend fun retrySync(historyItemId: String): Result<Unit>
}
