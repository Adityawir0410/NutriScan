package com.example.nutriscan.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey
    val id: String, // Firebase document ID
    val userId: String,
    val foodName: String,
    val imageUrl: String,
    val explanation: String,
    val status: String,
    val nutrition: String, // JSON string dari nutrition data
    val date: String,
    val timestamp: Long,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationAddress: String? = null,
    val isSynced: Boolean = false // Track apakah sudah sync ke Firestore
)
