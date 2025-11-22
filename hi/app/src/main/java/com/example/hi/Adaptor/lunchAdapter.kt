package com.example.hi.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hi.ProductDetailActivity
import com.example.hi.R
import com.example.hi.model.lunch

class lunchAdapter(
    private val productList: List<lunch>   // <-- CHANGED breakfast → lunch
) : RecyclerView.Adapter<lunchAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage1)
        val productName: TextView = itemView.findViewById(R.id.productName1)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item1_product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.productImage.setImageResource(product.Image)
        holder.productName.text = product.productname
        holder.productPrice.text = "₹${product.Price}"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("productName", product.productname)
                putExtra("productPrice", "₹${product.Price}")
                putExtra("productImage", product.Image)
                putExtra("productDescription", product.productDescription)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productList.size
}

