package com.example.nutriscan.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")

    // Ini adalah layar pembungkus utama (yang punya bottom bar)
    object MainApp : Screen("main_app")

    // Ini adalah sub-layar di dalam Bottom Bar
    object Home : Screen("home")
    object History : Screen("history")
    object Scan : Screen("scan")
    object Notification : Screen("notification")
    object Profile : Screen("profile")
}