import com.example.hi.R
import com.example.hi.model.breakfast

object Constants1 {
    fun getProducts(): List<breakfast> {
        return listOf(
            breakfast(1, R.drawable.chai, "Ginger Tea", 20, "Refreshing hot ginger tea."),
            breakfast(2, R.drawable.breakfast_cofee, "Cappuccino Coffee", 80, "Rich, foamy cappuccino with creamy milk."),
            breakfast(3, R.drawable.breakfast_bread, "Bread", 40, "Soft, fresh bread slices."),
            breakfast(4, R.drawable.breakfast_sandwich, "Sandwich", 60, "Grilled sandwich with veggies."),
            breakfast(5, R.drawable.breakfast_waffers, "Cookies", 140, "Crunchy wafers and cookies."),
            breakfast(6, R.drawable.breakfast_pancakes, "Pancake", 120, "Sweet fluffy pancakes with syrup.")
        )
    }
}


