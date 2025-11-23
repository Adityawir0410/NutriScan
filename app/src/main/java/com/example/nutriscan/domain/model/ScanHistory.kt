package com.example.nutriscan.domain.model

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

data class ScanHistory(
    val id: String = "",
    val nama: String = "",
    val status: String = "",
    val imageUrl: String = "",
    val date: String = "",
    val gizi: String = ""
) {
    companion object {
        // Helper untuk mengubah data Firestore menjadi object ScanHistory
        fun fromMap(id: String, data: Map<String, Any>): ScanHistory {
            val timestamp = data["timestamp"] as? Timestamp
            val dateStr = timestamp?.toDate()?.let {
                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(it)
            } ?: "Tanpa Tanggal"

            return ScanHistory(
                id = id,
                nama = (data["nama"] as? String) ?: "Tanpa Nama",
                status = (data["status"] as? String) ?: "-",
                imageUrl = (data["imageUrl"] as? String) ?: "",
                date = dateStr,
                gizi = (data["gizi"] as? String) ?: ""
            )
        }
    }
}