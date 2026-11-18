package com.example.pertemuan3.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pertemuan3.R

class CatalogTabFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_catalog_tab, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        val products = listOf(
            ProductModel("Madu HPAI", "Rp 85.000", "50", R.drawable.images),
            ProductModel("Kopi", "Rp 45.000", "99", R.drawable.images_kopi),
            ProductModel("Minyak HPAI", "Rp 120.000", "30", R.drawable.images_minyak),
            ProductModel("Susu HPAI", "Rp 55.000", "40", R.drawable.images_susu)
        )

        recyclerView.adapter = ProductAdapter(products)

        return view
    }
}
