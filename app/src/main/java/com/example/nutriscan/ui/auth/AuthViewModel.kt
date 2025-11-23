package com.example.nutriscan.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutriscan.domain.common.Result
import com.example.nutriscan.domain.usecase.GetAuthUserUseCase
import com.example.nutriscan.domain.usecase.LoginUseCase
import com.example.nutriscan.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // Beritahu Hilt ini adalah ViewModel
class AuthViewModel @Inject constructor(
    // Hilt akan "menyuntikkan" UseCase yang kita buat
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    // _state (Mutable) hanya bisa diubah di dalam ViewModel ini
    private val _state = MutableStateFlow(AuthState())
    // state (Immutable) bisa dibaca oleh UI
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // --- Fungsi untuk mengubah state dari UI ---
    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _state.value = _state.value.copy(confirmPassword = confirmPassword)
    }

    // --- Fungsi untuk Aksi (Event) ---

    fun onLoginClick() {
        // Jalankan di Coroutine (Modul 5)
        viewModelScope.launch {
            val email = _state.value.email
            val password = _state.value.password

            // Panggil UseCase
            loginUseCase(email, password).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, error = null)
                    }
                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            loginSuccess = true, // Tanda sukses
                            error = null
                        )
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message // Tampilkan pesan error
                        )
                    }
                }
            }
        }
    }

    fun onRegisterClick() {
        viewModelScope.launch {
            val email = _state.value.email
            val password = _state.value.password
            val confirmPassword = _state.value.confirmPassword

            // Validasi sederhana
            if (password != confirmPassword) {
                _state.value = _state.value.copy(error = "Password tidak cocok")
                return@launch
            }

            // Panggil UseCase
            registerUseCase(email, password).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, error = null)
                    }
                    is Result.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            registerSuccess = true, // Tanda sukses
                            error = null
                        )
                    }
                    is Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message // Tampilkan pesan error
                        )
                    }
                }
            }
        }
    }
}