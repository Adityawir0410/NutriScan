package com.example.nutriscan.domain.usecase

import com.example.nutriscan.domain.repository.AuthRepository
import javax.inject.Inject

// @Inject akan kita pakai nanti untuk Hilt
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    // 'operator fun invoke' agar class ini bisa dipanggil sbg fungsi
    suspend operator fun invoke(email: String, pass: String) =
        repository.login(email, pass)
}