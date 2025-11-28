package com.example.hi.repository

import android.util.Log
import com.example.hi.model.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    /**
     * 🚀 Save an order
     */
    suspend fun saveOrder(order: Order): Result<String> {
        return try {
            if (order.orderId.isEmpty()) {
                return Result.failure(Exception("Order ID cannot be empty"))
            }

            Log.d("OrderRepository", "Saving order: ${order.orderId}")

            val orderMap = hashMapOf<String, Any>(
                "orderId" to order.orderId,
                "userId" to order.userId,
                "customerName" to order.customerName,
                "phone" to order.phone,
                "email" to order.email,
                "tableNumber" to order.tableNumber,
                "totalAmount" to order.totalAmount,
                "cookingInstructions" to (order.cookingInstructions ?: ""),
                "status" to order.status,
                "timestamp" to order.timestamp
            )

            val itemsList = order.items.map { item ->
                hashMapOf(
                    "itemName" to item.itemName,
                    "itemPrice" to item.itemPrice,
                    "quantity" to item.quantity
                )
            }
            orderMap["items"] = itemsList

            ordersCollection.document(order.orderId).set(orderMap).await()

            Log.d("OrderRepository", "Order saved successfully: ${order.orderId}")
            Result.success(order.orderId)

        } catch (e: Exception) {
            Log.e("OrderRepository", "❌ Error saving order: ${e.message}", e)
            Result.failure(Exception("Failed to save order: ${e.localizedMessage}"))
        }
    }

    /**
     * 🔄 Update order status
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
     * 🔎 Get one order by ID
     */
    suspend fun getOrder(orderId: String): Result<Order> {
        return try {
            val snapshot = ordersCollection.document(orderId).get().await()
            val order = snapshot.toObject(Order::class.java)
            if (order != null) Result.success(order)
            else Result.failure(Exception("Order not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 📦 Get all orders of user sorted by latest
     */
    suspend fun getUserOrders(userId: String): Result<List<Order>> {
        return fetchOrders(userId)
    }

    /**
     * 📦 (OLD METHOD) - for compatibility only
     */
    suspend fun getOrdersByUserId(userId: String): Result<List<Order>> {
        return fetchOrders(userId)
    }

    /**
     * 🔁 Internal function - avoids code repetition
     */
    private suspend fun fetchOrders(userId: String): Result<List<Order>> {
        return try {
            Log.d("OrderRepository", "Fetching orders for user: $userId")

            val snapshot = ordersCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { document ->
                val order = document.toObject(Order::class.java)
                if (order != null && order.items == null) order.items = emptyList()
                order
            }

            Log.d("OrderRepository", "➡ Found ${orders.size} orders")
            Result.success(orders)

        } catch (e: Exception) {
            Log.e("OrderRepository", "❌ Failed to fetch orders: ${e.message}", e)
            Result.failure(Exception("Error loading orders"))
        }
    }

    /**
     * 🟡 Real-time tracking listener
     */
    fun listenToOrderStatus(orderId: String, callback: (Order?) -> Unit): ListenerRegistration {
        return ordersCollection.document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("OrderRepository", "Listener error: $error")
                    callback(null)
                    return@addSnapshotListener
                }
                val order = snapshot?.toObject(Order::class.java)
                callback(order)
            }
    }
}
