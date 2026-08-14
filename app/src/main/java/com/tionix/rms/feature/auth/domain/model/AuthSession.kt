package com.tionix.rms.feature.auth.domain.model

data class EntityRef(
    val id: String,
    val name: String,
    val code: String? = null
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String? = null,
    val user: User,
    val company: EntityRef?,
    val branch: EntityRef?,
    val warehouse: EntityRef?,
    val permissions: Set<String> = emptySet(),
    val availableWarehouses: List<EntityRef> = emptyList(),
    val availableBranches: List<EntityRef> = emptyList(),
    val availableCompanies: List<EntityRef> = emptyList()
)
