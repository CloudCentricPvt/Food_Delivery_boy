package com.cccinfotech.deliveryboy.ordermodel

data class Order(
    val orderId:String?="",
    val customerId:String? = "",
    val dBoy_Id:String? = "",
    val customerName:String? = "",
    val productId:String? = "",
    val productName:String? = "",
    val quantity:String? ="",
    val status:String? ="",
    val amount:String? = "",
    val orderDate:String? = "",
    val currentTime:String? = "",
    val deliveryBoye:String? = "",
    val orderAddress:String? = "",
    var orderLatitude: Double? = null,   // <-- add this
    var orderLongitude: Double? = null   // <-- add this
)
