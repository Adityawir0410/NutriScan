package com.example.nutriscan.data.local.dao

import androidx.room.*
import com.example.nutriscan.data.local.entity.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    
    /**
     * Get all history for a user, ordered by timestamp descending
     */
    @Query("SELECT * FROM scan_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHistoryByUserId(userId: String): Flow<List<ScanHistoryEntity>>
    
    /**
     * Get single history item by ID
     */
    @Query("SELECT * FROM scan_history WHERE id = :historyId LIMIT 1")
    suspend fun getHistoryById(historyId: String): ScanHistoryEntity?
    
    /**
     * Insert or replace history item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ScanHistoryEntity)
    
    /**
     * Insert multiple history items
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistories(histories: List<ScanHistoryEntity>)
    
    /**
     * Delete history item
     */
    @Delete
    suspend fun deleteHistory(history: ScanHistoryEntity)
    
    /**
     * Delete all history for a user
     */
    @Query("DELETE FROM scan_history WHERE userId = :userId")
    suspend fun deleteAllHistoryByUserId(userId: String)
    
    /**
     * Get unsynced history items (untuk sync ke Firestore)
     */
    @Query("SELECT * FROM scan_history WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedHistory(userId: String): List<ScanHistoryEntity>
    
    /**
     * Mark history as synced
     */
    @Query("UPDATE scan_history SET isSynced = 1 WHERE id = :historyId")
    suspend fun markAsSynced(historyId: String)
    
    /**
     * Get history count for a user
     */
    @Query("SELECT COUNT(*) FROM scan_history WHERE userId = :userId")
    suspend fun getHistoryCount(userId: String): Int
}
