package com.cccinfotech.deliveryboy.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignupScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val ctx = LocalContext.current
    Scaffold(
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp), verticalArrangement = Arrangement.Top
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Enter Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    )
                )
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text("Enter Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(role, { role = it }, label = { Text("Role") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    password,
                    { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        email.trim(),
                        password.trim()
                    ).addOnSuccessListener { res ->
                        val uid = res.user?.uid ?: return@addOnSuccessListener

                        val user = mapOf(
                            "uid" to uid,
                            "email" to email.trim(),
                            "role" to role.trim(),   // "admin", "customer", "delivery"
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