package com.tionix.rms.feature.auth.presentation

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val message: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    data class BiometricAvailable(val available: Boolean) : LoginUiState()
}
