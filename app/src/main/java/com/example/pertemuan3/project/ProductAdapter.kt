package com.example.pertemuan3.project

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pertemuan3.R

class ProductAdapter(private val productList: List<ProductModel>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProduct: ImageView = itemView.findViewById(R.id.ivProduct)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductStock: TextView = itemView.findViewById(R.id.tvProductStock)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val btnInfo: Button = itemView.findViewById(R.id.btnInfo)
        val btnBuy: Button = itemView.findViewById(R.id.btnBuy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.tvProductName.text = product.name
        holder.tvProductStock.text = "Stok: ${product.stock}"
        holder.tvProductPrice.text = product.price
        holder.ivProduct.setImageResource(product.imageRes)

        val context = holder.itemView.context

        val openDetail = {
            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                putExtra("product_name", product.name)
                putExtra("product_price", product.price)
                putExtra("product_image", product.imageRes)
            }
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener { openDetail() }
        holder.btnInfo.setOnClickListener { openDetail() }
        holder.btnBuy.setOnClickListener { openDetail() }
    }

    override fun getItemCount(): Int = productList.size
}
