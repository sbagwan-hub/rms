package com.tionix.rms.feature.refile.presentation

import com.tionix.rms.feature.refile.domain.model.FileRecord
import com.tionix.rms.feature.refile.domain.model.Refile

sealed class RefileUiState {
    object Loading : RefileUiState()
    data class Success(val refiles: List<Refile>) : RefileUiState()
    data class Error(val message: String) : RefileUiState()
    data class FileScanned(val fileRecord: FileRecord?) : RefileUiState()
    object RefileStarted : RefileUiState()
    object RefileCompleted : RefileUiState()
    object SessionStarted : RefileUiState()
    object SessionEnded : RefileUiState()
    data class MismatchDetected(val canOverride: Boolean) : RefileUiState()
}
