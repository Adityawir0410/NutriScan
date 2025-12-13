package com.example.nutriscan.ui.screens.scan

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.data.location.LocationHelper
import com.example.nutriscan.data.repository.HistoryRepository
import com.example.nutriscan.domain.common.Result
import com.example.nutriscan.domain.repository.ScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ScanRepository,
    private val historyRepository: HistoryRepository,
    private val locationHelper: LocationHelper // Inject LocationHelper
) : ViewModel() {

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState = _scanState.asStateFlow()

    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            repository.analyzeImage(bitmap).collect { result ->
                when (result) {
                    is Result.Loading -> _scanState.value = ScanState.Loading

                    is Result.Success -> {
                        try {
                            // 1. Bersihkan & Parsing JSON dari Gemini
                            val cleanJson = result.data?.trim()?.replace("```json", "")?.replace("```", "") ?: "{}"
                            val jsonObject = JSONObject(cleanJson)

                            val foodData = FoodResult(
                                nama = jsonObject.optString("nama", "Tidak dikenali"),
                                penjelasan = jsonObject.optString("penjelasan", "Tidak ada data"),
                                status = jsonObject.optString("status", "-"),
                                gizi = jsonObject.optString("gizi", "-")
                            )

                            // 2. Ambil lokasi dan update UI
                            launch(Dispatchers.IO) {
                                try {
                                    Log.d("ScanVM", "Mulai get location...")
                                    val locationData = locationHelper.getCurrentLocation()
                                    
                                    if (locationData != null) {
                                        Log.d("ScanVM", "Location berhasil: ${locationData.latitude}, ${locationData.longitude}")
                                        Log.d("ScanVM", "Address: ${locationData.address}")
                                    } else {
                                        Log.d("ScanVM", "Location unavailable or permission not granted")
                                    }
                                    
                                    // Update UI State dengan location address
                                    _scanState.value = ScanState.Success(foodData, bitmap, locationData?.address)
                                    
                                    // Simpan ke Firebase History di background
                                    Log.d("ScanVM", "Mulai upload ke Firebase...")
                                    historyRepository.saveScanResult(bitmap, foodData, locationData)
                                    Log.d("ScanVM", "BERHASIL SIMPAN KE HISTORY! ✅")
                                } catch (e: Exception) {
                                    Log.e("ScanVM", "Error: ${e.message}")
                                    e.printStackTrace()
                                    // Tetap tampilkan hasil scan meski tanpa lokasi
                                    _scanState.value = ScanState.Success(foodData, bitmap, null)
                                }
                            }

                        } catch (e: Exception) {
                            _scanState.value = ScanState.Error("Gagal membaca data makanan.")
                        }
                    }

                    is Result.Error -> _scanState.value = ScanState.Error(result.message ?: "Error")
                }
            }
        }
    }

    fun resetState() {
        _scanState.value = ScanState.Idle
    }
}

// State UI
sealed class ScanState {
    object Idle : ScanState()
    object Loading : ScanState()
    // Update: Tambahkan 'image: Bitmap' dan 'locationAddress' di sini
    data class Success(val data: FoodResult, val image: Bitmap, val locationAddress: String? = null) : ScanState()
    data class Error(val message: String) : ScanState()
}

// Wadah data hasil scan
data class FoodResult(
    val nama: String,
    val penjelasan: String,
    val status: String,
    val gizi: String
)