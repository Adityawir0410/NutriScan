package com.example.nutriscan.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.nutriscan.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class) // Perlu untuk TextField
@Composable
fun LoginScreen(navController: NavController) {

    // --- Ini adalah Modul 3: State ---
    // Kita gunakan 'remember' agar nilainya tidak hilang saat recomposition
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // --- Selesai Modul 3 ---

    // --- Ini adalah Modul 2: Layout (Column) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp), // Beri padding di samping
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Selamat Datang!",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Masuk ke akun NutriScan Anda",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Ini adalah Modul 2: Komponen UI (TextField) ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it }, // State (Modul 3) di-update di sini
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(), // Lebar penuh
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it }, // State (Modul 3) di-update di sini
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(), // Lebar penuh
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(), // Sembunyikan password
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Ini adalah Modul 2: Komponen UI (Button) ---
        Button(
            onClick = {
                // TODO: Nanti kita panggil ViewModel di sini
                // Untuk sekarang, kita navigasi ke Home
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth() // Lebar penuh
        ) {
            Text("LOGIN")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
            Text("Belum punya akun? Daftar di sini")
        }
        // --- Selesai Modul 2 ---
    }
}