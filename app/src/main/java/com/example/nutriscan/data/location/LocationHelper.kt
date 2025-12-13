package com.example.nutriscan.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String?
)

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get current location with address
     * Returns null if permission not granted or location unavailable
     */
    suspend fun getCurrentLocation(): LocationData? {
        if (!hasLocationPermission()) {
            return null
        }

        return try {
            // Get last known location first (faster)
            val lastLocation = getLastKnownLocation()
            if (lastLocation != null) {
                val address = getAddressFromLocation(lastLocation.latitude, lastLocation.longitude)
                return LocationData(
                    latitude = lastLocation.latitude,
                    longitude = lastLocation.longitude,
                    address = address
                )
            }

            // If no last location, get current location
            val currentLocation = getCurrentLocationFresh()
            if (currentLocation != null) {
                val address = getAddressFromLocation(currentLocation.latitude, currentLocation.longitude)
                return LocationData(
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    address = address
                )
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get last known location (fast, may be outdated)
     */
    @Suppress("MissingPermission")
    private suspend fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null

        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get fresh current location (more accurate, but takes longer)
     */
    @Suppress("MissingPermission")
    private suspend fun getCurrentLocationFresh(): Location? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                continuation.resume(location)
            }.addOnFailureListener {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }

    /**
     * Convert coordinates to human-readable address
     */
    private suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+, use async API with coroutine
                suspendCancellableCoroutine { continuation ->
                    try {
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            val resultAddress = addresses.firstOrNull()?.let { address ->
                                buildAddressString(address)
                            }
                            continuation.resume(resultAddress)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        continuation.resume(null)
                    }
                }
            } else {
                // For older Android versions
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.let { address ->
                    buildAddressString(address)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Build readable address string
     */
    private fun buildAddressString(address: android.location.Address): String {
        val parts = mutableListOf<String>()
        
        // Thoroughfare (street name)
        address.thoroughfare?.let { parts.add(it) }
        
        // SubLocality (district/neighborhood)
        address.subLocality?.let { parts.add(it) }
        
        // Locality (city/town)
        address.locality?.let { parts.add(it) }
        
        // AdminArea (province/state)
        address.adminArea?.let { parts.add(it) }
        
        // If we have parts, join them
        if (parts.isNotEmpty()) {
            return parts.joinToString(", ")
        }
        
        // Fallback: use full address line
        return address.getAddressLine(0) ?: "Lokasi tidak diketahui"
    }
}
