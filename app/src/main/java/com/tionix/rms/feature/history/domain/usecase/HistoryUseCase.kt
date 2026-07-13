package com.tionix.rms.feature.history.domain.usecase

import com.tionix.rms.feature.history.domain.model.ActionFilter
import com.tionix.rms.feature.history.domain.model.DateFilter
import com.tionix.rms.feature.history.domain.model.HistoryItem
import com.tionix.rms.feature.history.domain.repository.HistoryRepository
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(
        actionFilter: ActionFilter,
        dateFilter: DateFilter,
        userId: String? = null
    ): Result<List<HistoryItem>> {
        // TODO: BACKEND ENDPOINT PENDING - Room local data only for now
        return repository.getHistory(actionFilter, dateFilter, userId)
    }
}

class RetrySyncUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(historyItemId: String): Result<Unit> {
        // TODO: BACKEND ENDPOINT PENDING
        return repository.retrySync(historyItemId)
    }
}
