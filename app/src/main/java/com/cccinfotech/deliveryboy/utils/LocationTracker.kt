package com.cccinfotech.deliveryboy.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*

class LocationTracker(
    private val context: Context,
    private val orderId: String
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val dbRef = FirebaseDatabase.getInstance().getReference("delivery_boy")

    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val data = mapOf(
                "lat" to location.latitude,
                "lng" to location.longitude,
                "orderId" to orderId,
                "timestamp" to System.currentTimeMillis()
            )
            // Use orderId as child key so different orders have separate records
            dbRef.child("current_location").child(orderId)
                .setValue(data)
                .addOnSuccessListener { Log.d("#Firebase", "Location updated for $orderId") }
                .addOnFailureListener { e -> Log.e("#Firebase", "Update failed for $orderId", e) }

            Log.d("#LocationSent", data.toString())
        }
    }

    fun startUploading() {
        if (!hasLocationPermission()) {
            Log.d("#Tracker", "Location permission not granted")
            return
        }

        if (!isGPSEnabled()) {
            Log.d("#Tracker", "GPS is disabled")
            showGPSDialog()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).setMinUpdateIntervalMillis(5000).build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        if (job?.isActive != true) {
            job = scope.launch {
                while (isActive) delay(10_000) // keep coroutine alive
            }
        }
    }

    fun stopUploading() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        job?.cancel()
        job = null
        Log.d("#Tracker", "Location uploading stopped for order $orderId")
    }

    private fun hasLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showGPSDialog() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }
}
