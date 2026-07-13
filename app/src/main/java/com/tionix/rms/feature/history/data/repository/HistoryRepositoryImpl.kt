package com.tionix.rms.feature.history.data.repository

import com.tionix.rms.feature.history.domain.model.ActionFilter
import com.tionix.rms.feature.history.domain.model.DateFilter
import com.tionix.rms.feature.history.domain.model.HistoryItem
import com.tionix.rms.feature.history.domain.repository.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor() : HistoryRepository {
    override suspend fun getHistory(
        actionFilter: ActionFilter,
        dateFilter: DateFilter,
        userId: String?
    ): Result<List<HistoryItem>> {
        return Result.success(emptyList())
    }

    override suspend fun retrySync(historyItemId: String): Result<Unit> {
        return Result.success(Unit)
    }
}
