package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hi.model.CartItem
import com.example.hi.model.Order
import com.example.hi.model.OrderItem
import com.example.hi.repository.OrderRepository
import com.example.hi.utils.CartManager
import com.example.hi.utils.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class OrderActivity : AppCompatActivity() {

    private lateinit var itemsLayout: LinearLayout
    private lateinit var totalAmountTV: TextView
    private lateinit var btnPay: Button

    private var cartList = ArrayList<CartItem>()
    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        window.statusBarColor = getColor(R.color.orange)

        CartManager.loadCart(this)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        itemsLayout = findViewById(R.id.billTable)
        totalAmountTV = findViewById(R.id.totalAmount)
        btnPay = findViewById(R.id.btnPay)

        btnBack.setOnClickListener { finish() }

        cartList = CartManager.cartList

        displayItems()
        updateTotal()

        // Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_cart

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
                    startActivity(Intent(this, TrackOrderActivity::class.java))
                    finish()
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
                R.id.nav_cart -> true
                else -> false
            }
        }
    }

    private fun displayItems() {
        itemsLayout.removeAllViews()

        for ((index, item) in cartList.withIndex()) {
            val row = layoutInflater.inflate(R.layout.row_item_order, itemsLayout, false)

            val nameTV = row.findViewById<TextView>(R.id.itemName)
            val qtyTV = row.findViewById<TextView>(R.id.itemQty)
            val rateTV = row.findViewById<TextView>(R.id.itemRate)
            val deleteBtn = row.findViewById<ImageView>(R.id.btnDeleteItem)

            nameTV.text = item.Itemname
            qtyTV.text = item.Itemquantity.toString()
            rateTV.text = "₹${item.ItemPrice * item.Itemquantity}"

            deleteBtn.setOnClickListener {
                CartManager.cartList.removeAt(index)
                CartManager.saveCart(this)
                cartList = CartManager.cartList
                displayItems()
                updateTotal()
            }

            itemsLayout.addView(row)
        }
    }

    private fun updateTotal() {
        var total = 0
        for (item in cartList) {
            total += item.ItemPrice * item.Itemquantity
        }

        totalAmountTV.text = "Total: ₹$total"

        btnPay.isEnabled = total > 0
        btnPay.alpha = if (total > 0) 1f else 0.5f

        btnPay.setOnClickListener {
            if (auth.currentUser == null) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cookingInstruction = findViewById<EditText>(R.id.cookingInstruction)
            createOrder(total, cookingInstruction.text.toString())
        }
    }

    private fun createOrder(totalAmount: Int, cookingInstructions: String) {
        val currentUser = auth.currentUser ?: return
        val orderId = UUID.randomUUID().toString()

        val orderItems = cartList.map { cartItem ->
            OrderItem(
                itemName = cartItem.Itemname,
                itemPrice = cartItem.ItemPrice,
                quantity = cartItem.Itemquantity
            )
        }

        val order = Order(
            orderId = orderId,
            userId = currentUser.uid,
            items = orderItems,
            totalAmount = totalAmount,
            cookingInstructions = cookingInstructions,
            paymentStatus = Constants.PAYMENT_STATUS_PENDING,
            status = Constants.STATUS_ORDER_PLACED,
            timestamp = System.currentTimeMillis()
        )

        btnPay.isEnabled = false
        btnPay.text = "Creating Order..."

        lifecycleScope.launch {
            val result = orderRepository.saveOrder(order)

            if (result.isSuccess) {
                // Launch PaymentActivity
                val intent = Intent(this@OrderActivity, PaymentActivity::class.java)
                intent.putExtra("TOTAL_AMOUNT", totalAmount)
                intent.putExtra("ORDER_ID", orderId)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this@OrderActivity,
                    "Failed to create order: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
                btnPay.isEnabled = true
                btnPay.text = "PAY NOW"
            }
        }
    }
}
