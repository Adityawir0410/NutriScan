package com.example.nutriscan.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nutriscan.domain.model.ScanHistory

// --- Palette Warna ---
private val PrimaryGreen = Color(0xFF388E3C)
private val TextDark = Color(0xFF1F2937)
private val TextGrey = Color(0xFF6B7280)
private val BorderColor = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    navController: NavController,
    scanHistory: ScanHistory
) {
    Scaffold(
        containerColor = Color.White, // 1. Background Layar Putih
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Detail Riwayat",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = TextDark,
                        navigationIconContentColor = TextDark
                    )
                )
                // Divider tipis di bawah header
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFF3F4F6))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // 2. GAMBAR UTAMA (Lebih Besar & Rapi)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp) // Sedikit lebih tinggi agar detail terlihat
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF9FAFB)) // Placeholder abu sangat muda
            ) {
                AsyncImage(
                    model = scanHistory.imageUrl,
                    contentDescription = "Hasil Foto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. HEADER: Judul & Status
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = scanHistory.nama,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadgeDetail(status = scanHistory.status)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. PENJELASAN (Tentang Makanan)
            if (scanHistory.penjelasan.isNotEmpty()) {
                SectionTitle(icon = Icons.Default.Info, title = "Tentang Makanan")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = scanHistory.penjelasan,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextGrey,
                        lineHeight = 22.sp
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 5. KARTU INFORMASI GIZI (Putih dengan Border)
            SectionTitle(icon = Icons.Default.Restaurant, title = "Nilai Gizi")
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat style
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val nutrients = scanHistory.gizi.split(",").map { it.trim() }

                    if (nutrients.isNotEmpty() && scanHistory.gizi.isNotEmpty()) {
                        nutrients.chunked(2).forEachIndexed { index, rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowItems.forEach { item ->
                                    val parts = item.split(":")
                                    if (parts.size >= 2) {
                                        NutritionItemDetail(
                                            label = parts[0].trim(),
                                            value = parts[1].trim(),
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Text(item, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    }
                                }
                                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                            // Divider antar baris nutrisi
                            if (index < nutrients.chunked(2).size - 1) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(thickness = 1.dp, color = Color(0xFFF3F4F6))
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    } else {
                        Text(
                            text = "Informasi gizi tidak tersedia.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGrey
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 6. METADATA (Tanggal & Lokasi) - Clean Style
            SectionTitle(icon = Icons.Default.CalendarToday, title = "Detail Scan")
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Tanggal
                MetadataCard(
                    icon = Icons.Default.CalendarToday,
                    label = "Tanggal",
                    value = scanHistory.date
                )

                // Info Lokasi (Jika ada)
                if (scanHistory.latitude != null && scanHistory.longitude != null) {
                    val locationText = if (!scanHistory.locationAddress.isNullOrEmpty()) {
                        scanHistory.locationAddress
                    } else {
                        "${String.format("%.5f", scanHistory.latitude)}, ${String.format("%.5f", scanHistory.longitude)}"
                    }
                    MetadataCard(
                        icon = Icons.Default.LocationOn,
                        label = "Lokasi",
                        value = locationText ?: "Lokasi Tersimpan"
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun SectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
        )
    }
}

@Composable
fun NutritionItemDetail(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextGrey, fontSize = 11.sp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        )
    }
}

@Composable
fun MetadataCard(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White), // Card Putih
        border = BorderStroke(1.dp, BorderColor), // Border Abu Tipis
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextGrey,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextGrey)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = TextDark)
                )
            }
        }
    }
}

@Composable
fun StatusBadgeDetail(status: String) {
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
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}