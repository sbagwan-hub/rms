package com.tionix.rms.feature.profile.domain.model

import com.tionix.rms.feature.auth.domain.model.EntityRef

data class UserProfile(
    val id: String,
    val fullName: String,
    val username: String,
    val role: String,
    val roleLabel: String,
    val warehouses: List<String>,
    val companyName: String? = null,
    val branchName: String? = null,
    val warehouseName: String? = null,
    val warehouseCode: String? = null,
    val activeWarehouseId: String? = null,
    val availableWarehouses: List<EntityRef> = emptyList(),
    val deviceSerial: String?,
    val deviceModel: String?,
    val appVersion: String
)
