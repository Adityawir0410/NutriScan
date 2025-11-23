package com.example.nutriscan.data.di

import com.example.nutriscan.data.repository.AuthRepositoryImpl
import com.example.nutriscan.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    // 2. Cara menyediakan AuthRepository
    // INI ADALAH INSTRUKSI YANG HILANG
    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        // Saat ada yang minta AuthRepository (interface),
        // Hilt akan memberikan AuthRepositoryImpl (implementasinya)
        return AuthRepositoryImpl(auth)
    }

    @Provides
    @Singleton
    fun provideScanRepository(): com.example.nutriscan.domain.repository.ScanRepository {
        return com.example.nutriscan.data.repository.ScanRepositoryImpl()
    }
}