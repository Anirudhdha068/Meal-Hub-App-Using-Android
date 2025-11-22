package com.example.hi.utils

import com.example.hi.R
import com.example.hi.model.dinner

object Constants3 {
    fun getProducts(): List<dinner> {
    return listOf(
        dinner(1, R.drawable.dinner_dosa, "Masala Dosa", 170, "Crispy golden dosa filled with flavorful spiced potato masala, served with chutney and sambar."),
        dinner(2, R.drawable.dinner_gujrati, "Gujarati Thali", 140, "A wholesome Gujarati meal with roti, dal, sabzi, rice, farsan, and sweet dish."),
        dinner(3, R.drawable.dinner_punjabi, "Punjabi Thali", 240, "Rich North Indian platter with naan, dal makhani, paneer gravy, rice, salad, and dessert."),
        dinner(4, R.drawable.dinner_masrum, "Mushroom Curry", 250, "Tender mushrooms cooked in a creamy, mildly spiced gravy."),
        dinner(5, R.drawable.dinner_paneer_roll, "Paneer Roll", 90, "Soft wrap filled with marinated paneer, veggies, and flavorful sauces."),
        dinner(6, R.drawable.dinner_panner_chilli, "Paneer Chilli", 210, "Crispy paneer cubes tossed in spicy Indo-Chinese chilli sauce with veggies.")
    )
}
}