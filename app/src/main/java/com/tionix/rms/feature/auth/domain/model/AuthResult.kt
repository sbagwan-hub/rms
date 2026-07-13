package com.tionix.rms.feature.auth.domain.model

sealed class AuthResult {
    data class Success(val accessToken: String, val refreshToken: String, val user: User) : AuthResult()
    data class Error(val message: String, val code: String? = null) : AuthResult()
    object Loading : AuthResult()
}

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole
)
