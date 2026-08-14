package com.tionix.rms.feature.auth.data.remote.dto

data class EntityRefDto(
    val id: String,
    val name: String,
    val code: String? = null
)

data class SwitchWarehouseRequestDto(val warehouseId: String)
data class SwitchBranchRequestDto(val branchId: String)
data class SwitchCompanyRequestDto(val companyId: String)
