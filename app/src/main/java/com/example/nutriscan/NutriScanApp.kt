package com.example.nutriscan

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NutriScanApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inisialisasi Cloudinary
        val config = HashMap<String, String>()
        config["cloud_name"] = BuildConfig.CLOUDINARY_CLOUD_NAME
        MediaManager.init(this, config)
    }
}