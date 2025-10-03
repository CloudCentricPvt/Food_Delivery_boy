package com.cccinfotech.deliveryboy.screen


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Route

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.cccinfotech.deliveryboy.ordermodel.Order
import com.cccinfotech.deliveryboy.service.DeliveryTrackingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    val currentUser = FirebaseAuth.getInstance().currentUser
    val dBoyId = currentUser?.uid

    // Tab state
    val tabs = listOf("All", "Pending", "InProgress", "Delivered")
    var selectedTab by remember { mutableStateOf(0) }

    //  Fetch Orders
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                isLoading = false
                if (error != null) return@addSnapshotListener

                val formatter = DateTimeFormatter.ofPattern(
                    "dd-MM-yyyy HH:mm:ss",
                    Locale.getDefault()
                )

                orders = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                }?.sortedByDescending { order ->
                    val combined = "${order.orderDate} ${order.currentTime}"
                    runCatching { LocalDateTime.parse(combined, formatter) }
                        .getOrNull() ?: LocalDateTime.MIN
                } ?: emptyList()
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
                                Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
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
                        2 -> orders.filter { it.status == "inprogress" || it.status == "out for delivery" }
                        3 -> orders.filter { it.status == "delivered" }
                        else -> orders
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
                    }else{
                        LazyColumn(modifier = Modifier.fillMaxSize(),) {
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
                                                        snackbarHostState.showSnackbar("Not allowed, this order is already accepted by another Delivery boy")
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
                                            Text("#OrderId: ${order.orderId}", style = TextStyle(fontWeight = FontWeight.SemiBold, color = Color.Blue))
                                            Text("Customer: ${order.customerName}", style = TextStyle(fontWeight = FontWeight.Bold, color = Color.Black))
                                            Row { Text("Date: ${order.orderDate}|${order.currentTime}") }
                                            Text("Product: ${order.productName}")
                                            Text("Quantity: ${order.quantity}")
                                            Text("Amount: ₹${order.amount}")
                                            Text("Status: ${order.status}", style = TextStyle(fontWeight = FontWeight.W400, color = Color.Black))
                                            Text("Delivered address: ${order.orderAddress}", style = TextStyle(fontWeight = FontWeight.ExtraLight, color = Color.Black))
                                            Row(
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                //DirectionIcon(lat =order.orderLatitude!!, lng = order.orderLongitude!!)
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

    // Order Details Dialog
    selectedOrder?.let { order ->
        OrderDetailsDialog(
            order = order,
            currentUser = currentUser,
            dBoyId = dBoyId,
            onDismiss = { selectedOrder = null },
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
                        showLogoutDialog = false
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
    onDismiss: () -> Unit,
    onStatusChange: (Order) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Order Details") },
        text = {
            Column {
                Text("#OrderId: ${order.orderId}", style = TextStyle(fontWeight = FontWeight.SemiBold, color = Color.Black))
                Row { Text("Date: ${order.orderDate}|${order.currentTime}") }
                Text("Customer: ${order.customerName}")
                Text("Product: ${order.productName}")
                Text("Quantity: ${order.quantity}")
                Text("Amount: ₹${order.amount}")
                Text("Status: ${order.status}")
            }
        },
        confirmButton = {
            when (order.status) {
                "Pending" -> Button(onClick = {
                    //  Permission check
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.FOREGROUND_SERVICE
                        ) != PackageManager.PERMISSION_GRANTED
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
                    //  Start Foreground Service for tracking
                    val intent = Intent(context, DeliveryTrackingService::class.java)
                    intent.putExtra("orderId", order.orderId)
                    ContextCompat.startForegroundService(context, intent)

                    //  Update Firestore status
                    updateOrderStatusByOrderId(
                        order.orderId ?: "",
                        "inprogress",
                        "Sabir",
                        currentUser!!.uid
                    ) {
                        onStatusChange(order.copy(status = "inprogress"))
                        onDismiss()
                    }
                }) { Text("Accept") }

                "inprogress" -> Button(onClick = {
                    updateOrderStatusByOrderId(order.orderId ?: "", "out for delivery", "Sabir", "$dBoyId") {
                        onStatusChange(order.copy(status = "out for delivery"))
                        onDismiss()
                    }
                }) { Text("Out for Delivery") }

                "out for delivery" -> Button(onClick = {
                    updateOrderStatusByOrderId(order.orderId ?: "", "delivered", "Sabir", "$dBoyId") {
                        onStatusChange(order.copy(status = "delivered"))
                        onDismiss()
                        //  Stop Foreground Service when delivered
                        context.stopService(Intent(context, DeliveryTrackingService::class.java))
                    }
                }) { Text("Delivered") }
            }
        },
        dismissButton = {
            if (order.status == "Pending") OutlinedButton(onClick = { onDismiss() }) { Text("Cancel") }
        }
    )
}
//  Update Firestore
fun updateOrderStatusByOrderId(
    orderId: String,
    newStatus: String,
    dName: String,
    dBoyID: String,
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
                "dBoy_Id" to dBoyID
            )
        )
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { it.printStackTrace() }
}








