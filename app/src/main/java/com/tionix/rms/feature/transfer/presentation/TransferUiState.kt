package com.tionix.rms.feature.transfer.presentation

import com.tionix.rms.feature.transfer.domain.model.Transfer

sealed class TransferUiState {
    object Loading : TransferUiState()
    data class Success(val transfers: List<Transfer>) : TransferUiState()
    data class Error(val message: String) : TransferUiState()
    data class BoxScanned(val transfer: Transfer?) : TransferUiState()
    object TransferStarted : TransferUiState()
    object TransferCompleted : TransferUiState()
}
