package com.example.hi.model

import com.google.firebase.firestore.PropertyName

/**
 * Order Model - represents an order in Firestore
 */
data class Order(
    @PropertyName("orderId")
    var orderId: String = "",
    
    @PropertyName("userId")
    var userId: String = "",
    
    @PropertyName("items")
    var items: List<OrderItem> = emptyList(),
    
    @PropertyName("totalAmount")
    var totalAmount: Int = 0,
    
    @PropertyName("cookingInstructions")
    var cookingInstructions: String = "",
    
    @PropertyName("paymentStatus")
    var paymentStatus: String = "Pending", // Success, Failed, Pending
    
    @PropertyName("transactionId")
    var transactionId: String = "",
    
    @PropertyName("paymentAmount")
    var paymentAmount: Int = 0,
    
    @PropertyName("status")
    var status: String = "Order Placed", // Order Placed, Payment Confirmed, Preparing, Out for Delivery, Delivered
    
    @PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis(),
    
    @PropertyName("errorReason")
    var errorReason: String = "",
    
    @PropertyName("paymentMethod")
    var paymentMethod: String = "" // card, upi, wallet, etc.
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






