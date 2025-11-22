package com.example.hi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.hi.repository.OrderRepository
import com.example.hi.utils.CartManager
import com.example.hi.utils.Constants
import com.google.firebase.functions.FirebaseFunctions
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.StripeIntent
import com.stripe.android.view.CardInputWidget
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class PaymentActivity : AppCompatActivity() {

    private lateinit var stripe: Stripe
    private lateinit var functions: FirebaseFunctions
    private val orderRepository = OrderRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        window.statusBarColor = getColor(R.color.orange)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnPay = findViewById<Button>(R.id.btnPay)
        val totalTV = findViewById<TextView>(R.id.tvTotalAmount)
        val cardInputWidget = findViewById<CardInputWidget>(R.id.cardInputWidget)

        val totalAmount = intent.getIntExtra("TOTAL_AMOUNT", 0)
        val orderId = intent.getStringExtra("ORDER_ID") ?: ""
        totalTV.text = "Amount to Pay: ₹$totalAmount"

        functions = FirebaseFunctions.getInstance("http://10.0.2.2:5001/myapp-1757c/us-central1")

        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51SWBVGBk3AEyPzjsI7FWJfSUihTTbPx0wXnOpjPUnBiFQbz7Eb0UYqxLsQjbCc12h54LAkrzLU2qHCI0VZlEHu4Z00pmKcVd3F"
        )

        stripe = Stripe(applicationContext, PaymentConfiguration.getInstance(this).publishableKey)

        btnBack.setOnClickListener { onBackPressed() }

        btnPay.setOnClickListener {
            if (totalAmount > 0 && orderId.isNotEmpty()) {
                btnPay.isEnabled = false
                btnPay.text = "Processing..."
                createPaymentIntent(totalAmount, cardInputWidget, orderId, btnPay)
            } else {
                Toast.makeText(this, "Invalid amount or order ID", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createPaymentIntent(
        amount: Int,
        cardInputWidget: CardInputWidget,
        orderId: String,
        btnPay: Button
    ) {
        val amountInPaise = amount * 100

        functions.getHttpsCallable("createPaymentIntent")
            .call(mapOf("amount" to amountInPaise))
            .addOnSuccessListener { result ->
                val data = result.data as Map<*, *>
                val clientSecret = data["clientSecret"].toString()
                confirmPayment(clientSecret, cardInputWidget, orderId, btnPay)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                btnPay.isEnabled = true
                btnPay.text = "PAY NOW"
            }
    }

    private fun confirmPayment(
        clientSecret: String,
        cardInputWidget: CardInputWidget,
        orderId: String,
        btnPay: Button
    ) {
        val params = cardInputWidget.paymentMethodCreateParams
        if (params != null) {
            val confirmParams = ConfirmPaymentIntentParams
                .createWithPaymentMethodCreateParams(params, clientSecret)
            stripe.confirmPayment(this, confirmParams)
        } else {
            Toast.makeText(this, "Enter valid card details", Toast.LENGTH_SHORT).show()
            btnPay.isEnabled = true
            btnPay.text = "PAY NOW"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        stripe.onPaymentResult(requestCode, data, PaymentResultCallback(this))
    }

    private class PaymentResultCallback(activity: PaymentActivity) :
        ApiResultCallback<PaymentIntentResult> {

        private val activityRef = WeakReference(activity)

        override fun onSuccess(result: PaymentIntentResult) {
            val activity = activityRef.get() ?: return
            val btnPay = activity.findViewById<Button>(R.id.btnPay)
            btnPay.isEnabled = true
            btnPay.text = "PAY NOW"

            val orderId = activity.intent.getStringExtra("ORDER_ID") ?: ""
            val totalAmount = activity.intent.getIntExtra("TOTAL_AMOUNT", 0)

            if (result.intent.status == StripeIntent.Status.Succeeded) {
                // Update Firestore
                if (orderId.isNotEmpty()) {
                    activity.lifecycleScope.launch {
                        activity.orderRepository.updatePaymentStatus(orderId, Constants.PAYMENT_STATUS_SUCCESS)
                    }
                }

                // Clear cart
                CartManager.clearCart(activity)

                // Redirect to success page
                val intent = Intent(activity, PaymentSuccessActivity::class.java)
                intent.putExtra("ORDER_ID", orderId)
                intent.putExtra("TOTAL_AMOUNT", totalAmount)
                activity.startActivity(intent)
                activity.finish()

            } else {
                // Payment failed
                if (orderId.isNotEmpty()) {
                    activity.lifecycleScope.launch {
                        activity.orderRepository.updatePaymentStatus(orderId, Constants.PAYMENT_STATUS_FAILED)
                    }
                }

                val intent = Intent(activity, PaymentFailedActivity::class.java)
                intent.putExtra("ORDER_ID", orderId)
                intent.putExtra("TOTAL_AMOUNT", totalAmount)
                activity.startActivity(intent)
                activity.finish()
            }
        }

        override fun onError(e: Exception) {
            val activity = activityRef.get() ?: return
            val btnPay = activity.findViewById<Button>(R.id.btnPay)
            btnPay.isEnabled = true
            btnPay.text = "PAY NOW"
            Toast.makeText(activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
