package com.example.nutriscan.navigation

// Definisikan semua rute/layar di aplikasi Anda
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    // Nanti kita bisa tambahkan layar lain di sini,
    // misalnya Screen.ScanResult
}