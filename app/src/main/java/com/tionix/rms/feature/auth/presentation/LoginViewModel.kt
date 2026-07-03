package com.tionix.rms.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.auth.domain.model.LoginRequest
import com.tionix.rms.feature.auth.domain.usecase.CheckBiometricAvailabilityUseCase
import com.tionix.rms.feature.auth.domain.usecase.LoginUseCase
import com.tionix.rms.feature.auth.domain.usecase.LoginWithBiometricUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithBiometricUseCase: LoginWithBiometricUseCase,
    private val checkBiometricAvailabilityUseCase: CheckBiometricAvailabilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    init {
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            val available = checkBiometricAvailabilityUseCase()
            _uiState.value = LoginUiState.BiometricAvailable(available)
        }
    }

    fun onUsernameChanged(value: String) {
        _username.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun login(deviceId: String) {
        viewModelScope.launch {
            if (_username.value.isBlank() || _password.value.isBlank()) {
                _uiState.value = LoginUiState.Error("Username and password are required")
                return@launch
            }
            
            _uiState.value = LoginUiState.Loading
            val result = loginUseCase(
                LoginRequest(
                    username = _username.value,
                    password = _password.value,
                    deviceId = deviceId
                )
            )
            
            when (result) {
                is com.tionix.rms.feature.auth.domain.model.AuthResult.Success -> {
                    _uiState.value = LoginUiState.Success("Login successful")
                }
                is com.tionix.rms.feature.auth.domain.model.AuthResult.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                }
                is com.tionix.rms.feature.auth.domain.model.AuthResult.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }
    }

    fun loginWithBiometric() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = loginWithBiometricUseCase()
            
            when (result) {
                is com.tionix.rms.feature.auth.domain.model.AuthResult.Success -> {
                    _uiState.value = LoginUiState.Success("Biometric login successful")
                }
                is com.tionix.rms.feature.auth.domain.model.AuthResult.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                }
                is com.tionix.rms.feature.auth.domain.model.AuthResult.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }
    }
}
