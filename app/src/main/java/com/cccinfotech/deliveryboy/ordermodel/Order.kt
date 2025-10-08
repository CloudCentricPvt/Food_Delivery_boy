package com.cccinfotech.deliveryboy.ordermodel

/*data class Order(
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
)*/
data class Order(
    val orderNumber: String? = null,
    val orderId: String? = null,
    val customerName: String? = null,
    val orderDate: String? = null,
    val currentTime: String? = null,
    val status: String? = null,
    val orderAddress: String? = null,
    val orderLatitude: Double? = null,
    val orderLongitude: Double? = null,
    val dBoy_Id: String? = null,
    val deliveryBoye: String? = null,
    val productImage: String? = null,
    val deliveryTimeOTP: String? = null,

    // Fix: Use List<Item> instead of Map<String, Item>
    val items: List<Item>? = null
)

data class Item(
    val productName: String = "",
    val quantity: Int = 0,   // keep Int (Firestore saves numbers as number type, not string)
    val amount: String = "",
    val currentTime: String = "",
    val orderDate: String = "",
    val productImage: String = "",
    val productDetails: String = "",

)




