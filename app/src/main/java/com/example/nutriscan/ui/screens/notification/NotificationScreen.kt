package com.example.nutriscan.ui.screens.notification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

// --- Palet Warna (Sesuai Tema) ---
private val PrimaryGreen = Color(0xFF388E3C)
private val LightGreenBg = Color(0xFFE8F5E9) // Background untuk item belum dibaca
private val TextDark = Color(0xFF1F2937)
private val TextGrey = Color(0xFF6B7280)
private val BorderColor = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Semua", "Belum Dibaca")
    val uiState by viewModel.uiState.collectAsState()

    val displayedNotifications = when (selectedTabIndex) {
        0 -> uiState.notifications
        1 -> uiState.notifications.filter { !it.isRead }
        else -> uiState.notifications
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 1. Background Layar Putih
    ) {
        // --- HEADER SECTION ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Title & Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifikasi",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )

                if (viewModel.getUnreadCount() > 0) {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text(
                            "Tandai Sudah Dibaca",
                            color = PrimaryGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = PrimaryGreen,
                divider = { HorizontalDivider(color = Color(0xFFF3F4F6)) },
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PrimaryGreen,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTabIndex == index) PrimaryGreen else TextGrey
                                )

                                // Badge Count (Merah)
                                if (index == 1) {
                                    val unreadCount = viewModel.getUnreadCount()
                                    if (unreadCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFEF4444), // Merah
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = unreadCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // --- CONTENT SECTION ---
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }

            displayedNotifications.isEmpty() -> {
                EmptyNotificationState(isUnreadTab = selectedTabIndex == 1)
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayedNotifications) { notification ->
                        NotificationItemCard(
                            notification = notification,
                            onClick = { viewModel.markAsRead(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    // Styling Kondisional
    val isUnread = !notification.isRead
    val backgroundColor = if (isUnread) LightGreenBg else Color.White
    val borderColor = if (isUnread) PrimaryGreen.copy(alpha = 0.3f) else BorderColor

    // Warna & Icon berdasarkan Tipe
    val (iconVector, iconTint, iconBg) = when (notification.type) {
        NotificationType.INFO -> Triple(Icons.Default.Info, Color(0xFF2196F3), Color(0xFFE3F2FD)) // Biru
        NotificationType.WARNING -> Triple(Icons.Default.Warning, Color(0xFFFF9800), Color(0xFFFFF3E0)) // Oranye
        NotificationType.SUCCESS -> Triple(Icons.Default.CheckCircle, Color(0xFF4CAF50), Color(0xFFE8F5E9)) // Hijau
        NotificationType.SCAN -> Triple(Icons.Default.QrCodeScanner, PrimaryGreen, Color(0xFFE8F5E9)) // Hijau App
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 0.dp else 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            // Ganti crossAxisAlignment jadi verticalAlignment
            // Dan ganti Alignment.Start (Horizontal) jadi Alignment.Top atau CenterVertically (Vertikal)
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Icon Container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                            color = TextDark
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Dot Merah jika belum dibaca
                    if (isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUnread) TextDark else TextGrey
                    ),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatTimestamp(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isUnread) PrimaryGreen else TextGrey,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationState(isUnreadTab: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Icon Placeholder Besar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.LightGray
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isUnreadTab) "Semua sudah dibaca!" else "Belum ada notifikasi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isUnreadTab) "Hebat, kamu selalu update dengan informasi terbaru." else "Kami akan memberitahu jika ada update penting.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGrey,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Baru saja"
        diff < 3600000 -> "${diff / 60000} menit yang lalu"
        diff < 86400000 -> "${diff / 3600000} jam yang lalu"
        diff < 172800000 -> "Kemarin"
        else -> {
            val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("id", "ID"))
            sdf.format(Date(timestamp))
        }
    }
}