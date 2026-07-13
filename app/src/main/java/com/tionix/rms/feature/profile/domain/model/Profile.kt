package com.tionix.rms.feature.profile.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val employeeId: String,
    val role: String,
    val warehouseId: String,
    val warehouseName: String,
    val siteId: String,
    val siteName: String,
    val email: String?
)

data class DailyStats(
    val freshBoxMoves: Int,
    val refiles: Int,
    val transfers: Int,
    val segregations: Int,
    val merges: Int,
    val verifications: Int,
    val totalActions: Int
)
