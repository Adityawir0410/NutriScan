package com.example.nutriscan.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension untuk membuat DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val LAST_SYNC_TIMESTAMP = stringPreferencesKey("last_sync_timestamp")
    }
    
    /**
     * Save login state
     */
    suspend fun saveLoginState(userId: String, email: String?) {
        dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = userId
            preferences[USER_EMAIL] = email ?: ""
        }
    }
    
    /**
     * Clear login state (logout)
     */
    suspend fun clearLoginState() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Get login state as Flow
     */
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }
    
    /**
     * Get user ID
     */
    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID]
    }
    
    /**
     * Get user email
     */
    val userEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }
    
    /**
     * Get login state immediately (suspend function)
     */
    suspend fun getIsLoggedIn(): Boolean {
        var isLoggedIn = false
        dataStore.data.map { preferences ->
            isLoggedIn = preferences[IS_LOGGED_IN] ?: false
        }.collect { }
        return isLoggedIn
    }
    
    /**
     * Save last sync timestamp (when Room was synced with Firebase)
     */
    suspend fun saveLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] = timestamp.toString()
        }
    }
    
    /**
     * Get last sync timestamp
     */
    val lastSyncTimestamp: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIMESTAMP]?.toLongOrNull()
    }
}
