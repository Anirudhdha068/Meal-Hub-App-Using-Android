package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.hi.model.CartItem
import com.example.hi.model.Order
import com.example.hi.model.OrderItem
import com.example.hi.model.Userr
import com.example.hi.repository.OrderRepository
import com.example.hi.utils.CartManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*

class OrderActivity : AppCompatActivity() {

    private lateinit var itemsLayout: LinearLayout
    private lateinit var totalAmountTV: TextView
    private lateinit var btnConfirmOrder: Button
    private lateinit var tableNumberET: EditText
    private lateinit var customerNameET: EditText
    private lateinit var phoneET: EditText
    private lateinit var emailET: EditText
    private lateinit var cookingInstructionET: EditText

    private var cartList = ArrayList<CartItem>()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val orderRepository = OrderRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val rootLayout =
            findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.rootLayout)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            val sysBars = insets.getInsets(
                androidx.core.view.WindowInsetsCompat.Type.systemBars()
            )
            view.updatePadding(top = sysBars.top, bottom = sysBars.bottom)
            insets
        }

        window.statusBarColor = getColor(R.color.orange)

        CartManager.loadCart(this)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        itemsLayout = findViewById(R.id.billTable)
        totalAmountTV = findViewById(R.id.totalAmount)
        btnConfirmOrder = findViewById(R.id.btnPay)
        customerNameET = findViewById(R.id.etCustomerName)
        phoneET = findViewById(R.id.etPhone)
        emailET = findViewById(R.id.etEmail)
        tableNumberET = findViewById(R.id.etTableNumber)
        cookingInstructionET = findViewById(R.id.cookingInstruction)

        loadUserDetails()
        btnBack.setOnClickListener { finish() }

        cartList = CartManager.cartList
        displayItems()
        updateTotal()

        Log.d("OrderActivity", "Activity loaded successfully")

        // 🟢 Bottom Navigation (Removed finish())
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_cart
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, activitybreakfast::class.java))
                R.id.nav_menu -> startActivity(Intent(this, activityhome1::class.java))
                R.id.nav_feedback -> startActivity(Intent(this, FeedbackActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_cart -> true
                else -> false
            }
            true
        }
    }

    private fun loadUserDetails() {
        val currentUser = auth.currentUser ?: return

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(Userr::class.java)
                    user?.let {
                        customerNameET.setText(it.fullname)
                        phoneET.setText(it.phone)
                        emailET.setText(it.email)
                        tableNumberET.setText(it.tableNumber)
                    }
                }
            }
            .addOnFailureListener {
                Log.e("OrderActivity", "Failed to load user details: ${it.message}")
            }
    }

    private fun displayItems() {
        itemsLayout.removeAllViews()
        for ((index, item) in cartList.withIndex()) {
            val row = layoutInflater.inflate(R.layout.row_item_order, itemsLayout, false)

            row.findViewById<TextView>(R.id.itemName).text = item.Itemname
            row.findViewById<TextView>(R.id.itemQty).text = item.Itemquantity.toString()
            row.findViewById<TextView>(R.id.itemRate).text =
                "₹${item.ItemPrice * item.Itemquantity}"

            row.findViewById<ImageView>(R.id.btnDeleteItem).setOnClickListener {
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
        val total = cartList.sumOf { it.ItemPrice * it.Itemquantity }
        totalAmountTV.text = "Total: ₹$total"
        btnConfirmOrder.isEnabled = total > 0
        btnConfirmOrder.alpha = if (total > 0) 1f else 0.5f

        btnConfirmOrder.setOnClickListener {
            if (!validateUserDetails()) return@setOnClickListener
            createOrder(total)
        }
    }

    private fun validateUserDetails(): Boolean {
        if (customerNameET.text.isEmpty() ||
            phoneET.text.isEmpty() ||
            emailET.text.isEmpty() ||
            tableNumberET.text.isEmpty()
        ) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(emailET.text).matches()) {
            Toast.makeText(this, "Invalid email!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun createOrder(totalAmount: Int) {
        val currentUser = auth.currentUser ?: return
        val orderId = UUID.randomUUID().toString()

        val orderItems = cartList.map { item ->
            OrderItem(
                itemName = item.Itemname,
                itemPrice = item.ItemPrice,
                quantity = item.Itemquantity
            )
        }

        val order = Order(
            orderId = orderId,
            userId = currentUser.uid,
            customerName = customerNameET.text.toString(),
            phone = phoneET.text.toString(),
            email = emailET.text.toString(),
            tableNumber = tableNumberET.text.toString(),
            items = orderItems,
            totalAmount = totalAmount,
            status = "Order Placed",
            cookingInstructions = cookingInstructionET.text.toString(),
            timestamp = System.currentTimeMillis()
        )

        btnConfirmOrder.isEnabled = false
        btnConfirmOrder.text = "Placing Order..."

        lifecycleScope.launch {
            saveUserDetails()  // Keep saving details for future

            val result = orderRepository.saveOrder(order)
            if (result.isSuccess) {
                goToSuccessPage(orderId)
            } else {
                btnConfirmOrder.isEnabled = true
                btnConfirmOrder.text = "Confirm Order (COD)"
            }
        }
    }

    private fun saveUserDetails() {
        val currentUser = auth.currentUser ?: return

        val user = Userr(
            fullname = customerNameET.text.toString(),
            phone = phoneET.text.toString(),
            email = emailET.text.toString(),
            tableNumber = tableNumberET.text.toString()
        )

        db.collection("users").document(currentUser.uid)
            .set(user)
            .addOnSuccessListener { Log.d("OrderActivity", "User details saved") }
            .addOnFailureListener {
                Log.e("OrderActivity", "Failed to save user: ${it.message}")
            }
    }

    private fun goToSuccessPage(orderId: String) {
        val totalAmount = cartList.sumOf { it.ItemPrice * it.Itemquantity }
        val timestamp = System.currentTimeMillis()

        val intent = Intent(this, PaymentSuccessActivity::class.java)
        intent.putExtra("ORDER_ID", orderId)
        intent.putExtra("ORDER_EMAIL", emailET.text.toString())
        intent.putExtra("ORDER_TABLE", tableNumberET.text.toString())
        intent.putExtra("TOTAL_AMOUNT", totalAmount)
        intent.putExtra("ORDER_TIMESTAMP", timestamp)
        intent.putExtra("ORDER_TRACKING_NO", "TRK${orderId.takeLast(8)}") // FIXED KEY NAME

        startActivity(intent)
        finish()
    }
}