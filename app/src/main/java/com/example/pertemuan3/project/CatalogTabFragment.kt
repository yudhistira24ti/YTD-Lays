package com.example.pertemuan3.project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.pertemuan3.R

class CatalogTabFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_catalog_tab, container, false)

        val catCardMadu: LinearLayout = view.findViewById(R.id.catCardMadu)
        val catCardKopi: LinearLayout = view.findViewById(R.id.catCardKopi)
        val catCardMinyak: LinearLayout = view.findViewById(R.id.catCardMinyak)
        val catCardSusu: LinearLayout = view.findViewById(R.id.catCardSusu)

        catCardMadu.setOnClickListener {
            openProductDetail("Madu HPAI", "Rp 85.000", R.drawable.images)
        }
        catCardKopi.setOnClickListener {
            openProductDetail("Kopi", "Rp 45.000", R.drawable.images_kopi)
        }
        catCardMinyak.setOnClickListener {
            openProductDetail("Minyak HPAI", "Rp 120.000", R.drawable.images_minyak)
        }
        catCardSusu.setOnClickListener {
            openProductDetail("Susu HPAI", "Rp 55.000", R.drawable.images_susu)
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
