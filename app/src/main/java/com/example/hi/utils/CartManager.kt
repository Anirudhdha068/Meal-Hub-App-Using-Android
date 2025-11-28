package com.example.hi.utils

import android.content.Context
import com.example.hi.model.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {

    var cartList = ArrayList<CartItem>()

    // Save cart permanently
    fun saveCart(context: Context) {
        val shared = context.getSharedPreferences("CART", Context.MODE_PRIVATE)
        val editor = shared.edit()

        val json = Gson().toJson(cartList)
        editor.putString("cart_items", json)
        editor.apply()
    }

    // Load saved cart
    fun loadCart(context: Context) {
        val shared = context.getSharedPreferences("CART", Context.MODE_PRIVATE)
        val json = shared.getString("cart_items", null)

        if (json != null) {
            val type = object : TypeToken<ArrayList<CartItem>>() {}.type
            cartList = Gson().fromJson(json, type)
        }
    }

    // Clear the cart after order is successful
    fun clearCart(context: Context) {
        cartList.clear() // Clear in-memory cart

        val shared = context.getSharedPreferences("CART", Context.MODE_PRIVATE)
        shared.edit().remove("cart_items").apply() // Delete from storage
    }
}
