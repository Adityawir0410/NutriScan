package com.example.nutriscan.data.repository

import android.util.Log
import com.example.nutriscan.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor() {
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 150
        }
    )
    
    /**
     * Generate AI recommendation based on user's nutrition data
     * Returns a single sentence advice
     */
    suspend fun generateDailyAdvice(
        totalCalories: Float,
        totalSugar: Float,
        totalSodium: Float,
        targetCalories: Float = 2000f,
        targetSugar: Float = 50f,
        targetSodium: Float = 2300f,
        scanCount: Int = 0
    ): String = withContext(Dispatchers.IO) {
        try {
            val caloriePercent = (totalCalories / targetCalories * 100).toInt()
            val sugarPercent = (totalSugar / targetSugar * 100).toInt()
            val sodiumPercent = (totalSodium / targetSodium * 100).toInt()
            
            val prompt = """
                Anda adalah ahli gizi AI yang memberikan saran singkat dalam bahasa Indonesia.
                
                Data nutrisi hari ini:
                - Kalori: ${totalCalories.toInt()} kcal dari target ${targetCalories.toInt()} kcal ($caloriePercent%)
                - Gula: ${totalSugar.toInt()}g dari target ${targetSugar.toInt()}g ($sugarPercent%)
                - Sodium: ${totalSodium.toInt()}mg dari target ${targetSodium.toInt()}mg ($sodiumPercent%)
                - Jumlah scan makanan: $scanCount kali
                
                Berikan SATU kalimat saran motivasi atau peringatan yang personal, singkat, dan actionable (maksimal 15 kata).
                Jangan gunakan emoji. Fokus pada aspek yang paling perlu diperhatikan.
                
                Contoh:
                - "Konsumsi gula Anda sudah tinggi, coba kurangi minuman manis hari ini"
                - "Asupan kalori bagus, jaga pola makan sehat hingga malam"
                - "Sodium berlebih, hindari makanan olahan untuk sisa hari ini"
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val advice = response.text?.trim() ?: "Jaga pola makan seimbang untuk kesehatan optimal"
            
            Log.d("GeminiRepo", "✅ AI Advice: $advice")
            advice
            
        } catch (e: Exception) {
            Log.e("GeminiRepo", "❌ Gemini API failed: ${e.message}")
            // Fallback advice based on simple rules
            when {
                totalSugar > targetSugar * 0.8f -> "Konsumsi gula mendekati batas, kurangi makanan manis"
                totalSodium > targetSodium * 0.8f -> "Asupan sodium tinggi, batasi makanan olahan"
                totalCalories < targetCalories * 0.5f -> "Kalori masih rendah, pastikan makan cukup"
                totalCalories > targetCalories * 1.2f -> "Kalori berlebih, pertimbangkan porsi lebih kecil"
                else -> "Pola makan Anda hari ini cukup seimbang, pertahankan"
            }
        }
    }
    
    /**
     * Analyze single food scan and generate notification message
     */
    suspend fun analyzeFoodScan(
        foodName: String,
        status: String,
        nutrition: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Analisis makanan ini dan buat notifikasi singkat dalam bahasa Indonesia:
                
                Nama: $foodName
                Status: $status
                Nutrisi: $nutrition
                
                Buat SATU kalimat notifikasi (maksimal 12 kata) yang:
                - Jika status "Sehat": beri pujian positif
                - Jika status "Tidak Sehat": beri peringatan lembut
                - Sebutkan aspek nutrisi yang menonjol
                
                Contoh:
                - "Pilihan sehat! Tinggi protein dan rendah lemak jenuh"
                - "Perhatian: Kadar gula sangat tinggi, batasi konsumsi"
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            response.text?.trim() ?: generateFallbackNotification(status)
            
        } catch (e: Exception) {
            Log.e("GeminiRepo", "❌ Gemini notification failed: ${e.message}")
            generateFallbackNotification(status)
        }
    }
    
    private fun generateFallbackNotification(status: String): String {
        return when (status.lowercase()) {
            "sehat" -> "Makanan sehat terdeteksi! Pilihan nutrisi yang baik"
            "tidak sehat" -> "Makanan tidak sehat terdeteksi, konsumsi dengan bijak"
            else -> "Scan makanan berhasil, lihat detail nutrisi"
        }
    }
}
