package com.example.nutriscan.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.nutriscan.navigation.Screen
import com.example.nutriscan.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel() // 1. Minta ViewModel ke Hilt
) {
    // 2. Ambil state dari ViewModel
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // 3. Efek untuk Navigasi & Error
    LaunchedEffect(key1 = state.loginSuccess, key2 = state.error) {
        if (state.loginSuccess) {
            Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()

            // ✅ PERBAIKAN: Arahkan ke MainApp (Wadah Utama)
            navController.navigate(Screen.MainApp.route) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
        if (state.error != null) {
            // Jika gagal, tampilkan pesan error
            Toast.makeText(context, state.error, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ... (Teks "Selamat Datang!" biarkan saja) ...
        Text(
            text = "Selamat Datang!",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Masuk ke akun NutriScan Anda",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email, // 4. Ganti value
            onValueChange = viewModel::onEmailChange, // 5. Ganti onValueChange
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = state.error != null // Tampilkan error jika ada
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password, // 4. Ganti value
            onValueChange = viewModel::onPasswordChange, // 5. Ganti onValueChange
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = state.error != null // Tampilkan error jika ada
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = viewModel::onLoginClick, // 6. Ganti onClick
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading // 7. Matikan tombol saat loading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("LOGIN")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
            Text("Belum punya akun? Daftar di sini")
        }
    }
}