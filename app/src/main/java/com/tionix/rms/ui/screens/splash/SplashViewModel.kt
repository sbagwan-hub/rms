package com.tionix.rms.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.auth.data.local.AuthPreferences
import com.tionix.rms.feature.auth.domain.repository.AuthRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1000)

            val token = authPreferences.getAccessToken()
            val userId = authPreferences.getUserId()
            val warehouseId = authPreferences.getWarehouseId()

            if (token.isNullOrBlank() || userId.isNullOrBlank()) {
                _destination.value = SplashDestination.Login
                return@launch
            }

            val hydrateResult = authRepository.hydrateSessionFromMe()
            if (hydrateResult.isSuccess) {
                val session = hydrateResult.getOrNull()
                if (session?.warehouse != null) {
                    _destination.value = SplashDestination.Dashboard
                } else {
                    authPreferences.clear()
                    _destination.value = SplashDestination.Login
                }
            } else if (warehouseId != null) {
                _destination.value = SplashDestination.Dashboard
            } else {
                authPreferences.clear()
                _destination.value = SplashDestination.Login
            }
        }
    }
}
