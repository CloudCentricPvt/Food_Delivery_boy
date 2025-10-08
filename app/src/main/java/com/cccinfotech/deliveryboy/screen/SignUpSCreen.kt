package com.cccinfotech.deliveryboy.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignupScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }



    val ctx = LocalContext.current
    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            )
            {

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = ""
                    },
                    label = { Text("Enter Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    )
                )

                if (emailError.isNotEmpty()) {
                    Text(
                        text = emailError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                OutlinedTextField(
                    name,
                    onValueChange = {
                        name = it
                        nameError = ""
                    },
                    label = { Text("Enter Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()

                )

                if (nameError.isNotEmpty()) {
                    Text(
                        text = nameError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                OutlinedTextField(
                    phone,
                    onValueChange = {
                        phone = it
                        phoneError = ""
                    },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),

                    modifier = Modifier.fillMaxWidth()
                )
                if (phoneError.isNotEmpty()) {
                    Text(
                        text = phoneError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),

                    )
                }

                OutlinedTextField(
                    password,
                    onValueChange = {
                        password = it
                        passwordError = ""
                    },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (passwordError.isNotEmpty()) {
                    Text(
                        text = passwordError,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {

                    var valid = true

                    if (email.isBlank()) {
                        emailError = "Please enter your email"
                        valid = false
                    }
                    if (name.isBlank()) {
                        nameError = "Please enter your name"
                        valid = false
                    }
                    if (phone.isBlank()) {
                        phoneError = "Please enter your phone number"
                        valid = false
                    } else if (phone.length < 10) {
                        phoneError = "Invalid phone number"
                        valid = false
                    }
                    if (password.isBlank()) {
                        passwordError = "Please enter your password"
                        valid = false
                    } else if (password.length < 6) {
                        passwordError = "Password must be at least 6 characters"
                        valid = false
                    }

                    if (!valid) return@Button

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        email.trim(),
                        password.trim()
                    ).addOnSuccessListener { res ->
                        val uid = res.user?.uid ?: return@addOnSuccessListener

                        val user = mapOf(
                            "uid" to uid,
                            "email" to email.trim(),
                            "role" to "Delivery boy",   // "admin", "customer", "delivery"
                            "name" to name.trim(),
                            "phone" to phone.trim(),
                            "password" to password.trim(),
                            "createdAt" to System.currentTimeMillis()
                        )

                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(ctx, "Account created", Toast.LENGTH_SHORT).show()
                                navController.navigate("login") {
                                    popUpTo("SignUp") { inclusive = true }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(ctx, it.message, Toast.LENGTH_SHORT).show()
                            }

                    }.addOnFailureListener {
                        Toast.makeText(ctx, it.message, Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Sign Up") }
            }
        }
    )
}