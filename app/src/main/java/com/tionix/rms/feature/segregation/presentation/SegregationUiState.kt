package com.tionix.rms.feature.segregation.presentation

import com.tionix.rms.feature.segregation.domain.model.Segregation

sealed class SegregationUiState {
    object Loading : SegregationUiState()
    data class Success(val segregations: List<Segregation>) : SegregationUiState()
    data class Error(val message: String) : SegregationUiState()
    data class BoxScanned(val segregation: Segregation?) : SegregationUiState()
    object SegregationStarted : SegregationUiState()
    object SegregationCompleted : SegregationUiState()
    object SessionStarted : SegregationUiState()
    data class ValidationError(val message: String) : SegregationUiState()
    data class FileMoved(val fileBarcode: String) : SegregationUiState()
}
