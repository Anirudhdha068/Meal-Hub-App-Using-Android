# Quick Start: Test Payment with Razorpay

## 🚀 Setup in 3 Steps

### Step 1: Get Razorpay Test Key
1. Go to https://dashboard.razorpay.com/
2. Sign up/Login (Free account)
3. Go to **Settings** → **API Keys**
4. Copy **Test Key** (starts with `rzp_test_`)

### Step 2: Update Constants.kt
```kotlin
const val RAZORPAY_KEY_ID = "rzp_test_YOUR_COPIED_KEY_HERE"
const val IS_TEST_MODE = true
```

### Step 3: Enable Payment Methods
1. In Razorpay Dashboard → **Settings** → **Payment Methods**
2. Enable: ✅ Cards, ✅ UPI, ✅ Wallets

## 💳 Test Payment Credentials

### Credit/Debit Card:
- **Card Number**: `4111 1111 1111 1111`
- **CVV**: `123`
- **Expiry**: `12/25`
- **Name**: Any name

### UPI:
- **UPI ID**: `success@razorpay`

### Wallets:
- Use test credentials (enable in dashboard)

## ✅ That's It!

Now you can test payments without real money. The app will show "TEST MODE" indicator and you can click it to see test instructions.




