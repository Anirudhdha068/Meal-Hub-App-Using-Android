package com.example.hi.model

import com.google.firebase.firestore.PropertyName

/**
 * Order Model - Smart Canteen (COD only)
 */
data class Order(
    @PropertyName("orderId")
    var orderId: String = "",

    @PropertyName("userId")
    var userId: String = "",

    @PropertyName("customerName")
    var customerName: String = "",

    @PropertyName("phone")
    var phone: String = "",

    @PropertyName("email")
    var email: String = "",

    @PropertyName("tableNumber")
    var tableNumber: String = "", // Replaces delivery address

    @PropertyName("items")
    var items: List<OrderItem> = emptyList(),

    @PropertyName("totalAmount")
    var totalAmount: Int = 0,

    @PropertyName("cookingInstructions")
    var cookingInstructions: String = "",

    @PropertyName("status")
    var status: String = "Order Placed", // Order Placed → Preparing → Ready → Served

    @PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()
)

/**
 * Order Item Model
 */
data class OrderItem(
    @PropertyName("itemName")
    var itemName: String = "",

    @PropertyName("itemPrice")
    var itemPrice: Int = 0,

    @PropertyName("quantity")
    var quantity: Int = 0
)
