package com.tionix.rms.core.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tionix.rms.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.appSettingsDataStore

    companion object {
        private val KEY_SYNC_ON_CELLULAR = booleanPreferencesKey("sync_on_cellular")
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_SOUND_MUTED = booleanPreferencesKey("sound_muted")
    }

    val syncOnCellularFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_SYNC_ON_CELLULAR] ?: false
    }

    val serverUrlFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_SERVER_URL] ?: BuildConfig.API_BASE_URL
    }

    val soundMutedFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_SOUND_MUTED] ?: false
    }

    suspend fun isSyncOnCellular(): Boolean =
        dataStore.data.map { it[KEY_SYNC_ON_CELLULAR] ?: false }.first()

    suspend fun getServerUrl(): String =
        dataStore.data.map { it[KEY_SERVER_URL] ?: BuildConfig.API_BASE_URL }.first()

    suspend fun isSoundMuted(): Boolean =
        dataStore.data.map { it[KEY_SOUND_MUTED] ?: false }.first()

    suspend fun setSyncOnCellular(enabled: Boolean) {
        dataStore.edit { it[KEY_SYNC_ON_CELLULAR] = enabled }
    }

    suspend fun setServerUrl(url: String) {
        dataStore.edit { it[KEY_SERVER_URL] = url }
    }

    suspend fun setSoundMuted(muted: Boolean) {
        dataStore.edit { it[KEY_SOUND_MUTED] = muted }
    }
}
