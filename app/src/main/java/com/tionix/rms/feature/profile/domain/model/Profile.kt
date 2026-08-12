package com.tionix.rms.feature.profile.domain.model

data class UserProfile(
    val id: String,
    val fullName: String,
    val username: String,
    val role: String,
    val roleLabel: String,
    val warehouses: List<String>,
    val deviceSerial: String?,
    val deviceModel: String?,
    val appVersion: String
)
