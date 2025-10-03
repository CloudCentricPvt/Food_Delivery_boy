package com.cccinfotech.deliveryboy.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun DirectionIcon(lat: Double, lng: Double) {
    val context = LocalContext.current

    IconButton(onClick = {
        // Google Maps navigation URI
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        // Check if Maps app is installed
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // fallback to browser
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
            )
            context.startActivity(browserIntent)
        }
    }) {
        Icon(
            imageVector = Icons.Filled.Directions,
            contentDescription = "Directions"
        )
    }
}
