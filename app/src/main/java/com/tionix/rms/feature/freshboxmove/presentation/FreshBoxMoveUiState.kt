package com.tionix.rms.feature.freshboxmove.presentation

import com.tionix.rms.feature.freshboxmove.domain.model.FreshBoxMove

sealed class FreshBoxMoveUiState {
    object Loading : FreshBoxMoveUiState()
    data class Success(val moves: List<FreshBoxMove>) : FreshBoxMoveUiState()
    data class Error(val message: String) : FreshBoxMoveUiState()
    data class BoxScanned(val move: FreshBoxMove?) : FreshBoxMoveUiState()
    object MoveStarted : FreshBoxMoveUiState()
    object MoveCompleted : FreshBoxMoveUiState()
}
