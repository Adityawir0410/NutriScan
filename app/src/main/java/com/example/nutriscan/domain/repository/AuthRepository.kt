package com.example.nutriscan.domain.repository

import com.example.nutriscan.domain.model.User
import kotlinx.coroutines.flow.Flow

// Ini adalah "Kontrak" atau "Aturan"
// Kita belum menulis kodenya (itu di layer Data),
// kita hanya mendefinisikan fungsinya.
interface AuthRepository {

    // Fungsi untuk mendapatkan status login user saat ini
    fun getAuthUser(): Flow<User?>

    // Fungsi untuk register (suspend = Coroutine dari Modul 5)
    fun register(email: String, pass: String): Flow<Result<User>> // ✅ BENAR
    fun login(email: String, pass: String): Flow<Result<User>> // ✅ BENAR

    // Fungsi untuk logout
    suspend fun logout()
}