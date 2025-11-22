package com.example.hi.repository

import android.util.Log
import com.example.hi.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

/**
 * Repository for Order operations with Firestore
 */
class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    /**
     * Save order to Firestore
     */
    suspend fun saveOrder(order: Order): Result<String> {
        return try {

            if (order.orderId.isEmpty()) {
                return Result.failure(Exception("Order ID cannot be empty"))
            }

            Log.d("OrderRepository", "Starting order save: ${order.orderId}")

            val orderMap = hashMapOf<String, Any>(
                "orderId" to order.orderId,
                "userId" to order.userId,
                "totalAmount" to order.totalAmount,
                "cookingInstructions" to (order.cookingInstructions ?: ""),
                "paymentStatus" to order.paymentStatus,
                "transactionId" to (order.transactionId ?: ""),
                "paymentAmount" to (order.paymentAmount ?: 0),
                "status" to order.status,
                "timestamp" to order.timestamp,
                "errorReason" to (order.errorReason ?: ""),
                "paymentMethod" to (order.paymentMethod ?: "")
            )

            // Convert OrderItem list to List<Map>
            val itemsList = order.items.map { item ->
                hashMapOf<String, Any>(
                    "itemName" to item.itemName,
                    "itemPrice" to item.itemPrice,
                    "quantity" to item.quantity
                )
            }
            orderMap["items"] = itemsList

            val docRef = ordersCollection.document(order.orderId)
            docRef.set(orderMap).await()

            Log.d("OrderRepository", "Order saved successfully: ${order.orderId}")
            return Result.success(order.orderId)

        } catch (e: Exception) {
            Log.e("OrderRepository", "Error saving order: ${e.message}", e)
            return Result.failure(Exception("Failed to save order: ${e.localizedMessage}"))
        }
    }

    /**
     * Update order status
     */
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update payment status
     */
    suspend fun updatePaymentStatus(
        orderId: String,
        paymentStatus: String,
        transactionId: String = "",
        errorReason: String = "",
        paymentMethod: String = ""
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "paymentStatus" to paymentStatus,
                "transactionId" to transactionId
            )
            if (errorReason.isNotEmpty()) {
                updates["errorReason"] = errorReason
            }
            if (paymentMethod.isNotEmpty()) {
                updates["paymentMethod"] = paymentMethod
            }
            ordersCollection.document(orderId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get order by ID
     */
    suspend fun getOrder(orderId: String): Result<Order> {
        return try {
            val snapshot = ordersCollection.document(orderId).get().await()
            val order = snapshot.toObject(Order::class.java)
            if (order != null) {
                Result.success(order)
            } else {
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get orders by user ID
     */
    suspend fun getUserOrders(userId: String): Result<List<Order>> {
        return try {
            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
            Result.success(orders)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Listen to order status updates
     */
    fun listenToOrderStatus(orderId: String, callback: (Order?) -> Unit): ListenerRegistration {
        return ordersCollection.document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    callback(null)
                    return@addSnapshotListener
                }
                val order = snapshot?.toObject(Order::class.java)
                callback(order)
            }
    }
}
