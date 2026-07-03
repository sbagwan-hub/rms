package com.tionix.rms.feature.merge.presentation

import com.tionix.rms.feature.merge.domain.model.Merge

sealed class MergeUiState {
    object Loading : MergeUiState()
    data class Success(val merges: List<Merge>) : MergeUiState()
    data class Error(val message: String) : MergeUiState()
    data class BoxScanned(val merge: Merge?) : MergeUiState()
    object MergeStarted : MergeUiState()
    object MergeCompleted : MergeUiState()
}
