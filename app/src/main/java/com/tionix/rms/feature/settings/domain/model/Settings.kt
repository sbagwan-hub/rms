package com.tionix.rms.feature.settings.domain.model

data class Settings(
    val scannerContinuousMode: Boolean = true,
    val scannerBeep: Boolean = true,
    val scannerHaptic: Boolean = true,
    val syncAutoSync: Boolean = true,
    val syncWifiOnly: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.FOLLOW_SYSTEM
)

enum class ThemeMode {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK
}
