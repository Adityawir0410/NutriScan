package com.example.nutriscan.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.nutriscan.navigation.Screen

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val primaryGreen = Color(0xFF4CAF50)

    // Fungsi bantuan agar Navigasi LANCAR JAYA
    // Fungsi bantuan agar Navigasi LANCAR JAYA
    fun navigateToTab(route: String) {
        // Jangan navigasi ulang jika sudah di tab yang sama
        if (currentRoute != route) {
            navController.navigate(route) {
                // 1. Bersihkan tumpukan sampai ke Home (Start Destination)
                //    agar tombol Back tidak muter-muter
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // 2. Jangan buat copy layar yang sama (Single Top)
                launchSingleTop = true
                // 3. Restore state (posisi scroll, dll)
                restoreState = true
            }
        }
    }

    val customShape = BottomBarShape(cutoutRadius = 130f, cutoutDepth = 90f)

    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = modifier
            .height(112.dp)
            .shadow(elevation = 10.dp, shape = customShape, clip = true)
            .clip(customShape)
    ) {
        // --- KIRI 1: HOME ---
        val isHomeSelected = currentRoute == Screen.Home.route
        NavigationBarItem(
            selected = isHomeSelected,
            onClick = { navigateToTab(Screen.Home.route) }, // Pakai fungsi bantuan
            icon = {
                Icon(
                    imageVector = if (isHomeSelected) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(30.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryGreen,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )

        // --- KIRI 2: HISTORY ---
        val isHistorySelected = currentRoute == Screen.History.route
        NavigationBarItem(
            selected = isHistorySelected,
            onClick = { navigateToTab(Screen.History.route) }, // Pakai fungsi bantuan
            icon = {
                Icon(
                    imageVector = if (isHistorySelected) Icons.Default.History else Icons.Outlined.History,
                    contentDescription = "History",
                    modifier = Modifier.size(30.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryGreen,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )

        // --- TENGAH (Space) ---
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = {},
            enabled = false,
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )

        // --- KANAN 1: NOTIF ---
        val isNotifSelected = currentRoute == Screen.Notification.route
        NavigationBarItem(
            selected = isNotifSelected,
            onClick = { navigateToTab(Screen.Notification.route) }, // Pakai fungsi bantuan
            icon = {
                Icon(
                    imageVector = if (isNotifSelected) Icons.Default.Notifications else Icons.Outlined.Notifications,
                    contentDescription = "Notif",
                    modifier = Modifier.size(30.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryGreen,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )

        // --- KANAN 2: PROFILE ---
        val isProfileSelected = currentRoute == Screen.Profile.route
        NavigationBarItem(
            selected = isProfileSelected,
            onClick = { navigateToTab(Screen.Profile.route) }, // Pakai fungsi bantuan
            icon = {
                Icon(
                    imageVector = if (isProfileSelected) Icons.Default.Person else Icons.Outlined.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(30.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = primaryGreen,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
    }
}