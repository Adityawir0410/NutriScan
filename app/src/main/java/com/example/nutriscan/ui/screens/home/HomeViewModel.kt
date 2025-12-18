package com.example.nutriscan.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.data.repository.GeminiRepository
import com.example.nutriscan.data.repository.HistoryRepository
import com.example.nutriscan.domain.common.Result
import com.example.nutriscan.domain.model.ScanHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class NutritionMetrics(
    val totalCalories: Float = 0f,
    val totalProtein: Float = 0f,
    val totalFat: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalSugar: Float = 0f,
    val totalSodium: Float = 0f,
    val targetCalories: Float = 2000f,
    val targetProtein: Float = 50f,
    val targetFat: Float = 70f,
    val targetCarbs: Float = 300f,
    val targetSugar: Float = 50f,
    val targetSodium: Float = 2300f,
    val scanCount: Int = 0,
    val todayScans: List<ScanHistory> = emptyList()
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val metrics: NutritionMetrics = NutritionMetrics(),
    val aiAdvice: String = "Memuat saran...",
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadTodayData()
    }
    
    fun loadTodayData() {
        viewModelScope.launch {
            historyRepository.getScanHistory()
                .collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                        
                        is Result.Success -> {
                            val allScans = result.data ?: emptyList()
                            
                            Log.d("HomeVM", "Total scans in database: ${allScans.size}")
                            allScans.take(3).forEach { scan ->
                                Log.d("HomeVM", "Sample scan: ${scan.nama}, date=${scan.date}, gizi=${scan.gizi}")
                            }
                            
                            // Filter hanya scan hari ini
                            val todayScans = filterTodayScans(allScans)
                            
                            // Calculate metrics HANYA dari scan hari ini
                            val metrics = calculateMetrics(todayScans)
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                metrics = metrics,
                                error = null
                            )
                            
                            // Generate AI advice based on metrics
                            generateAIAdvice(metrics)
                        }
                        
                        is Result.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                            Log.e("HomeVM", "Error loading data: ${result.message}")
                        }
                    }
                }
        }
    }
    
    private fun filterTodayScans(scans: List<ScanHistory>): List<ScanHistory> {
        // Get today's date in "dd MMM yyyy" format
        val today = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())
        
        Log.d("HomeVM", "Filtering ${scans.size} total scans for today: $today")
        
        val todayScans = scans.filter { scan ->
            // Extract date part before comma (format: "11 Des 2025, 14:30")
            val scanDate = scan.date.split(",").firstOrNull()?.trim() ?: ""
            val matches = scanDate == today
            
            Log.d("HomeVM", "Scan '${scan.nama}': date='$scanDate', today='$today', matches=$matches")
            matches
        }
        
        Log.d("HomeVM", "Filtered to ${todayScans.size} scans for today")
        todayScans.forEach { scan ->
            Log.d("HomeVM", "Today's scan: ${scan.nama} - ${scan.date}")
        }
        
        return todayScans
    }
    
    private fun calculateMetrics(scans: List<ScanHistory>): NutritionMetrics {
        Log.d("HomeVM", "Calculating metrics for ${scans.size} scans")
        
        var totalCalories = 0f
        var totalProtein = 0f
        var totalFat = 0f
        var totalCarbs = 0f
        var totalSugar = 0f
        var totalSodium = 0f
        
        scans.forEach { scan ->
            try {
                // Format gizi: "Estimasi Kalori: 850 kkal, Protein: 45g, Lemak: 38g, Karbo: 90g"
                val giziText = scan.gizi
                Log.d("HomeVM", "Parsing scan: ${scan.nama}, gizi text: $giziText")
                
                // Extract calories
                val caloriesMatch = Regex("Kalori:\\s*([\\d.]+)\\s*kkal", RegexOption.IGNORE_CASE).find(giziText)
                val calories = caloriesMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                totalCalories += calories
                Log.d("HomeVM", "Kalori: $calories")
                
                // Extract protein
                val proteinMatch = Regex("Protein:\\s*([\\d.]+)\\s*g", RegexOption.IGNORE_CASE).find(giziText)
                val protein = proteinMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                totalProtein += protein
                Log.d("HomeVM", "Protein: $protein")
                
                // Extract fat (lemak)
                val fatMatch = Regex("Lemak:\\s*([\\d.]+)\\s*g", RegexOption.IGNORE_CASE).find(giziText)
                val fat = fatMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                totalFat += fat
                Log.d("HomeVM", "Lemak: $fat")
                
                // Extract carbs (karbo/karbohidrat)
                val carbsMatch = Regex("Karbo(?:hidrat)?:\\s*([\\d.]+)\\s*g", RegexOption.IGNORE_CASE).find(giziText)
                val carbs = carbsMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                totalCarbs += carbs
                Log.d("HomeVM", "Karbo: $carbs")
                
                // Extract sugar (gula) if present
                val sugarMatch = Regex("Gula:\\s*([\\d.]+)\\s*g", RegexOption.IGNORE_CASE).find(giziText)
                val sugar = sugarMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                totalSugar += sugar
                
                // Extract sodium (natrium) if present
                val sodiumMatch = Regex("(?:Natrium|Sodium):\\s*([\\d.]+)\\s*mg", RegexOption.IGNORE_CASE).find(giziText)
                val sodium = sodiumMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                totalSodium += sodium
                
            } catch (e: Exception) {
                Log.e("HomeVM", "Error parsing nutrition for ${scan.nama}: ${e.message}")
            }
        }
        
        Log.d("HomeVM", "Total Metrics - Kalori: $totalCalories, Protein: $totalProtein, Lemak: $totalFat, Karbo: $totalCarbs")
        
        return NutritionMetrics(
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalFat = totalFat,
            totalCarbs = totalCarbs,
            totalSugar = totalSugar,
            totalSodium = totalSodium,
            scanCount = scans.size,
            todayScans = scans
        )
    }
    
    private fun parseNutritionValue(value: String): Float {
        return try {
            // Remove units like "g", "mg", "kcal" and parse number
            value.replace(Regex("[^0-9.]"), "").toFloatOrNull() ?: 0f
        } catch (e: Exception) {
            0f
        }
    }
    
    private fun generateAIAdvice(metrics: NutritionMetrics) {
        viewModelScope.launch {
            try {
                val advice = geminiRepository.generateDailyAdvice(
                    totalCalories = metrics.totalCalories,
                    totalSugar = metrics.totalSugar,
                    totalSodium = metrics.totalSodium,
                    targetCalories = metrics.targetCalories,
                    targetSugar = metrics.targetSugar,
                    targetSodium = metrics.targetSodium,
                    scanCount = metrics.scanCount
                )
                
                _uiState.value = _uiState.value.copy(aiAdvice = advice)
                
            } catch (e: Exception) {
                Log.e("HomeVM", "Failed to generate AI advice: ${e.message}")
                
                // Generate fallback based on macros
                val fallback = when {
                    metrics.totalProtein < metrics.targetProtein * 0.5f -> 
                        "Asupan protein masih rendah, tambahkan sumber protein berkualitas"
                    metrics.totalFat > metrics.targetFat * 1.2f -> 
                        "Konsumsi lemak berlebih, kurangi makanan gorengan dan berminyak"
                    metrics.totalCarbs < metrics.targetCarbs * 0.5f -> 
                        "Karbohidrat kurang, pastikan energi tercukupi untuk aktivitas"
                    else -> "Jaga pola makan seimbang untuk kesehatan optimal"
                }
                
                _uiState.value = _uiState.value.copy(aiAdvice = fallback)
            }
        }
    }
    
    fun refreshData() {
        loadTodayData()
    }
}
