package com.tionix.rms.feature.history.presentation

import com.tionix.rms.feature.history.domain.model.PendingOperationItem
import com.tionix.rms.feature.history.domain.model.SyncedOperationItem

data class HistoryUiState(
    val pendingOps: List<PendingOperationItem> = emptyList(),
    val syncedOps: List<SyncedOperationItem> = emptyList(),
    val loadingSynced: Boolean = false,
    val syncedError: String? = null,
    val selectedTab: HistoryTab = HistoryTab.PENDING
)

enum class HistoryTab {
    PENDING,
    SYNCED
}
