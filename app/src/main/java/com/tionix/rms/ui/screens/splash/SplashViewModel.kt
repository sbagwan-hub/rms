package com.tionix.rms.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.auth.data.remote.AuthApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object Loading : SplashDestination()
    object Login : SplashDestination()
    object Dashboard : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val authApiService: AuthApiService
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)

            val token = authPreferences.getAccessToken()
            val userId = authPreferences.getUserId()

            if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                _destination.value = SplashDestination.Login
                return@launch
            }

            try {
                val response = authApiService.getMe()
                if (response.isSuccessful) {
                    _destination.value = SplashDestination.Dashboard
                } else if (response.code() == 401) {
                    authPreferences.clear()
                    _destination.value = SplashDestination.Login
                } else {
                    _destination.value = SplashDestination.Dashboard
                }
            } catch (_: Exception) {
                _destination.value = SplashDestination.Dashboard
            }
        }
    }
}
