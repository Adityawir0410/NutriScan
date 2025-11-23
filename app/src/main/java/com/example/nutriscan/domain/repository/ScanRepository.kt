package com.example.nutriscan.domain.repository

import android.graphics.Bitmap
import com.example.nutriscan.domain.common.Result
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    // Fungsi mengirim gambar (Bitmap) ke Gemini dan dapat balasan Teks
    suspend fun analyzeImage(image: Bitmap): Flow<Result<String>>
}