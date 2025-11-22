package com.example.hi.utils

/**
 * App-wide Constants
 */
object Constants {
    
    // Razorpay Configuration
    // For TEST MODE: Use your test key from Razorpay Dashboard (starts with rzp_test_)
    // Get it from: Razorpay Dashboard > Settings > API Keys > Test Key
    // For LIVE MODE: Use your live key (starts with rzp_live_)
    const val RAZORPAY_KEY_ID = "rzp_test_FUijwPsI1t6dUR" // Replace with your test key
    
    // Test Mode Flag
    const val IS_TEST_MODE = true // Set to false for production
    
    // Firestore Collections
    const val COLLECTION_ORDERS = "orders"
    const val COLLECTION_USERS = "users"
    const val COLLECTION_FEEDBACK = "feedback"
    const val COLLECTION_NOTIFICATIONS = "notifications"
    
    // Order Status
    const val STATUS_ORDER_PLACED = "Order Placed"
    const val STATUS_PAYMENT_CONFIRMED = "Payment Confirmed"
    const val STATUS_PREPARING = "Preparing"
    const val STATUS_OUT_FOR_DELIVERY = "Out for Delivery"
    const val STATUS_DELIVERED = "Delivered"
    
    // Payment Status
    const val PAYMENT_STATUS_SUCCESS = "Success"
    const val PAYMENT_STATUS_FAILED = "Failed"
    const val PAYMENT_STATUS_PENDING = "Pending"
    
    // Notification Types
    const val NOTIFICATION_PAYMENT_SUCCESS = "payment_success"
    const val NOTIFICATION_PAYMENT_FAILED = "payment_failed"
    const val NOTIFICATION_ORDER_CONFIRMED = "order_confirmed"
    const val NOTIFICATION_ORDER_PACKED = "order_packed"
    const val NOTIFICATION_OUT_FOR_DELIVERY = "out_for_delivery"
    const val NOTIFICATION_DELIVERED = "delivered"
}

