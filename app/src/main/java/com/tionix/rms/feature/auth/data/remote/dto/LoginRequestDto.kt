package com.tionix.rms.feature.auth.data.remote.dto

data class DeviceInfoDto(
    val serialNumber: String,
    val model: String,
    val appVersion: String
)

data class LoginRequestDto(
    val username: String,
    val password: String,
    val device: DeviceInfoDto
)
