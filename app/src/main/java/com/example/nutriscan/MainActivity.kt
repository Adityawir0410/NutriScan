package com.example.nutriscan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nutriscan.navigation.AppNavigation
import com.example.nutriscan.ui.theme.NutriScanTheme
import dagger.hilt.android.AndroidEntryPoint // <-- IMPORT INI

@AndroidEntryPoint // <-- TAMBAHKAN INI (WAJIB)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriScanTheme {
                AppNavigation()
            }
        }
    }
}