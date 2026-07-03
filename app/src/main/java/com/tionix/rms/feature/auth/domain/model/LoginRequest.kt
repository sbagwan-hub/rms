package com.tionix.rms.feature.auth.domain.model

data class LoginRequest(
    val username: String,
    val password: String,
    val deviceId: String
)
