package com.example.nutriscan.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

// Class ini bertugas menggambar bentuk "lembah" di tengah
class BottomBarShape(
    private val cutoutRadius: Float = 50f, // Lebar lengkungan
    private val cutoutDepth: Float = 40f   // Kedalaman lengkungan
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            Path().apply {
                val width = size.width
                val height = size.height

                // Titik tengah secara horizontal
                val centerX = width / 2f

                // Mulai gambar dari pojok kiri atas
                moveTo(0f, 0f)

                // 1. Garis lurus dari kiri sampai sebelum lengkungan
                lineTo(centerX - cutoutRadius - 20f, 0f)

                // 2. Gambar LENGKUNGAN KE BAWAH (Bezier Curve)
                // Ini matematika untuk membuat huruf "U" yang halus
                cubicTo(
                    x1 = centerX - cutoutRadius, y1 = 0f,
                    x2 = centerX - cutoutRadius, y2 = cutoutDepth,
                    x3 = centerX, y3 = cutoutDepth
                )
                cubicTo(
                    x1 = centerX + cutoutRadius, y1 = cutoutDepth,
                    x2 = centerX + cutoutRadius, y2 = 0f,
                    x3 = centerX + cutoutRadius + 20f, y3 = 0f
                )

                // 3. Lanjut garis lurus ke kanan
                lineTo(width, 0f)

                // 4. Garis ke bawah kanan
                lineTo(width, height)

                // 5. Garis ke bawah kiri
                lineTo(0f, height)

                // 6. Tutup jalur
                close()
            }
        )
    }
}