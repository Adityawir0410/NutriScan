package com.example.nutriscan.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nutriscan.domain.model.ScanHistory

// --- WARNA TEMA (Konsisten) ---
private val PrimaryGreen = Color(0xFF388E3C)
private val TextDark = Color(0xFF1F2937)
private val TextGrey = Color(0xFF6B7280)
private val BorderColor = Color(0xFFE5E7EB) // Abu sangat muda untuk border

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.historyState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 1. Background Menjadi Putih
    ) {
        // --- Header Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Riwayat Scan",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            )
        }

        // --- Content Section ---
        when (val currentState = state) {
            is HistoryState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            }
            is HistoryState.Empty -> {
                EmptyHistoryView()
            }
            is HistoryState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = currentState.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                }
            }
            is HistoryState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp)
                ) {
                    items(currentState.data) { historyItem ->
                        HistoryItemCard(
                            item = historyItem,
                            onClick = {
                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("scanHistory", historyItem)
                                navController.navigate(com.example.nutriscan.navigation.Screen.HistoryDetail.route)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: ScanHistory,
    onClick: () -> Unit = {}
) {
    // Card Putih di atas Background Putih memerlukan Border agar terlihat batasnya
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor), // Border tipis elegan
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Gambar Makanan
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3F4F6)) // Placeholder background abu
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = "Food Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    // Placeholder icon jika gambar loading/error (Opsional)
                    error = null
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Informasi Utama
            Column(modifier = Modifier.weight(1f)) {
                // Judul Makanan
                Text(
                    text = item.nama,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontSize = 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Tanggal & Waktu (dengan Icon Kecil)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextGrey,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrey
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lokasi (Jika ada)
                if (item.latitude != null && item.longitude != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryGreen, // Icon lokasi hijau
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.locationAddress?.take(25)?.plus("...") ?: "Lokasi Tersimpan",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGrey,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Badge Status
                StatusBadge(status = item.status)
            }

            // 3. Icon Panah (Chevron) di Kanan
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detail",
                tint = Color.LightGray
            )
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    // Tentukan warna berdasarkan status string
    val (bgColor, textColor) = when {
        status.contains("Sehat", ignoreCase = true) && !status.contains("Tidak", ignoreCase = true) ->
            Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32)) // Hijau
        status.contains("Kurang", ignoreCase = true) || status.contains("Peringatan", ignoreCase = true) ->
            Pair(Color(0xFFFFF3E0), Color(0xFFEF6C00)) // Oranye
        else ->
            Pair(Color(0xFFFFEBEE), Color(0xFFC62828)) // Merah
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyHistoryView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Icon Background Bulat
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.RestaurantMenu,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = Color.LightGray
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Belum Ada Riwayat",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scan makananmu sekarang untuk mulai mencatat!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGrey,
                modifier = Modifier.width(250.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}