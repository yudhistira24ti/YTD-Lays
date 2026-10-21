package com.example.pertemuan3.project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pertemuan3.R

class HomeTabFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_tab, container, false)

        val btnLihatLokasi: TextView = view.findViewById(R.id.btnLihatLokasi)
        val cardMadu: LinearLayout = view.findViewById(R.id.cardMadu)
        val cardKopi: LinearLayout = view.findViewById(R.id.cardKopi)
        val cardMinyak: LinearLayout = view.findViewById(R.id.cardMinyak)
        val cardSusu: LinearLayout = view.findViewById(R.id.cardSusu)

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
            Toast.makeText(requireContext(), "BC HPAI Yogyakarta 2 - Lokasi Toko", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun openProductDetail(name: String, price: String, imageRes: Int) {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java).apply {
            putExtra("product_name", name)
            putExtra("product_price", price)
            putExtra("product_image", imageRes)
        }
        startActivity(intent)
    }
}
