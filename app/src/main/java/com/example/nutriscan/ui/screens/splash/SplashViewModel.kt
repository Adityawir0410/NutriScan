package com.example.nutriscan.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.example.nutriscan.data.datastore.UserPreferencesManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreferences: UserPreferencesManager,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    /**
     * Check if user is logged in
     * Combines DataStore state with Firebase Auth state
     */
    val isLoggedIn: Flow<Boolean> = combine(
        userPreferences.isLoggedIn,
        userPreferences.userId
    ) { isLoggedInDataStore, userId ->
        // Check both DataStore and Firebase Auth
        val firebaseUser = auth.currentUser
        isLoggedInDataStore && userId != null && firebaseUser != null
    }
}
