package com.example.nutriscan.data.di

import android.content.Context
import androidx.room.Room
import com.example.nutriscan.data.datastore.UserPreferencesManager
import com.example.nutriscan.data.local.AppDatabase
import com.example.nutriscan.data.local.dao.ScanHistoryDao
import com.example.nutriscan.data.notification.NotificationHelper
import com.example.nutriscan.data.notification.NotificationPreferences
import com.example.nutriscan.data.repository.AuthRepositoryImpl
import com.example.nutriscan.data.repository.GeminiRepository
import com.example.nutriscan.data.repository.HistoryRepository
import com.example.nutriscan.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Berlaku selama aplikasi hidup
object AppModule {

    // 1. Cara menyediakan FirebaseAuth (dari Modul 4)
    @Provides
    @Singleton // Hanya dibuat sekali
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    
    // Room Database
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "nutriscan_database"
        )
            .fallbackToDestructiveMigration() // Reset database on schema change (dev only)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideScanHistoryDao(database: AppDatabase): ScanHistoryDao {
        return database.scanHistoryDao()
    }
    
    // DataStore untuk persistent login
    @Provides
    @Singleton
    fun provideUserPreferencesManager(@ApplicationContext context: Context): UserPreferencesManager {
        return UserPreferencesManager(context)
    }
    
    // HistoryRepository untuk sync Room dengan Firebase
    @Provides
    @Singleton
    fun provideHistoryRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        scanHistoryDao: ScanHistoryDao
    ): HistoryRepository {
        return HistoryRepository(auth, firestore, scanHistoryDao)
    }
    
    // GeminiRepository untuk AI recommendations
    @Provides
    @Singleton
    fun provideGeminiRepository(): GeminiRepository {
        return GeminiRepository()
    }
    
    // NotificationHelper for push notifications
    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }
    
    // NotificationPreferences for persistent read status
    @Provides
    @Singleton
    fun provideNotificationPreferences(@ApplicationContext context: Context): NotificationPreferences {
        return NotificationPreferences(context)
    }
    
    // 2. Cara menyediakan AuthRepository
    // INI ADALAH INSTRUKSI YANG HILANG
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        userPreferences: UserPreferencesManager,
        historyRepository: HistoryRepository
    ): AuthRepository {
        // Saat ada yang minta AuthRepository (interface),
        // Hilt akan memberikan AuthRepositoryImpl (implementasinya)
        return AuthRepositoryImpl(auth, userPreferences, historyRepository)
    }

    @Provides
    @Singleton
    fun provideScanRepository(): com.example.nutriscan.domain.repository.ScanRepository {
        return com.example.nutriscan.data.repository.ScanRepositoryImpl()
    }
}