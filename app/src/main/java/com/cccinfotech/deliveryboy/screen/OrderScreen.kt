package com.cccinfotech.deliveryboy.screen


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import kotlin.random.Random
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.cccinfotech.deliveryboy.ordermodel.Order
import com.cccinfotech.deliveryboy.ordermodel.User
import com.cccinfotech.deliveryboy.service.DeliveryTrackingService
import com.cccinfotech.deliveryboy.utils.SharedPrefManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import coil.compose.rememberImagePainter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.google.firebase.auth.FirebaseUser


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(26)
@Composable
fun DeliveryHome(navController: NavHostController) {

    val context = LocalContext.current
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    var expanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var userPhone by remember { mutableStateOf("") }
    val otpVerify by remember { mutableStateOf("") }

    // ✅ OTP Dialog ke liye parent level state
    var showOtpDialog by remember { mutableStateOf(false) }
    var orderForOtp by remember { mutableStateOf<Order?>(null) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val dBoyId = currentUser?.uid

    // Tab state
    val tabs = listOf("All", "Pending", "InProgress", "Delivered")
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {

        val db = FirebaseFirestore.getInstance()
        db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) {
                    Log.e("Firestore", "Error: ${error.message}")
                    return@addSnapshotListener
                }

                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

                orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                }?.sortedByDescending { order ->
                    val combined = "${order.orderDate} ${order.currentTime}"
                    runCatching { LocalDateTime.parse(combined, formatter) }
                        .getOrNull() ?: LocalDateTime.MIN
                } ?: emptyList()
            }

        getUserData(db, SharedPrefManager.getString("deliveryBoy_name")) { user ->
            userName = user.name.toString()
            userEmail = user.email.toString()
            userPhone = user.phone.toString()
            SharedPrefManager.putString("Role", user.role.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders") },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Profile") },
                            onClick = {
                                expanded = false
                                navController.navigate("profile_screen")
                            })
                        DropdownMenuItem(
                            text = { Text("Completed order") },
                            onClick = {
                                expanded = false
                                Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                            })
                        DropdownMenuItem(
                            text = { Text("Help & Support") },
                            onClick = {
                                expanded = false
                                Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                            })
                        DropdownMenuItem(
                            text = { Text("Log out") },
                            onClick = {
                                expanded = false
                                showLogoutDialog = true
                            })
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            //  Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                else -> {

                    val filteredOrders = when (selectedTab) {
                        1 -> orders.filter { it.status == "Pending" }
                        2 -> orders.filter {
                            (it.status == "inprogress" || it.status == "out for delivery") && it.dBoy_Id == dBoyId
                        }
                        3 -> orders.filter {
                            it.status == "delivered" && it.dBoy_Id == dBoyId
                        }
                        else -> orders.filter {
                            it.status == "Pending" || it.dBoy_Id == dBoyId
                        }
                    }

                    if (filteredOrders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No record found",
                                style = TextStyle(
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    } else {

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filteredOrders) { order ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clickable {
                                            val inProgressOrder = orders.find {
                                                it.status == "inprogress" || it.status == "out for delivery"
                                            }

                                            if (currentUser?.uid == order.dBoy_Id) {
                                                if (inProgressOrder != null && order.status == "Pending") {
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        snackbarHostState.showSnackbar("You already have an order in progress!")
                                                    }
                                                    return@clickable
                                                }
                                            } else {
                                                if (order.status == "inprogress" || order.status == "out for delivery") {
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        snackbarHostState.showSnackbar("Not allowed, this order is already accepted by ${order.deliveryBoye}")
                                                    }
                                                    return@clickable
                                                }
                                            }
                                            selectedOrder = order
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (order.status.equals("delivered", true)) {
                                            Color.White
                                        } else {
                                            Color(0xFFF5F5F5)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Order No: ${order.orderNumber ?: ""}", style = TextStyle(fontWeight = FontWeight.ExtraBold, color = Color.Blue))
                                            Text("Customer: ${order.customerName ?: ""}", style = TextStyle(fontWeight = FontWeight.Bold, color = Color.Black))
                                            Row { Text("Date: ${order.orderDate ?: ""}|${order.currentTime ?: ""}") }

                                            order.items?.forEach { item ->
                                                Divider(color = Color.Gray, thickness = 0.5.dp)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Image(
                                                        painter = rememberImagePainter(item.productImage),
                                                        contentDescription = item.productName,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(80.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                    )
                                                    Column(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .padding(start = 12.dp)
                                                    ) {
                                                        Text("Product: ${item.productName}")
                                                        Text("Quantity: ${item.quantity}")
                                                        Text("Amount: ₹${item.amount}")
                                                        Text("Description: ${item.productDetails}", maxLines = 2)
                                                    }
                                                }
                                            }
                                            Divider(color = Color.Gray, thickness = 0.5.dp)
                                            Text("Status: ${order.status}")
                                            Text("Delivery boy: ${order.deliveryBoye ?: ""}", color = Color.Red)
                                            Text("Delivered address: ${order.orderAddress}")
                                            Row(horizontalArrangement = Arrangement.End) {
                                                DirectionIcon(
                                                    lat = order.orderLatitude ?: 0.0,
                                                    lng = order.orderLongitude ?: 0.0
                                                )
                                            }
                                        }
                                        if (order.status.equals("delivered", true)) {
                                            Text("Delivered", style = TextStyle(fontWeight = FontWeight.Bold, color = Color.Red))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ✅ Order Details Dialog
    selectedOrder?.let { order ->
        OrderDetailsDialog(
            order = order,
            currentUser = currentUser,
            dBoyId = dBoyId,
            name = userName,
            otp = otpVerify,
            onDismiss = { selectedOrder = null },
            onStatusChange = { updatedOrder ->
                orders = orders.map { if (it.orderId == updatedOrder.orderId) updatedOrder else it }
            },
            onDeliveredClicked = { orderToDeliver ->
                orderForOtp = orderToDeliver
                showOtpDialog = true
                selectedOrder = null
            }
        )
    }

    // ✅ OTP Dialog parent level pe
    if (showOtpDialog && orderForOtp != null) {
        OtpDialogForOrder(
            order = orderForOtp!!,
            currentUser = currentUser,
            dBoyId = dBoyId,
            name = userName,
            onDismiss = {
                showOtpDialog = false
                orderForOtp = null
            },
            onStatusChange = { updatedOrder ->
                orders = orders.map { if (it.orderId == updatedOrder.orderId) updatedOrder else it }
            }
        )
    }

    //  Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out alert!") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        SharedPrefManager.remove("Logged_In")
                        navController.navigate("login") {
                            popUpTo("delivery_boy_home_screen") { inclusive = true }
                        }
                    }
                ) { Text("Log out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun OrderDetailsDialog(
    order: Order,
    currentUser: FirebaseUser?,
    dBoyId: String?,
    name: String?,
    otp: String?,
    onDismiss: () -> Unit,
    onStatusChange: (Order) -> Unit,
    onDeliveredClicked: (Order) -> Unit
) {
    val context = LocalContext.current
    var otp11 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order Details")
                Icon(imageVector = Icons.Default.Cancel, contentDescription = "Cancel", modifier = Modifier.clickable { onDismiss() })
            }
        },
        text = {
            Column {
                Text("Order No: ${order.orderNumber ?: ""}", style = TextStyle(fontWeight = FontWeight.ExtraBold, color = Color.Blue))
                Text("#OrderId: ${order.orderId ?: ""}")
                Row { Text("Date: ${order.orderDate}|${order.currentTime}") }
                Text("Customer: ${order.customerName}")

                order.items?.forEach { item ->
                    Divider(color = Color.Gray, thickness = 0.5.dp)
                    Text("Product: ${item.productName}")
                    Text("Quantity: ${item.quantity}")
                    Text("Amount: ₹${item.amount}")
                    Text("Description: ${item.productDetails}")
                }

                Divider(color = Color.Gray, thickness = 0.5.dp)
                Text("Delivery boy: ${order.deliveryBoye ?: ""}", color = Color.Red)
                Text("Status: ${order.status}")
                Text("Delivered address: ${order.orderAddress}")
            }
        },
        confirmButton = {
            when (order.status) {
                "Pending" -> Button(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            context as Activity,
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.FOREGROUND_SERVICE
                            ),
                            101
                        )
                        return@Button
                    }

                    val intent = Intent(context, DeliveryTrackingService::class.java)
                    intent.putExtra("orderId", order.orderId)
                    ContextCompat.startForegroundService(context, intent)
                    otp11 = generateOrderNumber()

                    val updatedOrder = order.copy(
                        status = "inprogress",
                        dBoy_Id = currentUser!!.uid,
                        deliveryBoye = "$name"
                    )
                    onStatusChange(updatedOrder)
                    onDismiss()

                    updateOrderStatusByOrderId(
                        order.orderId ?: "",
                        "inprogress",
                        "$name",
                        currentUser.uid,
                        deliveryTimeOTP = otp11
                    ) {}
                }) { Text("Accept") }

                "inprogress" -> Button(onClick = {
                    FirebaseFirestore.getInstance()
                        .collection("orders")
                        .document(order.orderId ?: "")
                        .get()
                        .addOnSuccessListener { document ->
                            val savedOtp = document.getString("deliveryTimeOTP") ?: ""
                            updateOrderStatusByOrderId(
                                order.orderId ?: "",
                                "out for delivery",
                                "$name",
                                "$dBoyId",
                                deliveryTimeOTP = savedOtp
                            ) {
                                onStatusChange(order.copy(status = "out for delivery"))
                                onDismiss()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to fetch OTP: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }) { Text("Out for Delivery") }

                "out for delivery" -> Button(onClick = {
                    onDeliveredClicked(order)  // ✅ yahan se OTP dialog parent me open hoga
                }) { Text("Delivered") }
            }
        },
        dismissButton = {
            if (order.status == "Pending") OutlinedButton(onClick = {
                updateOrderStatusByOrderId(order.orderId ?: "", "Cancelled", "$name", "$dBoyId", "") {
                    onStatusChange(order.copy(status = "Cancelled"))
                    onDismiss()
                }
            }) { Text("Cancel order", color = Color.Red) }
        }
    )
}

