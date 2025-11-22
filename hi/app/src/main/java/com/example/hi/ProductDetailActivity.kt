package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.hi.model.CartItem
import com.example.hi.utils.CartManager

class ProductDetailActivity : AppCompatActivity() {

    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_detail)

        window.statusBarColor = getColor(R.color.orange)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val productImage = findViewById<ImageView>(R.id.productImage)
        val productName = findViewById<TextView>(R.id.productName)
        val productPrice = findViewById<TextView>(R.id.productPrice)
        val productDescription = findViewById<TextView>(R.id.productDescription)
        val txtQuantity = findViewById<TextView>(R.id.txtQuantity)
        val btnMinus = findViewById<ImageButton>(R.id.btnMinus)
        val btnPlus = findViewById<ImageButton>(R.id.btnPlus)
        val orderBtn = findViewById<Button>(R.id.orderBtn)

        btnBack.setOnClickListener { finish() }

        // Get product data from intent
        val name = intent.getStringExtra("productName") ?: "Unknown Product"
        val price = intent.getStringExtra("productPrice") ?: "₹0"
        val image = intent.getIntExtra("productImage", R.drawable.chai)
        val description = intent.getStringExtra("productDescription") ?: "No description available."

        productName.text = name
        productPrice.text = price
        productDescription.text = description
        productImage.setImageResource(image)

        txtQuantity.text = quantity.toString()

        btnMinus.setOnClickListener {
            if (quantity > 1) quantity--
            txtQuantity.text = quantity.toString()
        }

        btnPlus.setOnClickListener {
            quantity++
            txtQuantity.text = quantity.toString()
        }

        // Add to cart button
        orderBtn.setOnClickListener {

            val priceInt = price.replace("₹", "").trim().toInt()

            // Check if item already exists
            val existingItem = CartManager.cartList.find { it.Itemname == name }

            if (existingItem != null) {
                existingItem.Itemquantity += quantity // increase quantity
            } else {
                CartManager.cartList.add(
                    CartItem(
                        Itemname = name,
                        ItemPrice = priceInt,
                        Itemquantity = quantity
                    )
                )
            }

            // Save cart permanently
            CartManager.saveCart(this)

            Toast.makeText(this, "$name added (x$quantity)", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this, OrderActivity::class.java))
        }
    }
}
