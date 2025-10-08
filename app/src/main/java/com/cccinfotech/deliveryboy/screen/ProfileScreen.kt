package com.cccinfotech.deliveryboy.screen


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cccinfotech.deliveryboy.ordermodel.User
import com.cccinfotech.deliveryboy.utils.SharedPrefManager
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    var userRole by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        //isLoading=true
        val db = FirebaseFirestore.getInstance()
        db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                getUserProfileData(db, SharedPrefManager.getString("deliveryBoy_name")) { user ->
                    userName = user.name.toString()
                    userEmail = user.email.toString()
                    userPhone = user.phone.toString()
                    userRole = user.role.toString()
                    isLoading = false
                }

            }

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIos,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),

                    shape = RoundedCornerShape(4.dp)


                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text(
                            text = userName,
                            style = TextStyle(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        )
                        Text(text = userEmail)
                        Text(text = userPhone)
                        Text(text = userRole)
                        isLoading = false
                    }

                }
            }

        }

    }


}

fun getUserProfileData(db: FirebaseFirestore, userId: String, onResult: (User) -> Unit) {
    db.collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val name = document.getString("name")
                val email = document.getString("email")
                val phone = document.getString("phone")
                val role = document.getString("role")
                onResult(User(name, email, phone, role))

                Log.d("FirestoreUser", "Name: $name, Email: $email, Phone: $phone")
            } else {
                Log.d("FirestoreUser", "No such document")
            }

        }
        .addOnFailureListener { e ->
            Log.e("FirestoreUser", "Error fetching user", e)
        }

}