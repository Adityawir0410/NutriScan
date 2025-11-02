package com.example.nutriscan.data.repository

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

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    // Fungsi untuk register
    override fun register(email: String, pass: String): Flow<Result<User>> = flow { // ✅ BENAR
        // Ini adalah Coroutine (Modul 5) [cite: 682]
        try {
            // 1. Kirim status Loading
            emit(Result.Loading())

            // 2. Coba buat user baru di Firebase
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // 3. Jika sukses, kirim data User
                emit(Result.Success(User(uid = firebaseUser.uid, email = firebaseUser.email)))
            } else {
                emit(Result.Error("Gagal membuat user."))
            }

        } catch (e: Exception) {
            // 3. Jika gagal (misal: email sudah terdaftar), kirim pesan error
            emit(Result.Error(e.message ?: "Error tidak diketahui"))
        }
    }

    // Fungsi untuk login
    override fun login(email: String, pass: String): Flow<Result<User>> = flow {
        try {
            // 1. Kirim status Loading
            emit(Result.Loading())

            // 2. Coba login ke Firebase
            val authResult = auth.signInWithEmailAndPassword(email, pass).await() // Modul 5 [cite: 667] (await)
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // 3. Jika sukses, kirim data User
                emit(Result.Success(User(uid = firebaseUser.uid, email = firebaseUser.email)))
            } else {
                emit(Result.Error("Gagal login."))
            }

        } catch (e: Exception) {
            // 3. Jika gagal (misal: password salah), kirim pesan error
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
        // Jalankan di thread IO (Modul 5)
        withContext(Dispatchers.IO) {
            auth.signOut()
        }
    }
}