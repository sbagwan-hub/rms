package com.tionix.rms.feature.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String? = null,
    val user: UserDto,
    val company: EntityRefDto? = null,
    val branch: EntityRefDto? = null,
    val warehouse: EntityRefDto? = null,
    val permissions: List<String>? = null,
    val availableCompanies: List<EntityRefDto>? = null,
    val availableBranches: List<EntityRefDto>? = null,
    val availableWarehouses: List<EntityRefDto>? = null,
    val deviceId: String? = null
)

data class UserDto(
    val id: String,
    val fullName: String? = null,
    val name: String? = null,
    @SerializedName("username") val email: String,
    val employeeCode: String? = null,
    val mobile: String? = null,
    val role: String,
    val permissions: List<String>? = null,
    val warehouses: List<WarehouseSummaryDto>? = null
)

data class WarehouseSummaryDto(
    val id: String,
    val code: String? = null,
    val name: String? = null
)

/** POST /auth/refresh request body */
data class RefreshRequestDto(
    val refreshToken: String
)

data class RefreshResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String? = null,
    val user: UserDto? = null,
    val company: EntityRefDto? = null,
    val branch: EntityRefDto? = null,
    val warehouse: EntityRefDto? = null,
    val permissions: List<String>? = null
)

data class MeResponseDto(
    val id: String,
    val fullName: String,
    val email: String,
    val employeeCode: String? = null,
    val phone: String? = null,
    val status: String? = null,
    val company: EntityRefDto? = null,
    val branch: EntityRefDto? = null,
    val warehouse: EntityRefDto? = null,
    val permissions: List<String>? = null,
    val role: MeRoleDto? = null,
    val profile: MeProfileDto? = null,
    val availableCompanies: List<EntityRefDto>? = null,
    val availableBranches: List<EntityRefDto>? = null,
    val availableWarehouses: List<EntityRefDto>? = null,
    val session: MeSessionDto? = null,
    val warehouses: List<String>? = null
)

data class MeProfileDto(
    val id: String? = null,
    val employeeCode: String? = null,
    val name: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val role: String? = null
)

data class MeSessionDto(
    val companyId: String? = null,
    val branchId: String? = null,
    val warehouseId: String? = null
)

data class MeRoleDto(
    val id: String? = null,
    val name: String? = null,
    val label: String? = null,
    val permissions: List<String>? = null
)

data class LogoutRequestDto(
    val refreshToken: String
)
