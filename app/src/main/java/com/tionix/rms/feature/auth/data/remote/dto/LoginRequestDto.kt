package com.tionix.rms.feature.auth.data.remote.dto

data class LoginRequestDto(
    val username: String,
    val password: String,
    val deviceId: String
)
