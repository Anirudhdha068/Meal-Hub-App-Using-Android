package com.example.hi.utils

/**
 * Razorpay Test Payment Helper
 * Provides test credentials for dummy payments in test mode
 * 
 * IMPORTANT: These are for TEST MODE only. Never use in production!
 */
object RazorpayTestHelper {
    
    /**
     * Test Credit/Debit Cards for Razorpay Test Mode
     * Use these cards to test payment without real money
     */
    object TestCards {
        // Success Cards
        const val VISA_SUCCESS = "4111111111111111"
        const val MASTERCARD_SUCCESS = "5104060600000008"
        const val RUPAY_SUCCESS = "6073846073846073"
        
        // Failure Cards
        const val CARD_DECLINED = "4000000000000002"
        const val INSUFFICIENT_FUNDS = "4000000000009995"
        
        // CVV for all test cards
        const val CVV = "123"
        
        // Expiry date (any future date)
        const val EXPIRY_MONTH = "12"
        const val EXPIRY_YEAR = "25"
        
        // Cardholder name (any name)
        const val CARDHOLDER_NAME = "Test User"
    }
    
    /**
     * Test UPI IDs for Razorpay Test Mode
     */
    object TestUPI {
        // Success UPI
        const val SUCCESS = "success@razorpay"
        const val SUCCESS_2 = "success@upi"
        
        // Failure UPI
        const val FAILURE = "failure@razorpay"
        
        // Note: You can also use any UPI ID in test mode
        // Razorpay will simulate success/failure based on the ID
    }
    
    /**
     * Test Wallets for Razorpay Test Mode
     */
    object TestWallets {
        // Paytm Test
        const val PAYTM_TEST = "paytm@test"
        
        // Freecharge Test
        const val FREECHARGE_TEST = "freecharge@test"
        
        // Note: Wallet testing depends on Razorpay dashboard configuration
        // Enable wallets in: Settings > Payment Methods > Wallets
    }
    
    /**
     * Get test payment instructions
     */
    fun getTestInstructions(): String {
        return """
            RAZORPAY TEST MODE - DUMMY PAYMENT INSTRUCTIONS
            
            📱 CREDIT/DEBIT CARD:
            Card Number: 4111 1111 1111 1111
            CVV: 123
            Expiry: 12/25
            Name: Any Name
            
            💳 ALTERNATIVE TEST CARDS:
            • Mastercard: 5104 0600 0000 0008
            • RuPay: 6073 8460 7384 6073
            
            📲 UPI:
            UPI ID: success@razorpay
            (Or use any UPI ID - Razorpay will simulate success)
            
            💰 WALLETS:
            • Paytm: Use test credentials
            • Freecharge: Use test credentials
            (Enable in Razorpay Dashboard > Payment Methods)
            
            ⚠️ NOTE: These are TEST credentials only!
            No real money will be charged in test mode.
        """.trimIndent()
    }
}







