package com.example.nutriscan.ui.components

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ScanFAB(
    onClick: () -> Unit
) {
    LargeFloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFF4CAF50),
        contentColor = Color.White,
        shape = RoundedCornerShape(26.dp), // Kotak tumpul
        modifier = Modifier
            .size(72.dp) // Ukuran tombol
            // UBAH POSISI DISINI:
            // Sesuaikan 'y' agar pas di tengah lengkungan.
            // Coba 40.dp atau 45.dp. Kalau kurang pas, mainkan angka ini.
            .offset(y = 45.dp)
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scan",
            modifier = Modifier.size(36.dp)
        )
    }
}