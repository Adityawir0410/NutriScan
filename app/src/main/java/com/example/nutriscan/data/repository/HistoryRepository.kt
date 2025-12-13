package com.example.nutriscan.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.nutriscan.BuildConfig
import com.example.nutriscan.data.local.dao.ScanHistoryDao
import com.example.nutriscan.data.local.entity.ScanHistoryEntity
import com.example.nutriscan.data.location.LocationData
import com.example.nutriscan.ui.screens.scan.FoodResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.nutriscan.domain.common.Result
import com.example.nutriscan.domain.model.ScanHistory
import kotlinx.coroutines.withContext

class HistoryRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val scanHistoryDao: ScanHistoryDao // Inject Room DAO
) {

    /**
     * Save scan result with hybrid strategy:
     * 1. Save to Room immediately (offline-first)
     * 2. Upload to Cloudinary and Firestore in background
     */
    suspend fun saveScanResult(bitmap: Bitmap, data: FoodResult, locationData: LocationData? = null) {
        val user = auth.currentUser ?: return
        val historyId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val dateString = dateFormat.format(Date(timestamp))

        try {
            // 1. Save to Room first (offline-first, fast)
            val tempEntity = ScanHistoryEntity(
                id = historyId,
                userId = user.uid,
                foodName = data.nama,
                imageUrl = "", // Will be updated after Cloudinary upload
                explanation = data.penjelasan,
                status = data.status,
                nutrition = data.gizi,
                date = dateString,
                timestamp = timestamp,
                latitude = locationData?.latitude,
                longitude = locationData?.longitude,
                locationAddress = locationData?.address,
                isSynced = false
            )
            scanHistoryDao.insertHistory(tempEntity)
            Log.d("HistoryRepo", "✅ Saved to Room (offline)")

            // 2. Upload to Cloudinary (can fail if offline)
            val downloadUrl = try {
                uploadToCloudinary(bitmap)
            } catch (e: Exception) {
                Log.e("HistoryRepo", "❌ Cloudinary upload failed (offline?): ${e.message}")
                "" // Empty URL if offline
            }

            // 3. Update Room with Cloudinary URL
            if (downloadUrl.isNotEmpty()) {
                scanHistoryDao.insertHistory(tempEntity.copy(imageUrl = downloadUrl))
                Log.d("HistoryRepo", "✅ Updated Room with Cloudinary URL")
            }

            // 4. Sync to Firestore (can fail if offline)
            try {
                val scanData = hashMapOf(
                    "uid" to user.uid,
                    "imageUrl" to downloadUrl,
                    "nama" to data.nama,
                    "penjelasan" to data.penjelasan,
                    "status" to data.status,
                    "gizi" to data.gizi,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                
                locationData?.let {
                    scanData["latitude"] = it.latitude
                    scanData["longitude"] = it.longitude
                    scanData["locationAddress"] = it.address ?: ""
                }

                firestore.collection("users")
                    .document(user.uid)
                    .collection("history")
                    .document(historyId) // Use same ID as Room
                    .set(scanData)
                    .await()

                // Mark as synced in Room
                scanHistoryDao.markAsSynced(historyId)
                Log.d("HistoryRepo", "✅ Synced to Firestore")
            } catch (e: Exception) {
                Log.e("HistoryRepo", "❌ Firestore sync failed (offline?): ${e.message}")
                // Data still in Room, will sync later
            }

        } catch (e: Exception) {
            Log.e("HistoryRepo", "❌ Save failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    // Fungsi bantuan untuk Upload ke Cloudinary
    private suspend fun uploadToCloudinary(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        // Konversi Bitmap ke ByteArray
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()

        val requestId = MediaManager.get().upload(byteArray)
            .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET) // Pakai preset dari local.properties
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // Ambil URL gambar yang aman (https)
                    val url = resultData["secure_url"] as? String ?: ""
                    // Lanjutkan proses (resume)
                    continuation.resume(url)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    // Batalkan proses jika error
                    continuation.resumeWithException(Exception("Cloudinary Error: ${error.description}"))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    /**
     * Force sync Room Database with Firestore
     * Called on every login to ensure fresh data
     */
    suspend fun syncFromFirestore() {
        val user = auth.currentUser ?: return
        
        withContext(Dispatchers.IO) {
            try {
                Log.d("HistoryRepo", "🔄 Force syncing from Firestore...")
                
                // 1. Clear all existing Room data for this user
                scanHistoryDao.deleteAllHistoryByUserId(user.uid)
                Log.d("HistoryRepo", "🗑️ Cleared old Room data")
                
                // 2. Fetch all data from Firestore
                val snapshot = firestore.collection("users")
                    .document(user.uid)
                    .collection("history")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val firestoreData = snapshot.documents.map { doc ->
                    val data = doc.data ?: emptyMap()
                    val timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                    val dateString = dateFormat.format(Date(timestamp))

                    ScanHistoryEntity(
                        id = doc.id,
                        userId = user.uid,
                        foodName = data["nama"] as? String ?: "",
                        imageUrl = data["imageUrl"] as? String ?: "",
                        explanation = data["penjelasan"] as? String ?: "",
                        status = data["status"] as? String ?: "",
                        nutrition = data["gizi"] as? String ?: "",
                        date = dateString,
                        timestamp = timestamp,
                        latitude = data["latitude"] as? Double,
                        longitude = data["longitude"] as? Double,
                        locationAddress = data["locationAddress"] as? String,
                        isSynced = true
                    )
                }

                // 3. Insert fresh data from Firestore to Room
                scanHistoryDao.insertHistories(firestoreData)
                Log.d("HistoryRepo", "✅ Force sync complete: ${firestoreData.size} items from Firestore")
                
            } catch (e: Exception) {
                Log.e("HistoryRepo", "❌ Force sync failed: ${e.message}")
                throw e
            }
        }
    }
    
    /**
     * Get scan history with hybrid strategy:
     * 1. Load from Room immediately (offline-first)
     * 2. Keep syncing from Firestore in background
     * Note: Call syncFromFirestore() on login to ensure fresh data
     */
    fun getScanHistory(): Flow<Result<List<ScanHistory>>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(Result.Error("User tidak login"))
            close()
            return@callbackFlow
        }

        trySend(Result.Loading())

        // Load from Room immediately (offline-first)
        withContext(Dispatchers.IO) {
            scanHistoryDao.getHistoryByUserId(user.uid).collect { entities ->
                val historyList = entities.map { entity ->
                    ScanHistory(
                        id = entity.id,
                        nama = entity.foodName,
                        imageUrl = entity.imageUrl,
                        penjelasan = entity.explanation,
                        status = entity.status,
                        gizi = entity.nutrition,
                        date = entity.date,
                        latitude = entity.latitude,
                        longitude = entity.longitude,
                        locationAddress = entity.locationAddress
                    )
                }
                trySend(Result.Success(historyList))
                Log.d("HistoryRepo", "✅ Loaded ${historyList.size} items from Room")
            }
        }

        // Continue syncing from Firestore in background (for real-time updates)
        val firestoreListener = firestore.collection("users")
            .document(user.uid)
            .collection("history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HistoryRepo", "❌ Firestore listener error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // Background sync: update Room with latest Firestore data
                    CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                        try {
                            val firestoreData = snapshot.documents.map { doc ->
                                val data = doc.data ?: emptyMap()
                                val timestamp = (data["timestamp"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
                                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                                val dateString = dateFormat.format(Date(timestamp))

                                ScanHistoryEntity(
                                    id = doc.id,
                                    userId = user.uid,
                                    foodName = data["nama"] as? String ?: "",
                                    imageUrl = data["imageUrl"] as? String ?: "",
                                    explanation = data["penjelasan"] as? String ?: "",
                                    status = data["status"] as? String ?: "",
                                    nutrition = data["gizi"] as? String ?: "",
                                    date = dateString,
                                    timestamp = timestamp,
                                    latitude = data["latitude"] as? Double,
                                    longitude = data["longitude"] as? Double,
                                    locationAddress = data["locationAddress"] as? String,
                                    isSynced = true
                                )
                            }
                            scanHistoryDao.insertHistories(firestoreData)
                            Log.d("HistoryRepo", "✅ Background sync: ${firestoreData.size} items updated")
                        } catch (e: Exception) {
                            Log.e("HistoryRepo", "❌ Background sync failed: ${e.message}")
                        }
                    }
                }
            }

        awaitClose { 
            firestoreListener.remove()
            Log.d("HistoryRepo", "🔌 Firestore listener removed")
        }
    }
}