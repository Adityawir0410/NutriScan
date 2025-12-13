package com.example.nutriscan.data.repository

import android.util.Log
import com.example.nutriscan.data.datastore.UserPreferencesManager
import com.example.nutriscan.domain.common.Result
import com.example.nutriscan.domain.model.User
import com.example.nutriscan.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val userPreferences: UserPreferencesManager,
    private val historyRepository: HistoryRepository // Inject HistoryRepository
) : AuthRepository {

    // Fungsi untuk register
    override fun register(email: String, pass: String): Flow<Result<User>> = flow {
        try {
            emit(Result.Loading())

            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Save login state to DataStore
                userPreferences.saveLoginState(firebaseUser.uid, firebaseUser.email)
                Log.d("AuthRepo", "✅ Login state saved to DataStore")
                
                emit(Result.Success(User(uid = firebaseUser.uid, email = firebaseUser.email)))
            } else {
                emit(Result.Error("Gagal membuat user."))
            }

        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Error tidak diketahui"))
        }
    }

    // Fungsi untuk login
    override fun login(email: String, pass: String): Flow<Result<User>> = flow {
        try {
            emit(Result.Loading())

            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Save login state to DataStore
                userPreferences.saveLoginState(firebaseUser.uid, firebaseUser.email)
                Log.d("AuthRepo", "✅ Login state saved to DataStore")
                
                // Sync Room Database with Firebase on every login
                try {
                    historyRepository.syncFromFirestore()
                    userPreferences.saveLastSyncTimestamp(System.currentTimeMillis())
                    Log.d("AuthRepo", "✅ Room Database synced with Firebase")
                } catch (e: Exception) {
                    Log.e("AuthRepo", "⚠️ Room sync failed (offline?): ${e.message}")
                    // Don't fail login if sync fails (might be offline)
                }
                
                emit(Result.Success(User(uid = firebaseUser.uid, email = firebaseUser.email)))
            } else {
                emit(Result.Error("Gagal login."))
            }

        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Error tidak diketahui"))
        }
    }

    // Fungsi untuk cek user yang sedang login
    override fun getAuthUser(): Flow<User?> = flow {
        // Cek user saat ini dari Firebase
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            emit(User(uid = firebaseUser.uid, email = firebaseUser.email))
        } else {
            emit(null)
        }
    }

    // Fungsi untuk logout
    override suspend fun logout() {
        withContext(Dispatchers.IO) {
            // Clear DataStore login state
            userPreferences.clearLoginState()
            Log.d("AuthRepo", "✅ Login state cleared from DataStore")
            
            // Sign out from Firebase
            auth.signOut()
        }
    }
}