package com.example.nutriscan.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nutriscan.data.local.dao.ScanHistoryDao
import com.example.nutriscan.data.local.entity.ScanHistoryEntity

@Database(
    entities = [ScanHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
}
