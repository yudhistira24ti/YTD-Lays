package com.example.pertemuan3.project

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pertemuan3.R
import com.example.pertemuan3.service.SupabaseService
import kotlinx.coroutines.launch
import org.json.JSONArray

class CatalogTabFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private val productList = mutableListOf<ProductModel>()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_catalog_tab, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter

        loadProductsFromSupabase()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadProductsFromSupabase()
    }

    private fun loadProductsFromSupabase() {
        lifecycleScope.launch {
            SupabaseService.getProducts { jsonArray ->
                productList.clear()
                val loadedNames = mutableSetOf<String>()

                if (jsonArray != null && jsonArray.length() > 0) {
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val name = obj.optString("name", "Produk Herbal")
                        val price = "Rp " + obj.optString("price", "0")
                        val stock = obj.optString("stock", "0")
                        loadedNames.add(name)
                        productList.add(ProductModel(name, price, stock, R.drawable.images))
                    }
                }

                // Ambil juga produk yang baru ditambahkan secara lokal
                val prefs = requireContext().getSharedPreferences("products_prefs", Context.MODE_PRIVATE)
                val rawJson = prefs.getString("local_products_json", "[]") ?: "[]"
                val localArray = JSONArray(rawJson)
                for (i in 0 until localArray.length()) {
                    val obj = localArray.getJSONObject(i)
                    val name = obj.optString("name")
                    if (!loadedNames.contains(name)) {
                        val price = "Rp " + obj.optString("price")
                        val stock = obj.optString("stock")
                        productList.add(ProductModel(name, price, stock, R.drawable.images))
                        loadedNames.add(name)
                    }
                }

                productAdapter.notifyDataSetChanged()
            }
        }
    }
}
