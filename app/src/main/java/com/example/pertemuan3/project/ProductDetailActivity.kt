package com.example.pertemuan3.project

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pertemuan3.R

class ProductDetailActivity : AppCompatActivity() {

    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val btnBack: ImageView = findViewById(R.id.btnBack)
        val btnShare: ImageView = findViewById(R.id.btnShare)
        val ivProductImage: ImageView = findViewById(R.id.ivProductImage)
        val tvProductName: TextView = findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = findViewById(R.id.tvProductPrice)
        val tvQuantity: TextView = findViewById(R.id.tvQuantity)
        val btnMinus: Button = findViewById(R.id.btnMinus)
        val btnPlus: Button = findViewById(R.id.btnPlus)
        val btnAddToCart: Button = findViewById(R.id.btnAddToCart)
        val btnBuyNow: Button = findViewById(R.id.btnBuyNow)

        // Get intent extras if provided
        val productName = intent.getStringExtra("product_name") ?: "Madu HPAI"
        val productPrice = intent.getStringExtra("product_price") ?: "Rp 85.000"
        val productImage = intent.getIntExtra("product_image", R.drawable.images)

        tvProductName.text = productName
        tvProductPrice.text = productPrice
        ivProductImage.setImageResource(productImage)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnShare.setOnClickListener {
            Toast.makeText(this, "Bagikan produk $productName", Toast.LENGTH_SHORT).show()
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                tvQuantity.text = quantity.toString()
            }
        }

        btnPlus.setOnClickListener {
            quantity++
            tvQuantity.text = quantity.toString()
        }

        btnAddToCart.setOnClickListener {
            Toast.makeText(this, "$quantity $productName berhasil ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
        }

        btnBuyNow.setOnClickListener {
            Toast.makeText(this, "Memproses pembelian $quantity $productName", Toast.LENGTH_SHORT).show()
        }
    }
}
