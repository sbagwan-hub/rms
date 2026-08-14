package com.tionix.rms.feature.auth.data.remote.dto

import com.tionix.rms.feature.auth.domain.model.AuthSession
import com.tionix.rms.feature.auth.domain.model.EntityRef
import com.tionix.rms.feature.auth.domain.model.LoginRequest
import com.tionix.rms.feature.auth.domain.model.User
import com.tionix.rms.feature.auth.domain.model.UserRole

fun LoginRequest.toDto(): LoginRequestDto {
    return LoginRequestDto(
        username = username,
        password = password,
        device = DeviceInfoDto(
            serialNumber = device.serialNumber,
            model = device.model,
            appVersion = device.appVersion
        )
    )
}

fun EntityRefDto.toDomain(): EntityRef = EntityRef(id = id, name = name, code = code)

fun UserDto.toDomain(): User {
    val userRole = try {
        UserRole.valueOf(role)
    } catch (e: IllegalArgumentException) {
        UserRole.OPERATOR
    }
    val resolvedName = fullName ?: name ?: email
    return User(
        id = id,
        fullName = resolvedName,
        email = email,
        role = userRole
    )
}

fun LoginResponseDto.toSession(): AuthSession {
    val perms = (permissions ?: user.permissions.orEmpty()).toSet()
    return AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresAt = expiresAt,
        user = user.toDomain(),
        company = company?.toDomain(),
        branch = branch?.toDomain(),
        warehouse = warehouse?.toDomain(),
        permissions = perms,
        availableWarehouses = availableWarehouses.orEmpty().map { it.toDomain() },
        availableBranches = availableBranches.orEmpty().map { it.toDomain() },
        availableCompanies = availableCompanies.orEmpty().map { it.toDomain() }
    )
}

fun RefreshResponseDto.toSession(existing: AuthSession? = null): AuthSession {
    val resolvedUser = user?.toDomain() ?: existing?.user
        ?: throw IllegalStateException("Refresh response missing user and no cached session")
    val perms = (permissions ?: user?.permissions ?: existing?.permissions.orEmpty()).toSet()
    return AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresAt = expiresAt,
        user = resolvedUser,
        company = company?.toDomain() ?: existing?.company,
        branch = branch?.toDomain() ?: existing?.branch,
        warehouse = warehouse?.toDomain() ?: existing?.warehouse,
        permissions = perms,
        availableWarehouses = existing?.availableWarehouses.orEmpty(),
        availableBranches = existing?.availableBranches.orEmpty(),
        availableCompanies = existing?.availableCompanies.orEmpty()
    )
}

fun MeResponseDto.toSession(
    accessToken: String,
    refreshToken: String,
    existingWarehouses: List<EntityRef> = emptyList(),
    existingBranches: List<EntityRef> = emptyList(),
    existingCompanies: List<EntityRef> = emptyList()
): AuthSession {
    val roleName = role?.name ?: profile?.role ?: "OPERATOR"
    val perms = (permissions ?: role?.permissions.orEmpty()).toSet()
    val user = User(
        id = id,
        fullName = fullName,
        email = email,
        role = try {
            UserRole.valueOf(roleName)
        } catch (_: Exception) {
            UserRole.OPERATOR
        }
    )
    return AuthSession(
        accessToken = accessToken,
        refreshToken = refreshToken,
        user = user,
        company = company?.toDomain(),
        branch = branch?.toDomain(),
        warehouse = warehouse?.toDomain(),
        permissions = perms,
        availableWarehouses = availableWarehouses?.map { it.toDomain() } ?: existingWarehouses,
        availableBranches = availableBranches?.map { it.toDomain() } ?: existingBranches,
        availableCompanies = availableCompanies?.map { it.toDomain() } ?: existingCompanies
    )
}

fun AuthSession.persistFields(): SessionPersistPayload {
    return SessionPersistPayload(
        accessToken = accessToken,
        refreshToken = refreshToken,
        userId = user.id,
        fullName = user.fullName,
        email = user.email,
        role = user.role.name,
        permissions = permissions,
        expiresAt = expiresAt,
        company = company,
        branch = branch,
        warehouse = warehouse,
        availableWarehouses = availableWarehouses,
        availableBranches = availableBranches,
        availableCompanies = availableCompanies
    )
}

data class SessionPersistPayload(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val fullName: String,
    val email: String,
    val role: String,
    val permissions: Set<String>,
    val expiresAt: String? = null,
    val company: EntityRef? = null,
    val branch: EntityRef? = null,
    val warehouse: EntityRef? = null,
    val availableWarehouses: List<EntityRef> = emptyList(),
    val availableBranches: List<EntityRef> = emptyList(),
    val availableCompanies: List<EntityRef> = emptyList()
)

suspend fun com.tionix.rms.feature.auth.data.local.AuthPreferences.persistSessionPayload(payload: SessionPersistPayload) {
    saveSession(
        accessToken = payload.accessToken,
        refreshToken = payload.refreshToken,
        userId = payload.userId,
        fullName = payload.fullName,
        email = payload.email,
        role = payload.role,
        permissions = payload.permissions,
        expiresAt = payload.expiresAt,
        company = payload.company,
        branch = payload.branch,
        warehouse = payload.warehouse,
        availableWarehouses = payload.availableWarehouses,
        availableBranches = payload.availableBranches,
        availableCompanies = payload.availableCompanies
    )
}
