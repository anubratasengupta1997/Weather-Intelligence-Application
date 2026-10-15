package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.domain.model.CityLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCityLocation(context: Context): CityLocation? {
        if (!hasLocationPermission(context)) return null

        val location = withTimeoutOrNull(6000L) {
            getDeviceLocation(context)
        } ?: return null

        val lat = location.latitude
        val lon = location.longitude

        // Reverse geocode to resolve city name
        val resolvedName = resolveCityName(context, lat, lon)

        return CityLocation(
            id = (lat * 1000 + lon).toLong(),
            name = resolvedName.first ?: "Current Location",
            admin1 = resolvedName.second,
            country = resolvedName.third,
            countryCode = null,
            latitude = lat,
            longitude = lon
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun getDeviceLocation(context: Context): Location? =
        suspendCancellableCoroutine { continuation ->
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            continuation.invokeOnCancellation {
                cts.cancel()
            }

            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        if (continuation.isActive) continuation.resume(loc)
                    } else {
                        // Fallback to last known location
                        fusedClient.lastLocation
                            .addOnSuccessListener { lastLoc ->
                                if (continuation.isActive) continuation.resume(lastLoc)
                            }
                            .addOnFailureListener {
                                if (continuation.isActive) continuation.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    // Try last known location on failure
                    fusedClient.lastLocation
                        .addOnSuccessListener { lastLoc ->
                            if (continuation.isActive) continuation.resume(lastLoc)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                }
        }

    private fun resolveCityName(context: Context, latitude: Double, longitude: Double): Triple<String?, String?, String?> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val addr = addresses?.firstOrNull()
            val city = addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea
            val admin1 = addr?.adminArea
            val country = addr?.countryName
            Triple(city, admin1, country)
        } catch (e: Exception) {
            Triple(null, null, null)
        }
    }
}
