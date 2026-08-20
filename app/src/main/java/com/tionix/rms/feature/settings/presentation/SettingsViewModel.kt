package com.tionix.rms.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.BuildConfig
import com.tionix.rms.core.audio.BeepPlayer
import com.tionix.rms.feature.settings.domain.model.Settings
import com.tionix.rms.feature.settings.domain.usecase.ClearLookupCacheUseCase
import com.tionix.rms.feature.settings.domain.usecase.GetSettingsUseCase
import com.tionix.rms.feature.settings.domain.usecase.SyncNowUseCase
import com.tionix.rms.feature.settings.domain.usecase.UpdateServerUrlUseCase
import com.tionix.rms.feature.settings.domain.usecase.UpdateSyncOnCellularUseCase
import com.tionix.rms.feature.sync.data.SyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: com.tionix.rms.feature.settings.domain.repository.SettingsRepository,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSyncOnCellularUseCase: UpdateSyncOnCellularUseCase,
    private val updateServerUrlUseCase: UpdateServerUrlUseCase,
    private val clearLookupCacheUseCase: ClearLookupCacheUseCase,
    private val syncNowUseCase: SyncNowUseCase,
    private val syncScheduler: SyncScheduler,
    private val beepPlayer: BeepPlayer
) : ViewModel() {

    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState.Loading)
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val appVersion: String = BuildConfig.VERSION_NAME
    val defaultServerUrl: String = BuildConfig.API_BASE_URL

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading
            getSettingsUseCase()
                .onSuccess { settings ->
                    _settingsState.value = SettingsState.Success(settings)
                }
                .onFailure { error ->
                    _settingsState.value = SettingsState.Error(
                        error.message ?: "Failed to load settings"
                    )
                }
        }
    }

    fun updateSyncOnCellular(enabled: Boolean) {
        viewModelScope.launch {
            updateSyncOnCellularUseCase(enabled)
                .onSuccess {
                    syncScheduler.schedulePeriodicSync()
                    loadSettings()
                }
                .onFailure { error ->
                    _message.value = error.message ?: "Failed to update sync preference"
                }
        }
    }

    fun updateSoundMuted(muted: Boolean) {
        viewModelScope.launch {
            repository.updateSoundMuted(muted)
                .onSuccess { loadSettings() }
                .onFailure { error ->
                    _message.value = error.message ?: "Failed to update sound preference"
                }
        }
    }

    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            updateServerUrlUseCase(url.trim())
                .onSuccess { loadSettings() }
                .onFailure { error ->
                    _message.value = error.message ?: "Failed to update server URL"
                }
        }
    }

    fun clearLookupCache() {
        viewModelScope.launch {
            clearLookupCacheUseCase()
                .onSuccess { _message.value = "Lookup cache cleared" }
                .onFailure { error ->
                    _message.value = error.message ?: "Failed to clear lookup cache"
                }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncNowUseCase()
                .onSuccess { _message.value = "Sync scheduled" }
                .onFailure { error ->
                    _message.value = error.message ?: "Sync failed"
                }
        }
    }

    fun testSuccessBeep() = beepPlayer.testPositive()
    fun testErrorBeep() = beepPlayer.testError()
    fun testWarningBeep() = beepPlayer.testWarning()

    fun clearMessage() {
        _message.value = null
    }
}

sealed class SettingsState {
    object Loading : SettingsState()
    data class Success(val settings: Settings) : SettingsState()
    data class Error(val message: String) : SettingsState()
}
