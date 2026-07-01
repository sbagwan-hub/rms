package com.tionix.rms.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.tionix.rms.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    private val _currentScreen = MutableStateFlow(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _lastScannedCode = MutableStateFlow<String?>(null)
    val lastScannedCode: StateFlow<String?> = _lastScannedCode.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun onCodeScanned(code: String) {
        _lastScannedCode.value = code
        _currentScreen.value = Screen.Dashboard
    }
}
