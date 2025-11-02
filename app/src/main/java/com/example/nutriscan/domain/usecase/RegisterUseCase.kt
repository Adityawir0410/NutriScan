package com.example.nutriscan.domain.usecase

import com.example.nutriscan.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, pass: String) =
        repository.register(email, pass)
}