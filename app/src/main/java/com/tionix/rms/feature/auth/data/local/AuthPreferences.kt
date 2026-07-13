package com.tionix.rms.feature.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
    }

    val accessTokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_ACCESS_TOKEN]
    }

    val refreshTokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_REFRESH_TOKEN]
    }

    val userIdFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_USER_ID]
    }

    val fullNameFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_FULL_NAME]
    }

    val emailFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_EMAIL]
    }

    val roleFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_ROLE]
    }

    val isBiometricEnabledFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_BIOMETRIC_ENABLED] ?: false
    }

    suspend fun getAccessToken(): String? = dataStore.data.map { it[KEY_ACCESS_TOKEN] }.first()
    suspend fun getRefreshToken(): String? = dataStore.data.map { it[KEY_REFRESH_TOKEN] }.first()
    suspend fun getUserId(): String? = dataStore.data.map { it[KEY_USER_ID] }.first()
    suspend fun getFullName(): String? = dataStore.data.map { it[KEY_FULL_NAME] }.first()
    suspend fun getEmail(): String? = dataStore.data.map { it[KEY_EMAIL] }.first()
    suspend fun getRole(): String? = dataStore.data.map { it[KEY_ROLE] }.first()
    suspend fun isBiometricEnabled(): Boolean = dataStore.data.map { it[KEY_BIOMETRIC_ENABLED] ?: false }.first()

    suspend fun saveAuthSession(
        accessToken: String,
        refreshToken: String,
        userId: String,
        fullName: String,
        email: String,
        role: String
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
            preferences[KEY_USER_ID] = userId
            preferences[KEY_FULL_NAME] = fullName
            preferences[KEY_EMAIL] = email
            preferences[KEY_ROLE] = role
        }
    }

    suspend fun setAccessToken(value: String?) {
        dataStore.edit { preferences ->
            if (value != null) {
                preferences[KEY_ACCESS_TOKEN] = value
            } else {
                preferences.remove(KEY_ACCESS_TOKEN)
            }
        }
    }

    suspend fun setRefreshToken(value: String?) {
        dataStore.edit { preferences ->
            if (value != null) {
                preferences[KEY_REFRESH_TOKEN] = value
            } else {
                preferences.remove(KEY_REFRESH_TOKEN)
            }
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
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_FULL_NAME)
            preferences.remove(KEY_EMAIL)
            preferences.remove(KEY_ROLE)
        }
    }
}
