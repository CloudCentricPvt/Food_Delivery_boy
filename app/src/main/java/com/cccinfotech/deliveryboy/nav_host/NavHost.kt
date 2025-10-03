package com.cccinfotech.deliveryboy.nav_host

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cccinfotech.deliveryboy.screen.AuthScreen
import com.cccinfotech.deliveryboy.screen.DeliveryHome
import com.cccinfotech.deliveryboy.screen.SignupScreen
import com.cccinfotech.deliveryboy.utils.SharedPrefManager

@Composable
fun DeliveryAppMain() {
    val navController = rememberNavController()

    val isLoggedIn = SharedPrefManager.getBoolean("Logged_In")
    val deliveryName = SharedPrefManager.getString("deliveryBoy_name")
    Log.d("#checkLOgged",isLoggedIn.toString())
    Log.d("#checkLOgged",deliveryName)

    // Decide the first screen,
    val startDestination = if (isLoggedIn) {
        "delivery_boy_home_screen"
    } else {
        "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") { AuthScreen(navController) }
        composable("delivery_boy_home_screen") { DeliveryHome(navController) }
        composable("sign_up_screen") { SignupScreen(navController) }
    }
}

