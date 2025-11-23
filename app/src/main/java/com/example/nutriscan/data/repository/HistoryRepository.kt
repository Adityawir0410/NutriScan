package com.example.nutriscan.data.repository

import android.graphics.Bitmap
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.nutriscan.BuildConfig
import com.example.nutriscan.ui.screens.scan.FoodResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.nutriscan.domain.common.Result
import com.example.nutriscan.domain.model.ScanHistory

class HistoryRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
    // HAPUS FirebaseStorage dari sini, kita tidak pakai lagi
) {

    suspend fun saveScanResult(bitmap: Bitmap, data: FoodResult) {
        val user = auth.currentUser ?: return

        try {
            // 1. Upload Gambar ke Cloudinary (Ganti Firebase Storage)
            val downloadUrl = uploadToCloudinary(bitmap)

            // 2. Simpan Data ke Firestore (Tetap sama)
            val scanData = hashMapOf(
                "uid" to user.uid,
                "imageUrl" to downloadUrl,
                "nama" to data.nama,
                "penjelasan" to data.penjelasan,
                "status" to data.status,
                "gizi" to data.gizi,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firestore.collection("users")
                .document(user.uid)
                .collection("history")
                .add(scanData)
                .await()

        } catch (e: Exception) {
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

    // --- Fungsi Get History (Tidak Berubah) ---
    fun getScanHistory(): Flow<Result<List<ScanHistory>>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(Result.Error("User tidak login"))
            close()
            return@callbackFlow
        }

        trySend(Result.Loading())

        val subscription = firestore.collection("users")
            .document(user.uid)
            .collection("history")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.Error(error.message ?: "Gagal memuat history"))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val historyList = snapshot.documents.map { doc ->
                        ScanHistory.fromMap(doc.id, doc.data ?: emptyMap())
                    }
                    trySend(Result.Success(historyList))
                }
            }

        awaitClose { subscription.remove() }
    }
}