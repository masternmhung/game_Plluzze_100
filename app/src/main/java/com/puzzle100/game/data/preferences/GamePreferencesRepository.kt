package com.puzzle100.game.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "puzzle100_preferences")

data class GamePreferences(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val masterVolume: Float = 1.0f,
    val musicEnabled: Boolean = true,
    val musicVolume: Float = 0.5f,
    val preferredTheme: String = "DARK_PREMIUM"
)

class GamePreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val MASTER_VOLUME = floatPreferencesKey("master_volume")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val MUSIC_VOLUME = floatPreferencesKey("music_volume")
        val PREFERRED_THEME = stringPreferencesKey("preferred_theme")
    }

    val gamePreferencesFlow: Flow<GamePreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            GamePreferences(
                soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                hapticEnabled = preferences[PreferencesKeys.HAPTIC_ENABLED] ?: true,
                masterVolume = preferences[PreferencesKeys.MASTER_VOLUME] ?: 1.0f,
                musicEnabled = preferences[PreferencesKeys.MUSIC_ENABLED] ?: true,
                musicVolume = preferences[PreferencesKeys.MUSIC_VOLUME] ?: 0.5f,
                preferredTheme = preferences[PreferencesKeys.PREFERRED_THEME] ?: "DARK_PREMIUM"
            )
        }

    suspend fun updateSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateHapticEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun updateMasterVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MASTER_VOLUME] = volume.coerceIn(0.0f, 1.0f)
        }
    }

    suspend fun updateMusicEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MUSIC_ENABLED] = enabled
        }
    }

    suspend fun updateMusicVolume(volume: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MUSIC_VOLUME] = volume.coerceIn(0.0f, 1.0f)
        }
    }

    suspend fun updateTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_THEME] = theme
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: GamePreferencesRepository? = null

        fun getInstance(context: Context): GamePreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GamePreferencesRepository(context.applicationContext.dataStore).also {
                    INSTANCE = it
                }
            }
        }
    }
}
