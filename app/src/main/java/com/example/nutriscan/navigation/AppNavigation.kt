package com.example.nutriscan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutriscan.ui.screens.home.HomeScreen
import com.example.nutriscan.ui.screens.login.LoginScreen
import com.example.nutriscan.ui.screens.onboarding.OnboardingScreen
import com.example.nutriscan.ui.screens.register.RegisterScreen
import com.example.nutriscan.ui.screens.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // NavHost adalah mesin yang mengatur layar mana yang tampil
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route // <-- Aplikasi mulai dari Splash
    ) {
        // Daftarkan semua layar Anda di sini

        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
    }
}