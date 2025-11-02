package com.example.nutriscan.domain.usecase

import com.example.nutriscan.domain.repository.AuthRepository
import javax.inject.Inject

class GetAuthUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() = repository.getAuthUser()
}