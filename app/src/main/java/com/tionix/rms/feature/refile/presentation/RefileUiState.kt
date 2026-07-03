package com.tionix.rms.feature.refile.presentation

import com.tionix.rms.feature.refile.domain.model.Refile

sealed class RefileUiState {
    object Loading : RefileUiState()
    data class Success(val refiles: List<Refile>) : RefileUiState()
    data class Error(val message: String) : RefileUiState()
    data class FileScanned(val refile: Refile?) : RefileUiState()
    object RefileStarted : RefileUiState()
    object RefileCompleted : RefileUiState()
}
