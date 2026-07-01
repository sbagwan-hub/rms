package com.tionix.rms.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    private val _operatorId = MutableStateFlow("")
    val operatorId: StateFlow<String> = _operatorId.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    fun updateOperatorId(id: String) {
        _operatorId.value = id
    }

    fun updatePassword(pwd: String) {
        _password.value = pwd
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }
}
