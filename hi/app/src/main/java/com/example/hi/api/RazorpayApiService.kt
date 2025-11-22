package com.example.hi.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Razorpay API Service Interface
 * Note: This is a placeholder - you'll need to implement your backend API
 * that creates Razorpay orders. The backend should call Razorpay API to create orders.
 */
interface RazorpayApiService {
    
    /**
     * Create Razorpay Order
     * This should be called from your backend server, not directly from the app
     * 
     * Backend endpoint example: POST /api/create-order
     * Request body: { "amount": 6000, "currency": "INR" }
     * Response: { "orderId": "order_xxx", "keyId": "rzp_test_xxx" }
     */
    @POST("api/create-order")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<CreateOrderResponse>
}

/**
 * Request model for creating Razorpay order
 */
data class CreateOrderRequest(
    val amount: Int, // Amount in paise (e.g., 6000 for ₹60)
    val currency: String = "INR"
)

/**
 * Response model from backend
 */
data class CreateOrderResponse(
    val orderId: String,
    val keyId: String // Your Razorpay key ID
)







