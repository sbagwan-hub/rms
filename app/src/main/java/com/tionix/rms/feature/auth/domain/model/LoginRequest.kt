package com.tionix.rms.feature.auth.domain.model

data class DeviceInfo(
    val serialNumber: String,
    val model: String,
    val appVersion: String
)

data class LoginRequest(
    val username: String,
    val password: String,
    val device: DeviceInfo
)
