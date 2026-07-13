package com.tionix.rms.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.auth.domain.model.LoginRequest
import com.tionix.rms.feature.auth.domain.usecase.CheckBiometricAvailabilityUseCase
import com.tionix.rms.feature.auth.domain.usecase.LoginUseCase
import com.tionix.rms.feature.auth.domain.usecase.LoginWithBiometricUseCase
import com.tionix.rms.utils.scanner.ScannerManager
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
    private val checkBiometricAvailabilityUseCase: CheckBiometricAvailabilityUseCase,
    private val scannerManager: ScannerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _usernameError = MutableStateFlow<String?>(null)
    val usernameError: StateFlow<String?> = _usernameError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    init {
        // Collect scan results (badge scans) to fill in username/employee ID
        viewModelScope.launch {
            scannerManager.scanResults.collect { scannedBarcode ->
                _username.value = scannedBarcode
                validateUsername()
            }
        }
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
        if (_usernameError.value != null) {
            validateUsername()
        }
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
        if (_passwordError.value != null) {
            validatePassword()
        }
    }

    private fun validateUsername(): Boolean {
        val value = _username.value.trim()
        return when {
            value.isEmpty() -> {
                _usernameError.value = "Username or Employee ID is required"
                false
            }
            else -> {
                _usernameError.value = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        val value = _password.value
        return when {
            value.isEmpty() -> {
                _passwordError.value = "Password is required"
                false
            }
            value.length < 6 -> {
                _passwordError.value = "Password must be at least 6 characters"
                false
            }
            else -> {
                _passwordError.value = null
                true
            }
        }
    }

    fun login(deviceId: String) {
        viewModelScope.launch {
            val isUserValid = validateUsername()
            val isPassValid = validatePassword()
            if (!isUserValid || !isPassValid) {
                return@launch
            }
            
            _uiState.value = LoginUiState.Loading
            val result = loginUseCase(
                LoginRequest(
                    username = _username.value.trim(),
                    password = _password.value,
                    deviceId = deviceId
                )
            )
            
            when (result) {
                is com.tionix.rms.feature.auth.domain.model.AuthResult.Success -> {
                    _uiState.value = LoginUiState.Success("Login successful")
                }
                is com.tionix.rms.feature.auth.domain.model.AuthResult.Error -> {
                    val errorMsg = when (result.code) {
                        "401", "INVALID_CREDENTIALS" -> "Invalid credentials. Please try again."
                        "404" -> "User not found."
                        else -> result.message
                    }
                    _uiState.value = LoginUiState.Error(errorMsg)
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

    fun enableScanner() {
        scannerManager.enable()
    }

    fun disableScanner() {
        scannerManager.disable()
    }
}
