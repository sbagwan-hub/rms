package com.tionix.rms.feature.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tionix.rms.feature.auth.data.remote.dto.EntityRefDto
import com.tionix.rms.feature.auth.domain.model.EntityRef
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_EXPIRES_AT = stringPreferencesKey("expires_at")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_PERMISSIONS = stringSetPreferencesKey("permissions")
        private val KEY_COMPANY_ID = stringPreferencesKey("company_id")
        private val KEY_COMPANY_NAME = stringPreferencesKey("company_name")
        private val KEY_BRANCH_ID = stringPreferencesKey("branch_id")
        private val KEY_BRANCH_NAME = stringPreferencesKey("branch_name")
        private val KEY_WAREHOUSE_ID = stringPreferencesKey("warehouse_id")
        private val KEY_WAREHOUSE_CODE = stringPreferencesKey("warehouse_code")
        private val KEY_WAREHOUSE_NAME = stringPreferencesKey("warehouse_name")
        private val KEY_AVAILABLE_WAREHOUSES = stringPreferencesKey("available_warehouses_json")
        private val KEY_AVAILABLE_BRANCHES = stringPreferencesKey("available_branches_json")
        private val KEY_AVAILABLE_COMPANIES = stringPreferencesKey("available_companies_json")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val accessTokenFlow: Flow<String?> = dataStore.data.map { it[KEY_ACCESS_TOKEN] }
    val refreshTokenFlow: Flow<String?> = dataStore.data.map { it[KEY_REFRESH_TOKEN] }
    val userIdFlow: Flow<String?> = dataStore.data.map { it[KEY_USER_ID] }
    val fullNameFlow: Flow<String?> = dataStore.data.map { it[KEY_FULL_NAME] }
    val emailFlow: Flow<String?> = dataStore.data.map { it[KEY_EMAIL] }
    val roleFlow: Flow<String?> = dataStore.data.map { it[KEY_ROLE] }
    val isBiometricEnabledFlow: Flow<Boolean> = dataStore.data.map { it[KEY_BIOMETRIC_ENABLED] ?: false }

    suspend fun getAccessToken(): String? = dataStore.data.map { it[KEY_ACCESS_TOKEN] }.first()
    suspend fun getRefreshToken(): String? = dataStore.data.map { it[KEY_REFRESH_TOKEN] }.first()
    suspend fun getExpiresAt(): String? = dataStore.data.map { it[KEY_EXPIRES_AT] }.first()
    suspend fun getUserId(): String? = dataStore.data.map { it[KEY_USER_ID] }.first()
    suspend fun getFullName(): String? = dataStore.data.map { it[KEY_FULL_NAME] }.first()
    suspend fun getEmail(): String? = dataStore.data.map { it[KEY_EMAIL] }.first()
    suspend fun getRole(): String? = dataStore.data.map { it[KEY_ROLE] }.first()
    suspend fun getPermissions(): Set<String> =
        dataStore.data.map { it[KEY_PERMISSIONS] ?: emptySet() }.first()
    suspend fun hasPermission(permission: String): Boolean = getPermissions().contains(permission)
    suspend fun isBiometricEnabled(): Boolean = dataStore.data.map { it[KEY_BIOMETRIC_ENABLED] ?: false }.first()

    suspend fun getCompanyId(): String? = dataStore.data.map { it[KEY_COMPANY_ID] }.first()
    suspend fun getCompanyName(): String? = dataStore.data.map { it[KEY_COMPANY_NAME] }.first()
    suspend fun getBranchId(): String? = dataStore.data.map { it[KEY_BRANCH_ID] }.first()
    suspend fun getBranchName(): String? = dataStore.data.map { it[KEY_BRANCH_NAME] }.first()
    suspend fun getWarehouseId(): String? = dataStore.data.map { it[KEY_WAREHOUSE_ID] }.first()
    suspend fun getWarehouseCode(): String? = dataStore.data.map { it[KEY_WAREHOUSE_CODE] }.first()
    suspend fun getWarehouseName(): String? = dataStore.data.map { it[KEY_WAREHOUSE_NAME] }.first()

    suspend fun getAvailableWarehouses(): List<EntityRef> = readEntityList(KEY_AVAILABLE_WAREHOUSES)
    suspend fun getAvailableBranches(): List<EntityRef> = readEntityList(KEY_AVAILABLE_BRANCHES)
    suspend fun getAvailableCompanies(): List<EntityRef> = readEntityList(KEY_AVAILABLE_COMPANIES)

    suspend fun saveAuthSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        fullName: String,
        email: String,
        role: String,
        permissions: Set<String> = emptySet()
    ) {
        saveSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            fullName = fullName,
            email = email,
            role = role,
            permissions = permissions
        )
    }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        fullName: String,
        email: String,
        role: String,
        permissions: Set<String> = emptySet(),
        expiresAt: String? = null,
        company: EntityRef? = null,
        branch: EntityRef? = null,
        warehouse: EntityRef? = null,
        availableWarehouses: List<EntityRef> = emptyList(),
        availableBranches: List<EntityRef> = emptyList(),
        availableCompanies: List<EntityRef> = emptyList()
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
            preferences[KEY_USER_ID] = userId
            preferences[KEY_FULL_NAME] = fullName
            preferences[KEY_EMAIL] = email
            preferences[KEY_ROLE] = role
            preferences[KEY_PERMISSIONS] = permissions

            if (expiresAt != null) preferences[KEY_EXPIRES_AT] = expiresAt
            else preferences.remove(KEY_EXPIRES_AT)

            if (company != null) {
                preferences[KEY_COMPANY_ID] = company.id
                preferences[KEY_COMPANY_NAME] = company.name
            } else {
                preferences.remove(KEY_COMPANY_ID)
                preferences.remove(KEY_COMPANY_NAME)
            }

            if (branch != null) {
                preferences[KEY_BRANCH_ID] = branch.id
                preferences[KEY_BRANCH_NAME] = branch.name
            } else {
                preferences.remove(KEY_BRANCH_ID)
                preferences.remove(KEY_BRANCH_NAME)
            }

            if (warehouse != null) {
                preferences[KEY_WAREHOUSE_ID] = warehouse.id
                warehouse.code?.let { preferences[KEY_WAREHOUSE_CODE] = it }
                    ?: preferences.remove(KEY_WAREHOUSE_CODE)
                preferences[KEY_WAREHOUSE_NAME] = warehouse.name
            } else {
                preferences.remove(KEY_WAREHOUSE_ID)
                preferences.remove(KEY_WAREHOUSE_CODE)
                preferences.remove(KEY_WAREHOUSE_NAME)
            }

            writeEntityList(preferences, KEY_AVAILABLE_WAREHOUSES, availableWarehouses)
            writeEntityList(preferences, KEY_AVAILABLE_BRANCHES, availableBranches)
            writeEntityList(preferences, KEY_AVAILABLE_COMPANIES, availableCompanies)
        }
    }

    suspend fun setAccessToken(value: String?) {
        dataStore.edit { preferences ->
            if (value != null) preferences[KEY_ACCESS_TOKEN] = value
            else preferences.remove(KEY_ACCESS_TOKEN)
        }
    }

    suspend fun setRefreshToken(value: String?) {
        dataStore.edit { preferences ->
            if (value != null) preferences[KEY_REFRESH_TOKEN] = value
            else preferences.remove(KEY_REFRESH_TOKEN)
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_REFRESH_TOKEN)
            preferences.remove(KEY_EXPIRES_AT)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_FULL_NAME)
            preferences.remove(KEY_EMAIL)
            preferences.remove(KEY_ROLE)
            preferences.remove(KEY_PERMISSIONS)
            preferences.remove(KEY_COMPANY_ID)
            preferences.remove(KEY_COMPANY_NAME)
            preferences.remove(KEY_BRANCH_ID)
            preferences.remove(KEY_BRANCH_NAME)
            preferences.remove(KEY_WAREHOUSE_ID)
            preferences.remove(KEY_WAREHOUSE_CODE)
            preferences.remove(KEY_WAREHOUSE_NAME)
            preferences.remove(KEY_AVAILABLE_WAREHOUSES)
            preferences.remove(KEY_AVAILABLE_BRANCHES)
            preferences.remove(KEY_AVAILABLE_COMPANIES)
        }
    }

    private suspend fun readEntityList(key: Preferences.Key<String>): List<EntityRef> {
        val json = dataStore.data.map { it[key] }.first()
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<EntityRefDto>>() {}.type
            val dtoList: List<EntityRefDto> = gson.fromJson(json, type)
            dtoList.map { EntityRef(it.id, it.name, it.code) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writeEntityList(
        preferences: Preferences.MutablePreferences,
        key: Preferences.Key<String>,
        items: List<EntityRef>
    ) {
        if (items.isEmpty()) {
            preferences.remove(key)
            return
        }
        val dtoList = items.map { EntityRefDto(it.id, it.name, it.code) }
        preferences[key] = gson.toJson(dtoList)
    }
}
