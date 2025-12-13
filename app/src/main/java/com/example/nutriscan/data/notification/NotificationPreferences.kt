package com.example.nutriscan.data.notification

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_prefs")

@Singleton
class NotificationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val READ_NOTIFICATION_IDS = stringSetPreferencesKey("read_notification_ids")
    }

    val readNotificationIds: Flow<Set<String>> = context.notificationDataStore.data
        .map { preferences ->
            preferences[READ_NOTIFICATION_IDS] ?: emptySet()
        }

    suspend fun markAsRead(notificationId: String) {
        context.notificationDataStore.edit { preferences ->
            val currentIds = preferences[READ_NOTIFICATION_IDS] ?: emptySet()
            preferences[READ_NOTIFICATION_IDS] = currentIds + notificationId
        }
    }

    suspend fun markAllAsRead(notificationIds: List<String>) {
        context.notificationDataStore.edit { preferences ->
            val currentIds = preferences[READ_NOTIFICATION_IDS] ?: emptySet()
            preferences[READ_NOTIFICATION_IDS] = currentIds + notificationIds.toSet()
        }
    }

    suspend fun clearReadStatus() {
        context.notificationDataStore.edit { preferences ->
            preferences.remove(READ_NOTIFICATION_IDS)
        }
    }
}
