package com.example.nutriscan.ui.auth

// Ini adalah data class yang menampung SEMUA state
// untuk layar login dan register.
data class AuthState(
    // Data dari form
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "", // Khusus untuk register

    // Status UI
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val registerSuccess: Boolean = false
)