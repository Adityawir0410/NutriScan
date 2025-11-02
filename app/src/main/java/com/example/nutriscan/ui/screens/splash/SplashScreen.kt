package com.example.nutriscan.ui.screens.splash

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun SplashScreen(navController: NavController) {
    // Nanti kita tambahkan logic di sini
    // Untuk sekarang, kita langsung lompat ke Login
    navController.navigate(com.example.nutriscan.navigation.Screen.Login.route) {
        popUpTo(com.example.nutriscan.navigation.Screen.Splash.route) { inclusive = true }
    }
    Text("Ini Splash Screen")
}