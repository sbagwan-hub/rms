package com.tionix.rms.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.tionix.rms.core.network.dto.UserData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_store")

/**
 * SessionStore
 * ============
 * Manages active user authentication session information (AccessToken, RefreshToken, Full Name,
 * and User Details including role, permissions, and assigned warehouses) using DataStore.
 */
@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.sessionDataStore
    private val gson = Gson()

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER = stringPreferencesKey("user_data")
        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
    }

    /**
     * Flow emitting the access token of the current session.
     */
    val accessTokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_ACCESS_TOKEN]
    }

    /**
     * Flow emitting the user metadata of the current session.
     */
    val userFlow: Flow<UserData?> = dataStore.data.map { preferences ->
        val json = preferences[KEY_USER] ?: return@map null
        try {
            gson.fromJson(json, UserData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Helper flow emitting whether a user is currently authenticated.
     */
    val isAuthenticatedFlow: Flow<Boolean> = accessTokenFlow.map { !it.isNullOrBlank() }

    suspend fun getAccessToken(): String? = dataStore.data.map { it[KEY_ACCESS_TOKEN] }.first()
    
    suspend fun getRefreshToken(): String? = dataStore.data.map { it[KEY_REFRESH_TOKEN] }.first()

    suspend fun getFullName(): String? = dataStore.data.map { it[KEY_FULL_NAME] }.first()

    suspend fun getUser(): UserData? {
        val json = dataStore.data.map { it[KEY_USER] }.first() ?: return null
        return try {
            gson.fromJson(json, UserData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Saves session credentials and metadata upon successful login.
     */
    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        fullName: String,
        user: UserData
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
            preferences[KEY_FULL_NAME] = fullName
            preferences[KEY_USER] = gson.toJson(user)
        }
    }

    /**
     * Clears all session credentials, effectively logging the user out.
     */
    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_REFRESH_TOKEN)
            preferences.remove(KEY_FULL_NAME)
            preferences.remove(KEY_USER)
        }
    }
}
