package com.example.nutriscan.data.repository

import android.graphics.Bitmap
import com.example.nutriscan.domain.common.Result
import com.example.nutriscan.domain.repository.ScanRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ScanRepositoryImpl @Inject constructor() : ScanRepository {

    override suspend fun analyzeImage(image: Bitmap): Flow<Result<String>> = flow {
        emit(Result.Loading())

        try {
            // Pindah ke IO Thread untuk networking
            val responseText = withContext(Dispatchers.IO) {
                // 1. Setup Gemini
                // GANTI "PASTE_API_KEY_DISINI" DENGAN KODE DARI GOOGLE AI STUDIO
                // ✅ Gunakan BuildConfig yang digenerate otomatis
                val apiKey = com.example.nutriscan.BuildConfig.GEMINI_API_KEY

                val generativeModel = GenerativeModel(
                    // GANTI MENJADI INI:
                    modelName = "gemini-2.5-flash",
                    apiKey = apiKey
                )

                // 2. Siapkan Prompt
                // 2. Siapkan Prompt Khusus
                val inputContent = content {
                    image(image)
                    text("""
                        Analisis gambar makanan ini.
                        Berikan output HANYA dalam format JSON murni (tanpa markdown ```json) dengan struktur kunci persis seperti ini:
                        {
                            "nama": "Nama Makanan (contoh: Nasi Goreng)",
                            "penjelasan": "Penjelasan singkat tentang makanan ini, rasanya, dan teksturnya.",
                            "status": "Sehat / Kurang Sehat / Tidak Sehat",
                            "gizi": "Estimasi Kalori: ... kkal, Protein: ...g, Lemak: ...g, Karbo: ...g"
                        }
                        Gunakan Bahasa Indonesia yang santai tapi informatif.
                    """.trimIndent())
                }

                // 3. Kirim ke Gemini
                val response = generativeModel.generateContent(inputContent)
                response.text ?: "Tidak ada respon dari AI."
            }

            emit(Result.Success(responseText))

        } catch (e: Exception) {
            emit(Result.Error("Gagal analisis: ${e.message}"))
        }
    }
}