fun updateOrderStatusByOrderId(
    orderId: String,
    newStatus: String,
    dName: String,
    dBoyID: String,
    deliveryTimeOTP: String,
    onSuccess: () -> Unit
) {
    FirebaseFirestore.getInstance()
        .collection("orders")
        .document(orderId)
        .update(
            mapOf(
                "status" to newStatus,
                "deliveryBoye" to dName,
                "orderId" to orderId,
                "dBoy_Id" to dBoyID,
                "deliveryTimeOTP" to deliveryTimeOTP
            )
        )
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { it.printStackTrace() }
}

fun getUserData(db: FirebaseFirestore, userId: String, onResult: (User) -> Unit) {
    db.collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val name = document.getString("name")
                val email = document.getString("email")
                val phone = document.getString("phone")
                onResult(User(name, email, phone))

                Log.d("FirestoreUser", "Name: $name, Email: $email, Phone: $phone")
            } else {
                Log.d("FirestoreUser", "No such document")
            }
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreUser", "Error fetching user", e)
        }

}
fun generateOrderNumber(): String {
    val number = Random.nextInt(1000, 10000) // from 100000 to 999999
    return number.toString()
}

@Composable
fun OtpDialogForOrder(
    order: Order,
    currentUser: FirebaseUser?,
    dBoyId: String?,
    name: String?,
    onDismiss: () -> Unit,
    onStatusChange: (Order) -> Unit
) {
    val context = LocalContext.current
    var enteredOtp by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Enter OTP", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Please enter the 4-digit OTP to confirm delivery.")
                Spacer(modifier = Modifier.height(16.dp))

                // 🔸 OTP Box UI
                OtpBoxRow(
                    otpText = enteredOtp,
                    onOtpChange = { value ->
                        if (value.length <= 4 && value.all { it.isDigit() }) {
                            enteredOtp = value
                        }
                    }
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val orderId = order.orderId ?: return@Button
                    FirebaseFirestore.getInstance()
                        .collection("orders")
                        .document(orderId)
                        .get()
                        .addOnSuccessListener { doc ->
                            val savedOtp = doc.getString("deliveryTimeOTP")
                            if (enteredOtp == savedOtp) {
                                updateOrderStatusByOrderId(
                                    orderId,
                                    "delivered",
                                    "$name",
                                    "$dBoyId",
                                    deliveryTimeOTP = savedOtp ?: ""
                                ) {
                                    Toast.makeText(context, "Order delivered successfully!", Toast.LENGTH_SHORT).show()
                                    onStatusChange(order.copy(status = "delivered"))
                                    onDismiss()
                                    context.stopService(Intent(context, DeliveryTrackingService::class.java))
                                }
                            } else {
                                errorMessage = "Invalid OTP. Please try again."
                            }
                        }
                        .addOnFailureListener {
                            errorMessage = "Error fetching OTP from server."
                        }
                },
                enabled = enteredOtp.length == 4
            ) {
                Text("Verify OTP")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OtpBoxRow(
    otpText: String,
    onOtpChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .clickable { focusRequester.requestFocus() }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val char = otpText.getOrNull(index)?.toString() ?: ""
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Text(
                        text = char,
                        style = TextStyle(fontSize = 20.sp, textAlign = TextAlign.Center)
                    )
                }
            }
        }

        BasicTextField(
            value = otpText,
            onValueChange = { onOtpChange(it) },
            modifier = Modifier
                .matchParentSize()
                .focusRequester(focusRequester)
                .alpha(0f), // invisible field for input
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}












