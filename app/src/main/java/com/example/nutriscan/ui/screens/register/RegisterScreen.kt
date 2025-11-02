package com.example.nutriscan.ui.screens.register

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
fun RegisterScreen(navController: NavController) {

    // --- Ini adalah Modul 3: State ---
    // Kita buat state untuk setiap field di form registrasi
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    // --- Selesai Modul 3 ---

    // --- Ini adalah Modul 2: Layout (Column) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp), // Beri padding
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Buat Akun Baru",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Silakan isi data di bawah ini",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Ini adalah Modul 2: Komponen UI (TextField) ---
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Konfirmasi Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Ini adalah Modul 2: Komponen UI (Button) ---
        Button(
            onClick = {
                // TODO: Nanti kita panggil ViewModel di sini
                // Untuk sekarang, kita kembali ke Login
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("DAFTAR")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.popBackStack() }) { // popBackStack = kembali
            Text("Sudah punya akun? Masuk")
        }
        // --- Selesai Modul 2 ---
    }
}