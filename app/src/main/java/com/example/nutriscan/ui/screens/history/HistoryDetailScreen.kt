package com.example.nutriscan.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.nutriscan.domain.model.ScanHistory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    navController: NavController,
    scanHistory: ScanHistory
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Detail Riwayat") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color.Black,
                        navigationIconContentColor = Color.Black
                    )
                )
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
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
            // 1. PREVIEW GAMBAR
            AsyncImage(
                model = scanHistory.imageUrl,
                contentDescription = "Hasil Foto",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. HEADER (Nama & Status)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nama Makanan
                Text(
                    text = scanHistory.nama,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                // Badge Status
                val statusColor = when {
                    scanHistory.status.contains("Sehat", ignoreCase = true) && !scanHistory.status.contains("Tidak", ignoreCase = true) -> Color(0xFF4CAF50)
                    scanHistory.status.contains("Kurang", ignoreCase = true) -> Color(0xFFFF9800)
                    else -> Color(0xFFE91E63)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = scanHistory.status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. PENJELASAN (Tentang Makanan Ini)
            if (scanHistory.penjelasan.isNotEmpty()) {
                Text(
                    "Tentang Makanan Ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scanHistory.penjelasan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. KANDUNGAN GIZI
            Text(
                "Informasi Nilai Gizi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                        NutritionItem(
                                            label = parts[0].trim(),
                                            value = parts[1].trim(),
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Text(
                                            item,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            if (index < nutrients.chunked(2).size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    } else {
                        Text(
                            text = if (scanHistory.gizi.isEmpty()) "Tidak ada informasi gizi tersedia." else scanHistory.gizi,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (scanHistory.gizi.isEmpty()) Color.Gray else Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Info Tanggal Scan
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tanggal scan:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = scanHistory.date,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // 6. Info Lokasi Scan (jika tersedia)
            if (scanHistory.latitude != null && scanHistory.longitude != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Lokasi",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lokasi scan:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Prioritaskan nama tempat, koordinat hanya fallback
                        if (!scanHistory.locationAddress.isNullOrEmpty()) {
                            Text(
                                text = scanHistory.locationAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        } else {
                            // Fallback: tampilkan koordinat jika address tidak ada
                            Text(
                                text = "${String.format("%.6f", scanHistory.latitude)}, ${String.format("%.6f", scanHistory.longitude)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// Komponen Kecil untuk Item Gizi
@Composable
fun NutritionItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
    }
}
