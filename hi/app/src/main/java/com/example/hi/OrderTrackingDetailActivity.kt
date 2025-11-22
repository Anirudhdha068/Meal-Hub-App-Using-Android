package com.example.hi

import android.os.Bundle
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
import com.example.hi.utils.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OrderTrackingDetailActivity : AppCompatActivity() {

    private lateinit var tvOrderDate: TextView
    private lateinit var tvMealReady: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvPaymentMethod: TextView
    private lateinit var ivPaymentLogo: ImageView
    private lateinit var recyclerViewItems: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var scrollView: ScrollView

    private lateinit var orderId: String
    private val orderRepository = OrderRepository()
    private lateinit var itemsAdapter: OrderItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_tracking_detail)

        window.statusBarColor = getColor(R.color.orange)

        orderId = intent.getStringExtra("ORDER_ID") ?: ""

        // Views
        tvOrderDate = findViewById(R.id.tvOrderDate)
        tvMealReady = findViewById(R.id.tvMealReady)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod)
        ivPaymentLogo = findViewById(R.id.ivPaymentLogo)
        recyclerViewItems = findViewById(R.id.recyclerViewItems)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        scrollView = findViewById(R.id.scrollView)

        // RecyclerView
        itemsAdapter = OrderItemsAdapter(emptyList())
        recyclerViewItems.layoutManager = LinearLayoutManager(this)
        recyclerViewItems.adapter = itemsAdapter

        // Back button
        findViewById<ImageView>(R.id.menuIcon).setOnClickListener { finish() }

        // Load order
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
                if (order != null && order.paymentStatus == Constants.PAYMENT_STATUS_SUCCESS) {
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
        tvEmptyState.visibility = TextView.GONE
        scrollView.visibility = ScrollView.VISIBLE

        // Order date
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        tvOrderDate.text = "Order date: ${sdf.format(Date(order.timestamp))}"

        // Meal ready estimate
        val minutes = (15..30).random()
        tvMealReady.text = "Meal will be ready: in $minutes min"

        // Total amount
        tvTotalAmount.text = "₹${order.totalAmount}"

        // Items list
        itemsAdapter.updateItems(order.items)

        // Payment method
        val paymentInfo = getPaymentMethodDisplay(order.paymentMethod, order.transactionId)
        tvPaymentMethod.text = paymentInfo.text
        setPaymentLogo(paymentInfo.type)
    }

    private fun showEmptyState() {
        tvEmptyState.visibility = TextView.VISIBLE
        scrollView.visibility = ScrollView.GONE
    }

    private fun getPaymentMethodDisplay(paymentMethod: String, transactionId: String): PaymentInfo {
        val method = paymentMethod.lowercase().trim()
        val lastDigits = if (transactionId.isNotEmpty() && transactionId.length >= 2) {
            transactionId.takeLast(2)
        } else "XX"

        return when {
            method.contains("card") || method.isEmpty() -> PaymentInfo("Card **$lastDigits", "card")
            method.contains("upi") -> PaymentInfo("UPI", "upi")
            method.contains("paytm") -> PaymentInfo("Paytm Wallet", "wallet")
            method.contains("freecharge") -> PaymentInfo("Freecharge", "wallet")
            method.contains("mobikwik") -> PaymentInfo("Mobikwik", "wallet")
            method.contains("airtel") -> PaymentInfo("Airtel Money", "wallet")
            method.contains("wallet") -> PaymentInfo("Wallet", "wallet")
            else -> PaymentInfo("Card **$lastDigits", "card")
        }
    }

    private fun setPaymentLogo(type: String) {
        when (type) {
            "card" -> ivPaymentLogo.setImageResource(R.drawable.visa_logo)
            "upi" -> ivPaymentLogo.setImageResource(R.drawable.upii)
            "wallet" -> ivPaymentLogo.setImageResource(R.drawable.wallet)
            else -> ivPaymentLogo.setImageResource(R.drawable.visa_logo)
        }
    }

    private data class PaymentInfo(val text: String, val type: String)
}

// Adapter for order items
class OrderItemsAdapter(private var items: List<OrderItem>) :
    RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder>() {

    fun updateItems(newItems: List<OrderItem>) {
        items = newItems
        notifyDataSetChanged()
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
