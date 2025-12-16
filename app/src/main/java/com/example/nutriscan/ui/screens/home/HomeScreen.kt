package com.example.nutriscan.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nutriscan.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// --- PALET WARNA MODERN ---
private val PrimaryGreen = Color(0xFF388E3C)
private val PrimaryGreenLight = Color(0xFFE8F5E9)
private val TextDark = Color(0xFF1F2937)
private val TextGrey = Color(0xFF6B7280)
private val BackgroundColor = Color(0xFFF9FAFB)

// Warna Makro (Pastel Modern)
private val ProteinColor = Color(0xFF3B82F6) // Blue
private val ProteinBg = Color(0xFFEFF6FF)
private val FatColor = Color(0xFFEF4444) // Red
private val FatBg = Color(0xFFFEF2F2)
private val CarbColor = Color(0xFFF59E0B) // Amber
private val CarbBg = Color(0xFFFFFBEB)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val uiState by viewModel.uiState.collectAsState()

    // Hitung progress (max 1.0)
    val calorieProgress = if (uiState.metrics.targetCalories > 0) {
        (uiState.metrics.totalCalories / uiState.metrics.targetCalories).coerceIn(0f, 1f)
    } else 0f

    // Format Tanggal (Contoh: "Senin, 16 Des")
    val currentDate = remember {
        try {
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMM", Locale("id", "ID")))
        } catch (e: Exception) {
            "Hari Ini"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // --- 1. HEADER SECTION ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = TextGrey,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Halo, ${currentUser?.email?.split("@")?.get(0)?.capitalize() ?: "User"}!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        fontSize = 22.sp
                    )
                )
            }
            // Avatar Simple
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryGreenLight)
                    .border(1.dp, PrimaryGreen.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser?.email?.first()?.uppercase() ?: "U",
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- 2. MAIN CONTENT ---
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp) // Jarak antar kartu lebih lega
            ) {

                // A. KARTU PROGRESS UTAMA
                item {
                    NutritionProgressCard(uiState.metrics, calorieProgress)
                }

                // B. REKOMENDASI AI
                item {
                    AiInsightCard(advice = uiState.aiAdvice)
                }

                // C. AKSI CEPAT & HISTORY
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Aktivitas",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextDark
                        )
                        QuickActionsGrid(navController)

                        // History Summary (Mini List)
                        if (uiState.metrics.scanCount > 0) {
                            HistorySummaryTile(count = uiState.metrics.scanCount, navController = navController)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// --- COMPONENT: KARTU NUTRISI (REDESIGNED) ---
@Composable
fun NutritionProgressCard(metrics: NutritionMetrics, progress: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Kartu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Energi Harian",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge.copy(color = TextGrey)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LINGKARAN PROGRESS BESAR
            Box(contentAlignment = Alignment.Center) {
                // Background Track
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(160.dp),
                    color = Color(0xFFF3F4F6), // Abu lembut
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round
                )
                // Active Progress
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(160.dp),
                    color = PrimaryGreen,
                    strokeWidth = 14.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = metrics.totalCalories.roundToInt().toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            fontSize = 32.sp
                        )
                    )
                    Text(
                        text = "/ ${metrics.targetCalories.roundToInt()} kkal",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextGrey)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ROW MACROS (CHIPS STYLE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroChip(
                    label = "Protein",
                    value = "${metrics.totalProtein.roundToInt()}g",
                    color = ProteinColor,
                    bgColor = ProteinBg,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                MacroChip(
                    label = "Lemak",
                    value = "${metrics.totalFat.roundToInt()}g",
                    color = FatColor,
                    bgColor = FatBg,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                MacroChip(
                    label = "Karbo",
                    value = "${metrics.totalCarbs.roundToInt()}g",
                    color = CarbColor,
                    bgColor = CarbBg,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// --- COMPONENT: MACRO CHIP (KOTAK KECIL DI BAWAH LINGKARAN) ---
@Composable
fun MacroChip(
    label: String,
    value: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dot Indikator Warna
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = TextGrey, fontSize = 10.sp)
        )
    }
}

// --- COMPONENT: AI INSIGHT ---
@Composable
fun AiInsightCard(advice: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryGreen, Color(0xFF66BB6A)) // Gradient Hijau Segar
                    )
                )
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                // Icon Lampu dalam lingkaran putih transparan
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Saran AI Hari Ini",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (advice.isEmpty()) "Menganalisis nutrisi Anda..." else advice,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }
    }
}

// --- COMPONENT: ACTION BUTTONS ---
@Composable
fun QuickActionsGrid(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tombol Scan (Utama)
        ActionButton(
            modifier = Modifier.weight(1f),
            title = "Scan Makanan",
            icon = Icons.Outlined.QrCodeScanner,
            backgroundColor = TextDark, // Hitam Elegan agar kontras
            iconColor = Color.White,
            onClick = { /* Navigate to Scan */ }
        )

        // Tombol Riwayat (Secondary)
        ActionButton(
            modifier = Modifier.weight(1f),
            title = "Riwayat",
            icon = Icons.Outlined.History,
            backgroundColor = Color.White,
            iconColor = TextDark,
            isBordered = true, // Pakai border
            onClick = {
                navController.navigate(Screen.History.route) { launchSingleTop = true }
            }
        )
    }
}

@Composable
fun ActionButton(
    modifier: Modifier,
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    isBordered: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = if (isBordered) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
        shadowElevation = if (isBordered) 0.dp else 4.dp // Shadow hanya untuk tombol solid
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor
                )
            )
        }
    }
}

// --- COMPONENT: HISTORY SUMMARY ---
@Composable
fun HistorySummaryTile(count: Int, navController: NavController) {
    Surface(
        onClick = { navController.navigate(Screen.History.route) },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Total Scan Hari Ini",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextGrey)
                )
                Text(
                    text = "$count item makanan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextGrey
            )
        }
    }
}