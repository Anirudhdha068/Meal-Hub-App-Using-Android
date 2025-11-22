package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hi.model.Order
import com.example.hi.repository.OrderRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import androidx.lifecycle.lifecycleScope
import com.example.hi.utils.Constants
import kotlinx.coroutines.launch

/**
 * OrderTrackingActivity - Shows order status with live Firestore updates
 * Features:
 * - Shows list of user's orders
 * - Click on order to see detailed tracking
 * - Live updates when admin changes order status
 * - Progress layout showing: Order Placed, Payment Confirmed, Preparing, Out for Delivery, Delivered
 */
class TrackOrderActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoOrders: TextView
    
    private val auth = FirebaseAuth.getInstance()
    private val orderRepository = OrderRepository()
    private var orderListener: ListenerRegistration? = null
    
    private val ordersList = mutableListOf<Order>()
    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_order)
        
        window.statusBarColor = getColor(R.color.orange)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewOrders)
        progressBar = findViewById(R.id.progressBar)
        tvNoOrders = findViewById(R.id.tvNoOrders)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Setup RecyclerView
        ordersAdapter = OrdersAdapter(ordersList) { order ->
            // Open order details activity
            openOrderDetails(order)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ordersAdapter

        // Setup bottom navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_orders
        setupBottomNavigation(bottomNavigationView)

        btnBack?.setOnClickListener { finish() }

        // Load orders
        loadOrders()
    }

    /**
     * Load user orders from Firestore
     */
    private fun loadOrders() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            tvNoOrders.visibility = TextView.VISIBLE
            tvNoOrders.text = "Please login first"
            return
        }

        progressBar.visibility = ProgressBar.VISIBLE
        tvNoOrders.visibility = TextView.GONE

        // Use coroutines to load orders - only show orders with successful payment
        lifecycleScope.launch {
            try {
                val result = orderRepository.getUserOrders(currentUser.uid)
                if (result.isSuccess) {
                    ordersList.clear()
                    // Filter only orders with successful payment
                    val paidOrders = result.getOrNull()?.filter { 
                        it.paymentStatus == Constants.PAYMENT_STATUS_SUCCESS
                    } ?: emptyList()
                    ordersList.addAll(paidOrders)
                    ordersAdapter.notifyDataSetChanged()
                    
                    if (ordersList.isEmpty()) {
                        tvNoOrders.visibility = TextView.VISIBLE
                        tvNoOrders.text = "No orders yet. Complete a payment to see your orders here."
                    } else {
                        tvNoOrders.visibility = TextView.GONE
                    }
                } else {
                    tvNoOrders.visibility = TextView.VISIBLE
                    tvNoOrders.text = "No orders yet. Complete a payment to see your orders here."
                }
            } catch (e: Exception) {
                tvNoOrders.visibility = TextView.VISIBLE
                tvNoOrders.text = "No orders yet. Complete a payment to see your orders here."
            } finally {
                progressBar.visibility = ProgressBar.GONE
            }
        }
    }

    /**
     * Open order details with live tracking
     */
    private fun openOrderDetails(order: Order) {
        val intent = Intent(this, OrderTrackingDetailActivity::class.java)
        intent.putExtra("ORDER_ID", order.orderId)
        startActivity(intent)
    }

    /**
     * Setup bottom navigation
     */
    private fun setupBottomNavigation(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, activitybreakfast::class.java))
                    finish()
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, activityhome1::class.java))
                    finish()
                    true
                }
                R.id.nav_orders -> {
                    // Already on orders page
                    true
                }
                R.id.nav_feedback -> {
                    startActivity(Intent(this, FeedbackActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, OrderActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        orderListener?.remove()
    }
}

/**
 * Simple adapter for orders list
 */
class OrdersAdapter(
    private val orders: List<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): OrderViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
        holder.itemView.setOnClickListener { onOrderClick(order) }
    }

    override fun getItemCount() = orders.size

    class OrderViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(order: Order) {
            text1.text = "Order #${order.orderId.take(8)}"
            text2.text = "Status: ${order.status} | Total: ₹${order.totalAmount}"
        }
    }
}
