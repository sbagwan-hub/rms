package com.tionix.rms.feature.freshboxmove.presentation

sealed class FreshBoxMoveUiState {
    object Idle : FreshBoxMoveUiState()
    object Loading : FreshBoxMoveUiState()
    object ActiveSession : FreshBoxMoveUiState()
    data class Error(val message: String) : FreshBoxMoveUiState()
}
