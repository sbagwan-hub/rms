package com.tionix.rms.feature.inventory.presentation

import com.tionix.rms.feature.inventory.domain.model.InventoryVerification

sealed class InventoryVerificationUiState {
    object Loading : InventoryVerificationUiState()
    data class Success(val verifications: List<InventoryVerification>) : InventoryVerificationUiState()
    data class Error(val message: String) : InventoryVerificationUiState()
    object VerificationStarted : InventoryVerificationUiState()
    object BoxScanned : InventoryVerificationUiState()
    object VerificationCompleted : InventoryVerificationUiState()
}
