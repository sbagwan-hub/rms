package com.tionix.rms.feature.history.presentation

import com.tionix.rms.feature.history.domain.model.HistoryItem

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val history: List<HistoryItem>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}
