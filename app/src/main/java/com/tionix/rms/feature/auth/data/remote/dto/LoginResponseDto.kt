package com.tionix.rms.feature.auth.data.remote.dto

data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String
)
