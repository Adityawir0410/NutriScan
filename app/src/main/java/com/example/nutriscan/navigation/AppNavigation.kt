package com.example.nutriscan.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutriscan.ui.screens.home.HomeScreen
import com.example.nutriscan.ui.screens.login.LoginScreen
import com.example.nutriscan.ui.screens.main.MainScreen // Import MainScreen
import com.example.nutriscan.ui.screens.onboarding.OnboardingScreen
import com.example.nutriscan.ui.screens.register.RegisterScreen
import com.example.nutriscan.ui.screens.scan.ScanScreen // Import ScanScreen di sini
import com.example.nutriscan.ui.screens.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController() // Ini adalah rootNavController

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
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

        // Rute Utama (yang ada Bottom Bar-nya)
        composable(Screen.MainApp.route) {
            // Kita kirim navController root ke dalam MainScreen
            MainScreen(rootNavController = navController)
        }

        // RUTE SCAN PINDAH KE SINI (Di luar MainScreen)
        // Hasilnya: Scan akan Full Screen menutupi Bottom Bar
        composable(Screen.Scan.route) {
            ScanScreen(navController = navController)
        }
    }
}