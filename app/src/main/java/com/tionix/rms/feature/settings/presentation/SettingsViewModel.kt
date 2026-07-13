package com.tionix.rms.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tionix.rms.feature.settings.domain.model.Settings
import com.tionix.rms.feature.settings.domain.model.ThemeMode
import com.tionix.rms.feature.settings.domain.usecase.GetSettingsUseCase
import com.tionix.rms.feature.settings.domain.usecase.SyncNowUseCase
import com.tionix.rms.feature.settings.domain.usecase.UpdateSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val syncNowUseCase: SyncNowUseCase
) : ViewModel() {

    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState.Loading)
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _settingsState.value = SettingsState.Loading
            val result = getSettingsUseCase()
            
            if (result.isSuccess) {
                _settingsState.value = SettingsState.Success(result.getOrNull() ?: Settings())
            } else {
                _settingsState.value = SettingsState.Error(result.exceptionOrNull()?.message ?: "Failed to load settings")
            }
        }
    }

    fun updateScannerContinuousMode(enabled: Boolean) {
        val currentState = (_settingsState.value as? SettingsState.Success)?.settings ?: return
        val updatedSettings = currentState.copy(scannerContinuousMode = enabled)
        updateSettings(updatedSettings)
    }

    fun updateScannerBeep(enabled: Boolean) {
        val currentState = (_settingsState.value as? SettingsState.Success)?.settings ?: return
        val updatedSettings = currentState.copy(scannerBeep = enabled)
        updateSettings(updatedSettings)
    }

    fun updateScannerHaptic(enabled: Boolean) {
        val currentState = (_settingsState.value as? SettingsState.Success)?.settings ?: return
        val updatedSettings = currentState.copy(scannerHaptic = enabled)
        updateSettings(updatedSettings)
    }

    fun updateSyncAutoSync(enabled: Boolean) {
        val currentState = (_settingsState.value as? SettingsState.Success)?.settings ?: return
        val updatedSettings = currentState.copy(syncAutoSync = enabled)
        updateSettings(updatedSettings)
    }

    fun updateSyncWifiOnly(enabled: Boolean) {
        val currentState = (_settingsState.value as? SettingsState.Success)?.settings ?: return
        val updatedSettings = currentState.copy(syncWifiOnly = enabled)
        updateSettings(updatedSettings)
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        val currentState = (_settingsState.value as? SettingsState.Success)?.settings ?: return
        val updatedSettings = currentState.copy(themeMode = themeMode)
        updateSettings(updatedSettings)
    }

    private fun updateSettings(settings: Settings) {
        viewModelScope.launch {
            val result = updateSettingsUseCase(settings)
            if (result.isSuccess) {
                _settingsState.value = SettingsState.Success(settings)
            } else {
                _settingsState.value = SettingsState.Error(result.exceptionOrNull()?.message ?: "Failed to update settings")
            }
        }
    }

    fun syncNow(): Result<Unit> {
        return runCatching {
            viewModelScope.launch {
                val result = syncNowUseCase()
                if (result.isSuccess) {
                    _settingsState.value = SettingsState.SyncCompleted
                } else {
                    _settingsState.value = SettingsState.Error(result.exceptionOrNull()?.message ?: "Sync failed")
                }
            }
            Result.success(Unit)
        }
    }
}

sealed class SettingsState {
    object Loading : SettingsState()
    data class Success(val settings: Settings) : SettingsState()
    data class Error(val message: String) : SettingsState()
    object SyncCompleted : SettingsState()
}
