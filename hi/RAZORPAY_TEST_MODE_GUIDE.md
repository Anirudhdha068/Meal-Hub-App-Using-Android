# Razorpay Test Mode - Dummy Payment Guide

## ✅ Test Mode Setup Complete

The app is now configured for **TEST MODE** with dummy payment credentials.

## 🔑 Step 1: Get Your Razorpay Test Key

1. Go to [Razorpay Dashboard](https://dashboard.razorpay.com/)
2. Login or create a free account
3. Navigate to **Settings** → **API Keys**
4. Copy your **Test Key** (starts with `rzp_test_`)
5. Update `Constants.kt`:
   ```kotlin
   const val RAZORPAY_KEY_ID = "rzp_test_YOUR_ACTUAL_TEST_KEY"
   ```

## 💳 Test Payment Credentials

### 1. Credit/Debit Cards (Test Mode)

**✅ Success Cards:**
- **Visa**: `4111 1111 1111 1111`
- **Mastercard**: `5104 0600 0000 0008`
- **RuPay**: `6073 8460 7384 6073`

**Common Details for All Test Cards:**
- **CVV**: `123`
- **Expiry Month**: `12`
- **Expiry Year**: `25` (or any future year)
- **Cardholder Name**: Any name (e.g., "Test User")

**❌ Failure Cards (for testing errors):**
- Declined: `4000 0000 0000 0002`
- Insufficient Funds: `4000 0000 0000 9995`

### 2. UPI (Test Mode)

**✅ Success UPI IDs:**
- `success@razorpay`
- `success@upi`
- Any UPI ID ending with `@razorpay` or `@upi`

**❌ Failure UPI:**
- `failure@razorpay`

**Note**: In test mode, you can use any UPI ID. Razorpay will simulate success/failure based on the ID pattern.

### 3. Wallets (Test Mode)

**Paytm:**
- Use any test credentials
- Enable in Razorpay Dashboard → Settings → Payment Methods → Wallets

**Freecharge:**
- Use any test credentials
- Enable in Razorpay Dashboard

**Other Wallets:**
- Mobikwik, Airtel Money, etc.
- Enable in Razorpay Dashboard

## 🧪 How to Test

### Test Credit/Debit Card Payment:
1. Click "PROCEED TO PAY"
2. Select "Card" payment method
3. Enter test card: `4111 1111 1111 1111`
4. Enter CVV: `123`
5. Enter Expiry: `12/25`
6. Enter any name
7. Click "Pay"
8. ✅ Payment will succeed (no real money charged)

### Test UPI Payment:
1. Click "PROCEED TO PAY"
2. Select "UPI" payment method
3. Enter UPI ID: `success@razorpay`
4. Click "Pay"
5. ✅ Payment will succeed (no real money charged)

### Test Wallet Payment:
1. Click "PROCEED TO PAY"
2. Select "Wallet" (Paytm/Freecharge/etc.)
3. Use test credentials
4. ✅ Payment will succeed (no real money charged)

## 📱 Payment Methods Configuration

### Enable Payment Methods in Razorpay Dashboard:
1. Login to Razorpay Dashboard
2. Go to **Settings** → **Payment Methods**
3. Enable:
   - ✅ **Cards** (Credit/Debit)
   - ✅ **UPI**
   - ✅ **Wallets** (Paytm, Freecharge, etc.)
   - ❌ Netbanking (optional)
   - ❌ EMI (optional)

## ⚠️ Important Notes

1. **Test Mode Only**: These credentials work ONLY in test mode
2. **No Real Money**: No actual money will be charged
3. **Test Key Required**: Must use `rzp_test_...` key (not live key)
4. **Dashboard Settings**: Payment methods must be enabled in Razorpay Dashboard
5. **Test Data**: All transactions are simulated, not real

## 🔄 Switching to Live Mode

When ready for production:
1. Get Live Key from Razorpay Dashboard (starts with `rzp_live_`)
2. Update `Constants.kt`:
   ```kotlin
   const val RAZORPAY_KEY_ID = "rzp_live_YOUR_LIVE_KEY"
   const val IS_TEST_MODE = false
   ```
3. Remove test mode indicator from UI
4. Test with real payment methods

## 📝 Test Helper Class

A helper class `RazorpayTestHelper.kt` has been created with:
- Test card numbers
- Test UPI IDs
- Test wallet info
- Test instructions

You can reference this class for all test credentials.

## ✅ Current Configuration

- ✅ Test mode enabled
- ✅ Test key validation removed (allows test keys)
- ✅ Test mode indicator in UI
- ✅ Payment methods configured (Card, UPI, Wallet)
- ✅ Dummy payment ready

## 🎯 Quick Test Checklist

- [ ] Get Razorpay Test Key from Dashboard
- [ ] Update `Constants.RAZORPAY_KEY_ID` with test key
- [ ] Enable payment methods in Razorpay Dashboard
- [ ] Test with card: `4111 1111 1111 1111`
- [ ] Test with UPI: `success@razorpay`
- [ ] Test with Wallet (if enabled)
- [ ] Verify payment success flow
- [ ] Check order tracking after payment







