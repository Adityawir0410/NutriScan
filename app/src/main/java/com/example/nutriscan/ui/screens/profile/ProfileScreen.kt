package com.example.nutriscan.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nutriscan.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

// --- PALET WARNA ---
private val PrimaryGreen = Color(0xFF388E3C)
private val LightGreenBg = Color(0xFFE8F5E9)
private val TextDark = Color(0xFF1F2937)
private val TextGrey = Color(0xFF6B7280)
private val DangerColor = Color(0xFFEF4444) // Merah untuk Logout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val logoutState by viewModel.logoutState.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Efek saat Logout Sukses
    LaunchedEffect(logoutState) {
        if (logoutState is LogoutState.Success) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
            viewModel.resetLogoutState()
        }
    }

    // --- LOGOUT BOTTOM SHEET ---
    if (showLogoutDialog) {
        ModalBottomSheet(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon Logout Besar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)), // Merah Sangat Muda
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = DangerColor
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Konfirmasi Logout",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Apakah Anda yakin ingin keluar dari akun ini? Sesi Anda akan berakhir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGrey,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Tombol Logout (Merah)
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
                    shape = RoundedCornerShape(16.dp),
                    enabled = logoutState !is LogoutState.Loading
                ) {
                    if (logoutState is LogoutState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text(
                            text = "Ya, Keluar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol Batal
                TextButton(
                    onClick = { showLogoutDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Batal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGrey
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- KONTEN UTAMA ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Background Putih
            .verticalScroll(rememberScrollState())
    ) {
        // 1. PROFILE HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar dengan Border Hijau
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, PrimaryGreen, CircleShape)
                    .padding(4.dp) // Gap antara border dan foto
                    .clip(CircleShape)
                    .background(LightGreenBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser?.email?.first()?.uppercase() ?: "U",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentUser?.email?.split("@")?.get(0)?.replaceFirstChar { it.uppercase() } ?: "Pengguna",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            )

            Text(
                text = currentUser?.email ?: "email@example.com",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextGrey)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Edit Profil
            OutlinedButton(
                onClick = { /* Navigasi ke Edit Profil */ },
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profil", fontWeight = FontWeight.SemiBold)
            }
        }

        Divider(color = Color(0xFFF3F4F6), thickness = 8.dp) // Pemisah Section Tebal

        // 2. MENU OPTIONS
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Pengaturan Akun",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ProfileMenuItem(
                icon = Icons.Outlined.Notifications,
                title = "Notifikasi",
                onClick = { /* Navigate */ }
            )

            ProfileMenuItem(
                icon = Icons.Outlined.Lock,
                title = "Keamanan & Privasi",
                onClick = { /* Navigate */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Lainnya",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDark),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ProfileMenuItem(
                icon = Icons.Outlined.Help,
                title = "Bantuan & Dukungan",
                onClick = { /* Navigate */ }
            )

            ProfileMenuItem(
                icon = Icons.Outlined.Info,
                title = "Tentang Aplikasi",
                onClick = { /* Navigate */ },
                isLastItem = true // Item terakhir tidak perlu divider
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. TOMBOL LOGOUT (Di paling bawah)
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFEE2E2), // Merah Muda
                contentColor = DangerColor
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// --- KOMPONEN ITEM MENU ---
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isLastItem: Boolean = false
) {
    Column {
        Surface(
            onClick = onClick,
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon dalam Lingkaran
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF9FAFB)), // Abu sangat muda
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextDark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextDark
                    ),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        }

        // Garis pemisah antar item (Kecuali item terakhir)
        if (!isLastItem) {
            HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(start = 60.dp))
        }
    }
}