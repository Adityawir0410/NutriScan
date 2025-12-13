package com.example.nutriscan.ui.screens.notification

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.data.notification.NotificationHelper
import com.example.nutriscan.data.notification.NotificationPreferences
import com.example.nutriscan.data.repository.GeminiRepository
import com.example.nutriscan.data.repository.HistoryRepository
import com.example.nutriscan.domain.common.Result
import com.example.nutriscan.domain.model.ScanHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: NotificationType
)

enum class NotificationType {
    INFO, WARNING, SUCCESS, SCAN
}

data class NotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<NotificationItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val geminiRepository: GeminiRepository,
    private val notificationHelper: NotificationHelper,
    private val notificationPreferences: NotificationPreferences
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    
    init {
        loadNotifications()
    }
    
    fun loadNotifications() {
        viewModelScope.launch {
            // First, collect read status from DataStore
            notificationPreferences.readNotificationIds.collect { readIds ->
                // Then collect scan history
                historyRepository.getScanHistory().collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        
                        is Result.Success -> {
                            val scans = result.data ?: emptyList()
                            val notifications = generateNotifications(scans, readIds)
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                notifications = notifications,
                                error = null
                            )
                        }
                        
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                            Log.e("NotificationVM", "Error: ${result.message}")
                        }
                    }
                }
            }
        }
    }
    
    private suspend fun generateNotifications(scans: List<ScanHistory>, readIds: Set<String>): List<NotificationItem> {
        val notifications = mutableListOf<NotificationItem>()
        
        // Add welcome notification if first time
        if (scans.isEmpty()) {
            val welcomeNotif = NotificationItem(
                id = "welcome",
                title = "Selamat Datang di NutriScan! 🎉",
                message = "Mulai scan makanan untuk mendapat analisis nutrisi",
                timestamp = System.currentTimeMillis(),
                isRead = readIds.contains("welcome"),
                type = NotificationType.INFO
            )
            notifications.add(welcomeNotif)
            
            // Send push notification if not read
            if (!readIds.contains("welcome")) {
                sendPushNotification(welcomeNotif)
            }
            
            return notifications
        }
        
        // Generate notifications from recent scans (last 20)
        val recentScans = scans.take(20)
        
        recentScans.forEach { scan ->
            val notificationId = "scan_${scan.id}"
            
            // Determine notification type based on status
            val type = when (scan.status.lowercase()) {
                "sehat" -> NotificationType.SUCCESS
                "tidak sehat" -> NotificationType.WARNING
                else -> NotificationType.SCAN
            }
            
            // Generate AI message for the scan
            val message = try {
                geminiRepository.analyzeFoodScan(
                    foodName = scan.nama,
                    status = scan.status,
                    nutrition = scan.gizi
                )
            } catch (e: Exception) {
                generateFallbackMessage(scan)
            }
            
            val notifItem = NotificationItem(
                id = notificationId,
                title = "Scan: ${scan.nama}",
                message = message,
                timestamp = parseDateToTimestamp(scan.date),
                isRead = readIds.contains(notificationId),
                type = type
            )
            
            notifications.add(notifItem)
            
            // Send push notification if not read
            if (!readIds.contains(notificationId)) {
                sendPushNotification(notifItem)
            }
        }
        
        // Add achievement notifications
        val achievements = generateAchievementNotifications(scans, readIds)
        notifications.addAll(achievements)
        
        // Send push for new achievements
        achievements.filter { !readIds.contains(it.id) }.forEach { achievement ->
            sendPushNotification(achievement)
        }
        
        // Sort by timestamp (newest first)
        return notifications.sortedByDescending { it.timestamp }
    }
    
    private fun sendPushNotification(notification: NotificationItem) {
        val priority = when (notification.type) {
            NotificationType.WARNING -> NotificationCompat.PRIORITY_HIGH
            NotificationType.SUCCESS -> NotificationCompat.PRIORITY_DEFAULT
            else -> NotificationCompat.PRIORITY_LOW
        }
        
        notificationHelper.showNotification(
            notificationId = notification.id.hashCode(),
            title = notification.title,
            message = notification.message,
            priority = priority
        )
    }
    
    private fun generateFallbackMessage(scan: ScanHistory): String {
        return when (scan.status.lowercase()) {
            "sehat" -> "Pilihan makanan sehat! Nutrisi seimbang terdeteksi"
            "tidak sehat" -> "Perhatian: makanan ini tinggi kalori atau gula"
            else -> "Scan berhasil, lihat detail untuk info nutrisi"
        }
    }
    
    private fun generateAchievementNotifications(scans: List<ScanHistory>, readIds: Set<String>): List<NotificationItem> {
        val achievements = mutableListOf<NotificationItem>()
        
        // Achievement: First scan
        if (scans.size == 1) {
            achievements.add(
                NotificationItem(
                    id = "achievement_first",
                    title = "🎯 Pencapaian: Scan Pertama!",
                    message = "Selamat! Anda telah melakukan scan makanan pertama",
                    timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5),
                    isRead = readIds.contains("achievement_first"),
                    type = NotificationType.SUCCESS
                )
            )
        }
        
        // Achievement: 5 scans
        if (scans.size == 5) {
            achievements.add(
                NotificationItem(
                    id = "achievement_5",
                    title = "🏆 Pencapaian: 5 Scan!",
                    message = "Luar biasa! Anda semakin peduli dengan nutrisi",
                    timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10),
                    isRead = readIds.contains("achievement_5"),
                    type = NotificationType.SUCCESS
                )
            )
        }
        
        // Achievement: 10 scans
        if (scans.size >= 10) {
            achievements.add(
                NotificationItem(
                    id = "achievement_10",
                    title = "⭐ Pencapaian: Nutrition Expert!",
                    message = "Hebat! 10+ scan makanan, Anda nutrition expert!",
                    timestamp = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1),
                    isRead = readIds.contains("achievement_10"),
                    type = NotificationType.SUCCESS
                )
            )
        }
        
        // Health warning if too many unhealthy scans recently
        val recentUnhealthy = scans.take(5).count { it.status.lowercase() == "tidak sehat" }
        if (recentUnhealthy >= 3) {
            achievements.add(
                NotificationItem(
                    id = "warning_unhealthy",
                    title = "⚠️ Peringatan Kesehatan",
                    message = "3 dari 5 scan terakhir tidak sehat, pertimbangkan makanan lebih sehat",
                    timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30),
                    isRead = readIds.contains("warning_unhealthy"),
                    type = NotificationType.WARNING
                )
            )
        }
        
        return achievements
    }
    
    private fun parseDateToTimestamp(dateStr: String): Long {
        // Format: "dd MMM yyyy, HH:mm"
        // For simplicity, return current time minus random offset
        // In production, parse the actual date
        return System.currentTimeMillis() - TimeUnit.HOURS.toMillis((1..48).random().toLong())
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationPreferences.markAsRead(notificationId)
            
            // Update UI state
            _uiState.value = _uiState.value.copy(
                notifications = _uiState.value.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
            )
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            val allIds = _uiState.value.notifications.map { it.id }
            notificationPreferences.markAllAsRead(allIds)
            
            _uiState.value = _uiState.value.copy(
                notifications = _uiState.value.notifications.map { it.copy(isRead = true) }
            )
        }
    }
    
    fun getUnreadCount(): Int {
        return _uiState.value.notifications.count { !it.isRead }
    }
}
