package com.cccinfotech.deliveryboy

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import com.cccinfotech.deliveryboy.nav_host.DeliveryAppMain
import com.cccinfotech.deliveryboy.utils.SharedPrefManager
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // 👈 Important line


        if (FirebaseApp.getApps(this).isEmpty()) FirebaseApp.initializeApp(this)
        SharedPrefManager.init(this)
        setContent { DeliveryAppMain() }
    }
}
