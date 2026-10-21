package com.example.pertemuan3.project

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pertemuan3.R

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val navCatalog: LinearLayout = findViewById(R.id.navCatalog)
        val navKeranjang: LinearLayout = findViewById(R.id.navKeranjang)
        val navProfil: LinearLayout = findViewById(R.id.navProfil)

        val btnLihatLokasi: TextView = findViewById(R.id.btnLihatLokasi)
        val cardMadu: LinearLayout = findViewById(R.id.cardMadu)
        val cardKopi: LinearLayout = findViewById(R.id.cardKopi)
        val cardMinyak: LinearLayout = findViewById(R.id.cardMinyak)
        val cardSusu: LinearLayout = findViewById(R.id.cardSusu)

        navCatalog.setOnClickListener {
            startActivity(Intent(this, CatalogActivity::class.java))
        }

        navKeranjang.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        navProfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        cardMadu.setOnClickListener {
            openProductDetail("Madu HPAI", "Rp 85.000", R.drawable.images)
        }

        cardKopi.setOnClickListener {
            openProductDetail("Kopi", "Rp 45.000", R.drawable.images_kopi)
        }

        cardMinyak.setOnClickListener {
            openProductDetail("Minyak HPAI", "Rp 120.000", R.drawable.images_minyak)
        }

        cardSusu.setOnClickListener {
            openProductDetail("Susu HPAI", "Rp 55.000", R.drawable.images_susu)
        }

        btnLihatLokasi.setOnClickListener {
            Toast.makeText(this, "BC HPAI Yogyakarta 2 - Lokasi Toko", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openProductDetail(name: String, price: String, imageRes: Int) {
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra("product_name", name)
            putExtra("product_price", price)
            putExtra("product_image", imageRes)
        }
        startActivity(intent)
    }
}
