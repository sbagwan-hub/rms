package com.tionix.rms.feature.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val fullName: String,
    @SerializedName("username") val email: String,
    val role: String
)

/** POST /auth/refresh request body — backend's refreshSchema requires exactly this shape. */
data class RefreshRequestDto(
    val refreshToken: String
)

/** POST /auth/refresh response — unlike login, the backend does not re-send the user object. */
data class RefreshResponseDto(
    val accessToken: String,
    val refreshToken: String
)
