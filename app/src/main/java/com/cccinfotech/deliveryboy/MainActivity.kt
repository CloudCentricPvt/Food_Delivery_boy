package com.cccinfotech.deliveryboy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.cccinfotech.deliveryboy.nav_host.DeliveryAppMain
import com.cccinfotech.deliveryboy.utils.SharedPrefManager
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseApp.getApps(this).isEmpty()) FirebaseApp.initializeApp(this)
        SharedPrefManager.init(this)
        setContent { DeliveryAppMain() }
    }
}
