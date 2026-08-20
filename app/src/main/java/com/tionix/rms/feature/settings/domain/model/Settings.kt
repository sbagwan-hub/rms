package com.tionix.rms.feature.settings.domain.model

data class Settings(
    val syncOnCellular: Boolean = false,
    val soundMuted: Boolean = false,
    val serverUrl: String = "",
    val scannerModeLabel: String = "Camera (fallback)"
)
