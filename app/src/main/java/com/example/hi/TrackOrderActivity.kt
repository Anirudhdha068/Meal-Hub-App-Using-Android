package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hi.model.Order
import com.example.hi.model.OrderItem
import com.example.hi.repository.OrderRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TrackOrderActivity: AppCompatActivity() {

    private lateinit var tvOrderDate: TextView
    private lateinit var tvMealReady: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var recyclerViewItems: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var scrollView: ScrollView

    private lateinit var orderId: String
    private val orderRepository = OrderRepository()
    private lateinit var itemsAdapter: OrderItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_order)

        window.statusBarColor = getColor(R.color.orange)

        // Get order ID from previous screen
        orderId = intent.getStringExtra("ORDER_ID") ?: ""

        // Bind views
        tvOrderDate = findViewById(R.id.tvOrderDate)
        tvMealReady = findViewById(R.id.tvMealReady)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod)
        recyclerViewItems = findViewById(R.id.recyclerViewItems)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        scrollView = findViewById(R.id.scrollView)

        // Setup RecyclerView
        itemsAdapter = OrderItemsAdapter(emptyList())
        recyclerViewItems.layoutManager = LinearLayoutManager(this)
        recyclerViewItems.adapter = itemsAdapter

        // Handle Back Button
        findViewById<ImageView>(R.id.menuIcon).setOnClickListener { finish() }

        // 🚀 Bottom Navigation (Important part you missed)


        // Load Order Data
        loadOrderData()
    }


    private fun loadOrderData() {
        if (orderId.isEmpty()) {
            showEmptyState()
            return
        }

        lifecycleScope.launch {
            try {
                val result = orderRepository.getOrder(orderId)
                val order = result.getOrNull()
                if (order != null) {
                    updateUI(order)
                } else {
                    showEmptyState()
                }
            } catch (e: Exception) {
                showEmptyState()
            }
        }
    }

    private fun updateUI(order: Order) {
        tvEmptyState.visibility = View.GONE
        scrollView.visibility = View.VISIBLE

        // 🚀 Clear previous items (if any)
        itemsAdapter.updateItems(emptyList())

        // Set order date
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        tvOrderDate.text = "Order date: ${sdf.format(Date(order.timestamp))}"

        // Set meal ready estimate
        val minutes = (10..30).random()
        tvMealReady.text = "Meal will be ready: in $minutes min"

        // Set total amount
        tvTotalAmount.text = "₹${order.totalAmount}"

        // Load latest order items
        itemsAdapter.updateItems(order.items)

        // Payment method (fixed)
        tvPaymentMethod.text = "Cash On Delivery"
    }
    private fun showEmptyState() {
        tvEmptyState.visibility = TextView.VISIBLE
        scrollView.visibility = ScrollView.GONE
    }
}

// Adapter for order items
class OrderItemsAdapter(private var items: List<OrderItem>) :
    RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder>() {

    fun updateItems(newItems: List<OrderItem>) {
        items = newItems
        notifyDataSetChanged()  // Refresh RecyclerView
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_detail, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class OrderItemViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvRate: TextView = itemView.findViewById(R.id.tvRate)

        fun bind(item: OrderItem) {
            tvItemName.text = item.itemName
            tvQuantity.text = item.quantity.toString()
            tvRate.text = "₹${item.itemPrice * item.quantity}"
        }
    }
}