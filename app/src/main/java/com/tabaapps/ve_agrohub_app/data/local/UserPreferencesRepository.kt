package com.tabaapps.ve_agrohub_app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tabaapps.ve_agrohub_app.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {
    companion object {
        private val API_KEY = stringPreferencesKey("custom_api_key")
        private val AI_MODEL = stringPreferencesKey("ai_model")
        private val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
    }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_KEY] ?: BuildConfig.API_KEY
    }

    val selectedModelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[AI_MODEL] ?: "gpt-3.5-turbo"
    }

    val systemPromptFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SYSTEM_PROMPT] ?: "You are a helpful, smart, concise AI assistant."
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }

    suspend fun saveSelectedModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[AI_MODEL] = model
        }
    }

    suspend fun saveSystemPrompt(systemPrompt: String) {
        context.dataStore.edit { preferences ->
            preferences[SYSTEM_PROMPT] = systemPrompt
        }
    }
}
