package com.tionix.rms.feature.history.domain.model

data class HistoryItem(
    val id: String,
    val actionType: ActionType,
    val itemId: String,
    val itemName: String?,
    val userId: String,
    val userName: String,
    val timestamp: String,
    val syncStatus: SyncStatus,
    val details: String?
)

enum class ActionType {
    FRESH_BOX_MOVE,
    REFILE,
    TRANSFER,
    SEGREGATION,
    MERGE,
    VERIFICATION,
    OTHER
}

enum class SyncStatus {
    SYNCED,
    PENDING,
    FAILED
}

enum class DateFilter {
    TODAY,
    SEVEN_DAYS,
    THIRTY_DAYS,
    ALL
}

enum class ActionFilter {
    ALL,
    FRESH_BOX,
    REFILE,
    TRANSFER,
    SEGREGATION,
    MERGE,
    VERIFICATION
}
