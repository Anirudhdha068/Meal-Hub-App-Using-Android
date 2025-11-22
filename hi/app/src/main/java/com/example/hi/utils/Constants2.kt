package com.example.hi.utils

import com.example.hi.R
import com.example.hi.model.lunch

object Constants2 {fun getProducts(): List<lunch> {
    return listOf(
        lunch(1, R.drawable.lunch_pizza, "Pizza", 240, "Classic cheese pizza with a crispy crust and rich tomato sauce."),
        lunch(2, R.drawable.lunch_french_fries, "French Fries", 90, "Crispy golden fries seasoned to perfection."),
        lunch(3, R.drawable.lunch_noodles, "Noodles", 140, "Stir-fried noodles tossed with fresh veggies and savory seasoning."),
        lunch(4, R.drawable.lunch_burger, "Burger", 110, "Soft bun stuffed with a flavorful patty, veggies, and special sauce."),
        lunch(5, R.drawable.lunch_blackforest, "BlackForest Pastry", 80, "Soft chocolate sponge layered with whipped cream and cherries."),
        lunch(6, R.drawable.lunch_hotdog, "Hot Dog", 70, "Juicy sausage served in a warm bun with mustard and ketchup.")
    )
}
}