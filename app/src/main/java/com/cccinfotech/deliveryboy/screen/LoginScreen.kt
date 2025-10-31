package com.cccinfotech.deliveryboy.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cccinfotech.deliveryboy.ordermodel.User
import com.cccinfotech.deliveryboy.reusable_widget.KUserInputTest
import com.cccinfotech.deliveryboy.utils.SharedPrefManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging


@Composable
fun AuthScreen(navController: NavController?) {

    val context = LocalContext.current
    var fcmToken by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf("") }

        // Token fetch hoke fcmToken me save hoga
        GetDeviceTokenComposable { token ->
            fcmToken = token
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(100.dp))
            Text("Log In", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            Text("Please sign in to your account", color = Color.White, fontSize = 16.sp)

            Spacer(Modifier.height(60.dp))

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 4.dp
            ) {
                Column(Modifier.padding(20.dp)) {

                    Text("Email")
                    Spacer(modifier = Modifier.height(5.dp))
                    KUserInputTest().UserTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = ""
                        },

                        hint = "Enter Email"
                    )
                    // Show email error
                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text("Password")
                    Spacer(modifier = Modifier.height(5.dp))
                    KUserInputTest().UserTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = ""
                        },
                        hint = "Enter Password"
                    )
                    // Show password error
                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Don't have an account ? ")
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Sign Up",
                            color = Color.Red,
                            modifier = Modifier.clickable {
                                navController?.navigate("sign_up_screen")
                            }
                        )
                    }

                    Button(
                        onClick = {

                            var valid = true

                            if (email.isBlank()) {
                                emailError = "Please enter your email"
                                valid = false
                            }

                            if (password.isBlank()) {
                                passwordError = "Please enter your password"
                                valid = false
                            }

                            if (!valid) return@Button


                            FirebaseAuth.getInstance()
                                .signInWithEmailAndPassword(email.trim(), password.trim())
                                .addOnSuccessListener { res ->
                                    val uid = res.user?.uid ?: return@addOnSuccessListener

                                    val db = FirebaseFirestore.getInstance()

                                    // 🔹 Step 1: Get user details from Firestore
                                    db.collection("users")
                                        .document(uid)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            if (document != null && document.exists()) {
                                                val role = document.getString("role")

                                                if (role == "Delivery boy") {
                                                    // Step 2: Update FCM token
                                                    db.collection("users")
                                                        .document(uid)
                                                        .update("fcm_token", fcmToken)
                                                        .addOnSuccessListener {
                                                            Log.d("##FCM", "Token saved: $fcmToken")
                                                        }
                                                        .addOnFailureListener {
                                                            Log.e("##FCM", "Failed to save token: ${it.message}")
                                                        }

                                                    // Step 3: Navigate to delivery home screen
                                                    navController?.navigate("delivery_boy_home_screen") {
                                                        SharedPrefManager.putBoolean("Logged_In", true)
                                                        SharedPrefManager.putString("deliveryBoy_name", uid)
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                } else {
                                                    // User is not a delivery boy
                                                    Toast.makeText(context, "This user is not a Delivery Boy", Toast.LENGTH_LONG).show()
                                                    FirebaseAuth.getInstance().signOut()
                                                }
                                            } else {
                                                Toast.makeText(context, "User record not found", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error fetching user details: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                                    Log.d("#Result", it.message.toString())
                                }

                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Login") }
                }
            }
        }
    }
}

@Composable
fun GetDeviceTokenComposable(onTokenReceived: (String) -> Unit) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("##FCM", "Token: $token")
                onTokenReceived(token)
            }
    }
}






