package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hi.model.Order
import com.example.hi.model.OrderItem
import com.example.hi.repository.OrderRepository
import com.example.hi.utils.CartManager
import com.example.hi.utils.Constants
import com.example.hi.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class OrderPaymentActivity : AppCompatActivity() {

    private lateinit var totalAmountTV: TextView
    private lateinit var btnPay: Button
    private lateinit var btnBack: ImageView

    private var totalAmount: Int = 0
    private var orderId: String = ""
    private var cookingInstructions: String = ""

    private val orderRepository = OrderRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_payment)

        window.statusBarColor = getColor(R.color.orange)

        NotificationHelper.createNotificationChannel(this)

        totalAmountTV = findViewById(R.id.tvTotalAmount)
        btnPay = findViewById(R.id.btnPay)
        btnBack = findViewById<ImageView>(R.id.btnBack)

        totalAmount = intent.getIntExtra("TOTAL_AMOUNT", 0)
        cookingInstructions = intent.getStringExtra("COOKING_INSTRUCTIONS") ?: ""

        totalAmountTV.text = "Amount to Pay: ₹$totalAmount"

        btnBack.setOnClickListener { finish() }

        btnPay.setOnClickListener {
            if (totalAmount > 0) {
                createOrder()
            } else {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Create order in Firestore
     */
    private fun createOrder() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnPay.isEnabled = false
        btnPay.text = "Creating Order..."

        lifecycleScope.launch {
            try {
                if (CartManager.cartList.isEmpty()) {
                    Toast.makeText(
                        this@OrderPaymentActivity,
                        "Your cart is empty",
                        Toast.LENGTH_SHORT
                    ).show()
                    btnPay.isEnabled = true
                    btnPay.text = "PROCEED TO PAY"
                    return@launch
                }

                orderId = UUID.randomUUID().toString()

                val orderItems = CartManager.cartList.map { cartItem ->
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

                val result = orderRepository.saveOrder(order)

                if (result.isSuccess) {
                    // Order saved successfully → move to Stripe payment
                    val intent = Intent(this@OrderPaymentActivity, PaymentActivity::class.java)
                    intent.putExtra("TOTAL_AMOUNT", totalAmount)
                    intent.putExtra("ORDER_ID", orderId)
                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(
                        this@OrderPaymentActivity,
                        "Failed to create order: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    btnPay.isEnabled = true
                    btnPay.text = "PROCEED TO PAY"
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@OrderPaymentActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                btnPay.isEnabled = true
                btnPay.text = "PROCEED TO PAY"
            }
        }
    }
}
