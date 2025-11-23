package com.example.nutriscan.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutriscan.navigation.Screen
import com.example.nutriscan.ui.components.BottomNavBar
import com.example.nutriscan.ui.components.ScanFAB
import com.example.nutriscan.ui.screens.home.HomeScreen
import com.example.nutriscan.ui.screens.history.HistoryScreen
import com.example.nutriscan.ui.screens.notification.NotificationScreen
import com.example.nutriscan.ui.screens.profile.ProfileScreen
// ScanScreen TIDAK DI-IMPORT DI SINI LAGI

@Composable
fun MainScreen(
    rootNavController: NavController // 1. Tambahkan parameter ini (Remote ke induk)
) {
    val bottomNavController = rememberNavController() // Ini remote untuk tab bawah

    Scaffold(
        floatingActionButton = {
            ScanFAB(
                onClick = {
                    // 2. UBAH INI: Gunakan 'rootNavController' untuk pindah ke Scan
                    // Ini akan menimpa seluruh layar (Full Screen)
                    rootNavController.navigate(Screen.Scan.route) {
                        launchSingleTop = true
                    }
                }
            )
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            BottomNavBar(navController = bottomNavController)
        }
    ) { innerPadding ->

        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(bottomNavController) }
            composable(Screen.History.route) { HistoryScreen(bottomNavController) }
            // 3. HAPUS RUTE SCAN DARI SINI (composable(Screen.Scan.route) { ... })
            composable(Screen.Notification.route) { NotificationScreen(bottomNavController) }
            composable(Screen.Profile.route) { ProfileScreen(bottomNavController) }
        }
    }
}