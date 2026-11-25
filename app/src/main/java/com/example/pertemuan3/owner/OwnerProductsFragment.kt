package com.example.pertemuan3.owner

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pertemuan3.R
import com.example.pertemuan3.service.SupabaseService
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class OwnerProductsFragment : Fragment() {

    private data class ProductItem(
        val id: String?,
        val name: String,
        val category: String,
        val price: Double,
        val stock: Int,
        val imageUri: String?,
        val synced: Boolean = true
    )

    private lateinit var containerOwnerProducts: LinearLayout
    private lateinit var layoutEmptyProducts: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptyDesc: TextView
    private lateinit var tvProductCount: TextView
    private lateinit var tvLowStockCount: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var etSearchProduct: EditText
    private lateinit var ivClearSearch: ImageView

    private val allProducts = mutableListOf<ProductItem>()
    private var currentQuery: String = ""

    private var activeImageUri: Uri? = null
    private var dialogPreviewImageView: ImageView? = null
    private var dialogFileNameTextView: TextView? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null && isAdded) {
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }
                activeImageUri = uri
                dialogFileNameTextView?.text = uri.lastPathSegment ?: "Gambar dipilih"
                dialogPreviewImageView?.let { preview ->
                    preview.imageTintList = null
                    preview.scaleType = ImageView.ScaleType.CENTER_CROP
                    preview.setImageURI(uri)
                }
                Toast.makeText(requireContext(), "Gambar dipilih!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_owner_products, container, false)

        containerOwnerProducts = view.findViewById(R.id.containerOwnerProducts)
        layoutEmptyProducts = view.findViewById(R.id.layoutEmptyProducts)
        tvEmptyTitle = view.findViewById(R.id.tvEmptyProductsTitle)
        tvEmptyDesc = view.findViewById(R.id.tvEmptyProductsDesc)
        tvProductCount = view.findViewById(R.id.tvProductCount)
        tvLowStockCount = view.findViewById(R.id.tvLowStockCount)
        pbLoading = view.findViewById(R.id.pbProductsLoading)
        etSearchProduct = view.findViewById(R.id.etSearchProduct)
        ivClearSearch = view.findViewById(R.id.ivClearSearchProduct)
        val btnAddProduct: Button = view.findViewById(R.id.btnAddProduct)

        btnAddProduct.setOnClickListener {
            showProductDialog(editingItem = null)
        }

        etSearchProduct.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim() ?: ""
                ivClearSearch.visibility = if (currentQuery.isEmpty()) View.GONE else View.VISIBLE
                renderProducts()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        ivClearSearch.setOnClickListener {
            etSearchProduct.text.clear()
        }

        renderLocalProductsFirst()
        syncCategoriesCache()
        loadOwnerProducts()

        return view
    }

    override fun onResume() {
        super.onResume()
        syncCategoriesCache()
        loadOwnerProducts()
    }

    private fun showProductDialog(editingItem: ProductItem?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_owner_product, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle: TextView = dialogView.findViewById(R.id.tvDialogProductTitle)
        val btnClose: ImageView = dialogView.findViewById(R.id.btnDialogProductClose)
        val etName: EditText = dialogView.findViewById(R.id.etDialogProductName)
        val spinnerCat: Spinner = dialogView.findViewById(R.id.spinnerDialogCategory)
        val etPrice: EditText = dialogView.findViewById(R.id.etDialogProductPrice)
        val etStock: EditText = dialogView.findViewById(R.id.etDialogProductStock)
        val ivPreview: ImageView = dialogView.findViewById(R.id.ivDialogProductPreview)
        val tvFileName: TextView = dialogView.findViewById(R.id.tvDialogSelectedFileName)
        val btnPickImage: Button = dialogView.findViewById(R.id.btnDialogPickImage)
        val btnCancel: Button = dialogView.findViewById(R.id.btnDialogProductCancel)
        val btnSubmit: Button = dialogView.findViewById(R.id.btnDialogProductSubmit)

        dialogPreviewImageView = ivPreview
        dialogFileNameTextView = tvFileName
        activeImageUri = editingItem?.imageUri?.takeIf { it.isNotBlank() }?.let {
            try { Uri.parse(it) } catch (_: Exception) { null }
        }

        val categoriesList = loadCategoryOptions(extraCategory = editingItem?.category)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoriesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCat.adapter = adapter

        if (editingItem != null) {
            tvTitle.text = "Edit Produk"
            btnSubmit.text = "Simpan"
            etName.setText(editingItem.name)
            etName.setSelection(etName.text.length)
            etPrice.setText(formatEditableNumber(editingItem.price))
            etStock.setText(editingItem.stock.toString())

            val catIndex = categoriesList.indexOfFirst { it.equals(editingItem.category, ignoreCase = true) }
            if (catIndex >= 0) {
                spinnerCat.setSelection(catIndex)
            }

            if (activeImageUri != null) {
                bindPreviewImage(ivPreview, tvFileName, activeImageUri!!)
            }
        } else {
            tvTitle.text = "Tambah Produk Baru"
            btnSubmit.text = "Tambah"
        }

        btnPickImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        dialog.setOnDismissListener {
            dialogPreviewImageView = null
            dialogFileNameTextView = null
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val category = spinnerCat.selectedItem?.toString()?.trim() ?: ""
            val priceStr = etPrice.text.toString().trim()
            val stockStr = etStock.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Nama produk wajib diisi"
                etName.requestFocus()
                return@setOnClickListener
            }
            if (category.isEmpty() || category == "-- Pilih Kategori --") {
                Toast.makeText(requireContext(), "Silakan pilih kategori produk", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull()
            if (price == null || price < 0) {
                etPrice.error = "Masukkan harga yang valid"
                etPrice.requestFocus()
                return@setOnClickListener
            }

            val stock = stockStr.toIntOrNull()
            if (stock == null || stock < 0) {
                etStock.error = "Masukkan stok yang valid"
                etStock.requestFocus()
                return@setOnClickListener
            }

            val imageUriString = activeImageUri?.toString().orEmpty()
            btnSubmit.isEnabled = false
            btnSubmit.text = "Menyimpan..."

            if (editingItem != null) {
                lifecycleScope.launch {
                    SupabaseService.updateProduct(
                        id = editingItem.id,
                        originalName = editingItem.name,
                        name = name,
                        price = price,
                        stock = stock,
                        category = category
                    ) { success, errorMsg ->
                        if (!isAdded) return@updateProduct
                        saveProductImageUri(editingItem.name, null)
                        if (imageUriString.isNotEmpty()) {
                            saveProductImageUri(name, imageUriString)
                        }
                        updateProductLocal(
                            originalName = editingItem.name,
                            updated = ProductItem(editingItem.id, name, category, price, stock, imageUriString, synced = success)
                        )
                        if (success) {
                            Toast.makeText(requireContext(), "Produk '$name' berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("OwnerProducts", "Error update Supabase: $errorMsg")
                            Toast.makeText(requireContext(), "Diperbarui lokal (Supabase: ${errorMsg ?: "offline"})", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                        loadOwnerProducts()
                    }
                }
            } else {
                lifecycleScope.launch {
                    SupabaseService.addProduct(name, price, stock, category, imageUriString) { success, errorMsg ->
                        if (!isAdded) return@addProduct
                        if (imageUriString.isNotEmpty()) {
                            saveProductImageUri(name, imageUriString)
                        }
                        saveProductLocally(
                            ProductItem(null, name, category, price, stock, imageUriString, synced = success)
                        )
                        if (success) {
                            Toast.makeText(requireContext(), "Produk '$name' berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("OwnerProducts", "Error add Supabase: $errorMsg")
                            Toast.makeText(requireContext(), "Tersimpan lokal (Supabase: ${errorMsg ?: "offline"})", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                        loadOwnerProducts()
                    }
                }
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun confirmDeleteProduct(item: ProductItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus produk '${item.name}'?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    SupabaseService.deleteProduct(item.id, item.name) { success, errorMsg ->
                        if (!isAdded) return@deleteProduct
                        deleteProductLocal(item.name)
                        saveProductImageUri(item.name, null)
                        if (success) {
                            Toast.makeText(requireContext(), "Produk '${item.name}' dihapus", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("OwnerProducts", "Error delete Supabase: $errorMsg")
                            Toast.makeText(requireContext(), "Dihapus lokal (Supabase: ${errorMsg ?: "offline"})", Toast.LENGTH_SHORT).show()
                        }
                        loadOwnerProducts()
                    }
                }
            }
            .show()
    }

    private fun syncCategoriesCache() {
        lifecycleScope.launch {
            SupabaseService.getCategories { jsonArray ->
                if (!isAdded || jsonArray == null || jsonArray.length() == 0) return@getCategories
                val prefs = requireContext().getSharedPreferences("categories_prefs", Context.MODE_PRIVATE)
                val existing = LinkedHashSet(prefs.getStringSet("categories_list", emptySet()) ?: emptySet())
                for (i in 0 until jsonArray.length()) {
                    val name = jsonArray.getJSONObject(i).optString("name", "").trim()
                    if (name.isNotEmpty()) existing.add(name)
                }
                prefs.edit().putStringSet("categories_list", existing).apply()
            }
        }
    }

    private fun loadCategoryOptions(extraCategory: String?): List<String> {
        val prefs = requireContext().getSharedPreferences("categories_prefs", Context.MODE_PRIVATE)
        val categoriesSet = prefs.getStringSet("categories_list", emptySet()) ?: emptySet()

        val list = mutableListOf("-- Pilih Kategori --")
        list.addAll(categoriesSet.sorted())
        if (!extraCategory.isNullOrBlank() && list.none { it.equals(extraCategory, ignoreCase = true) }) {
            list.add(extraCategory)
        }
        return list
    }

    private fun renderLocalProductsFirst() {
        val localItems = readLocalProducts()
        if (localItems.isNotEmpty()) {
            allProducts.clear()
            allProducts.addAll(localItems)
            renderProducts()
        }
    }

    private fun loadOwnerProducts() {
        if (allProducts.isEmpty()) {
            pbLoading.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            SupabaseService.getProducts { jsonArray ->
                if (!isAdded) return@getProducts
                pbLoading.visibility = View.GONE

                val merged = mutableListOf<ProductItem>()
                val localItems = readLocalProducts()

                if (jsonArray != null) {
                    val loadedNames = mutableSetOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.opt("id")?.toString()?.takeIf { it != "null" && it.isNotBlank() }
                        val name = obj.optString("name", "Produk").trim()
                        val cat = obj.optString("category", "Herbal").trim().ifEmpty { "Herbal" }
                        val price = parsePrice(obj.opt("price")?.toString())
                        val stock = obj.optInt("stock", obj.optString("stock", "0").toIntOrNull() ?: 0)
                        val imageUri = getProductImageUri(name)

                        merged.add(ProductItem(id, name, cat, price, stock, imageUri, synced = true))
                        loadedNames.add(name)
                    }

                    for (local in localItems) {
                        if (!local.synced && !loadedNames.contains(local.name)) {
                            merged.add(local)
                            loadedNames.add(local.name)
                        }
                    }
                    persistLocalProducts(merged)
                } else {
                    merged.addAll(localItems)
                }

                allProducts.clear()
                allProducts.addAll(merged)
                renderProducts()
            }
        }
    }

    private fun renderProducts() {
        if (!isAdded) return
        containerOwnerProducts.removeAllViews()

        val filtered = if (currentQuery.isEmpty()) {
            allProducts
        } else {
            allProducts.filter {
                it.name.contains(currentQuery, ignoreCase = true) ||
                    it.category.contains(currentQuery, ignoreCase = true)
            }
        }

        tvProductCount.text = "${allProducts.size} Produk"
        val lowStockTotal = allProducts.count { it.stock < 5 }
        if (lowStockTotal > 0) {
            tvLowStockCount.visibility = View.VISIBLE
            tvLowStockCount.text = "$lowStockTotal Stok Menipis"
        } else {
            tvLowStockCount.visibility = View.GONE
        }

        if (filtered.isEmpty()) {
            layoutEmptyProducts.visibility = View.VISIBLE
            if (currentQuery.isNotEmpty()) {
                tvEmptyTitle.text = "Produk tidak ditemukan"
                tvEmptyDesc.text = "Tidak ada produk yang cocok dengan \"$currentQuery\"."
            } else {
                tvEmptyTitle.text = "Belum ada produk"
                tvEmptyDesc.text = "Ketuk '+ Tambah' untuk menambahkan produk ke toko."
            }
            return
        }

        layoutEmptyProducts.visibility = View.GONE
        val inflater = LayoutInflater.from(requireContext())

        for (item in filtered) {
            val card = inflater.inflate(R.layout.item_owner_product, containerOwnerProducts, false)
            val ivThumb: ImageView = card.findViewById(R.id.ivOwnerProductThumb)
            val tvName: TextView = card.findViewById(R.id.tvOwnerProductName)
            val tvCategory: TextView = card.findViewById(R.id.tvOwnerProductCategory)
            val tvPrice: TextView = card.findViewById(R.id.tvOwnerProductPrice)
            val tvStock: TextView = card.findViewById(R.id.tvOwnerProductStock)
            val btnEdit: View = card.findViewById(R.id.btnEditProduct)
            val btnDelete: View = card.findViewById(R.id.btnDeleteProduct)

            tvName.text = item.name
            tvCategory.text = item.category
            tvPrice.text = formatRupiah(item.price)

            when {
                item.stock <= 0 -> {
                    tvStock.text = "Stok Habis"
                    tvStock.setBackgroundResource(R.drawable.bg_badge_danger)
                    tvStock.setTextColor(ContextCompat.getColor(requireContext(), R.color.merah_tua))
                }
                item.stock < 5 -> {
                    tvStock.text = "Stok: ${item.stock} (Menipis)"
                    tvStock.setBackgroundResource(R.drawable.bg_badge_orange)
                    tvStock.setTextColor(ContextCompat.getColor(requireContext(), R.color.simaju_orange_text))
                }
                else -> {
                    tvStock.text = "Stok: ${item.stock}"
                    tvStock.setBackgroundResource(R.drawable.bg_badge_neutral)
                    tvStock.setTextColor(ContextCompat.getColor(requireContext(), R.color.simaju_text_primary))
                }
            }

            bindCardThumbnail(ivThumb, item.imageUri)

            btnEdit.setOnClickListener { showProductDialog(editingItem = item) }
            btnDelete.setOnClickListener { confirmDeleteProduct(item) }

            containerOwnerProducts.addView(card)
        }
    }

    private fun bindPreviewImage(imageView: ImageView, labelView: TextView, uri: Uri) {
        try {
            imageView.imageTintList = null
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setImageURI(uri)
            labelView.text = uri.lastPathSegment ?: "Gambar dipilih"
        } catch (_: Exception) {
            imageView.setImageResource(R.drawable.ic_image)
        }
    }

    private fun bindCardThumbnail(imageView: ImageView, uriString: String?) {
        if (!uriString.isNullOrBlank()) {
            try {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setImageURI(Uri.parse(uriString))
                if (imageView.drawable != null) return
            } catch (_: Exception) {
            }
        }
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setImageResource(R.drawable.images)
    }

    private fun parsePrice(raw: String?): Double {
        if (raw.isNullOrBlank()) return 0.0
        return raw.replace("Rp", "", ignoreCase = true).trim().toDoubleOrNull() ?: 0.0
    }

    private fun formatRupiah(price: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
        formatter.maximumFractionDigits = 0
        return "Rp ${formatter.format(price)}"
    }

    private fun formatEditableNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    private fun readLocalProducts(): MutableList<ProductItem> {
        val prefs = requireContext().getSharedPreferences("products_prefs", Context.MODE_PRIVATE)
        val rawJson = prefs.getString("local_products_json", "[]") ?: "[]"
        val result = mutableListOf<ProductItem>()
        try {
            val array = JSONArray(rawJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.optString("name", "").trim()
                if (name.isNotEmpty()) {
                    val img = obj.optString("imageUri", "").ifBlank { getProductImageUri(name) ?: "" }
                    result.add(
                        ProductItem(
                            id = obj.optString("id", "").takeIf { it.isNotBlank() },
                            name = name,
                            category = obj.optString("category", "Herbal"),
                            price = parsePrice(obj.optString("price", "0")),
                            stock = obj.optString("stock", "0").toDoubleOrNull()?.toInt() ?: 0,
                            imageUri = img.takeIf { it.isNotBlank() },
                            synced = obj.optBoolean("synced", false)
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        return result
    }

    private fun persistLocalProducts(items: List<ProductItem>) {
        val prefs = requireContext().getSharedPreferences("products_prefs", Context.MODE_PRIVATE)
        val array = JSONArray()
        for (item in items) {
            array.put(JSONObject().apply {
                put("id", item.id ?: "")
                put("name", item.name)
                put("category", item.category)
                put("price", formatEditableNumber(item.price))
                put("stock", item.stock.toString())
                put("imageUri", item.imageUri ?: "")
                put("synced", item.synced)
            })
        }
        prefs.edit().putString("local_products_json", array.toString()).apply()
    }

    private fun saveProductLocally(item: ProductItem) {
        val list = readLocalProducts()
        list.removeAll { it.name == item.name }
        list.add(0, item)
        persistLocalProducts(list)
    }

    private fun updateProductLocal(originalName: String, updated: ProductItem) {
        val list = readLocalProducts()
        val idx = list.indexOfFirst { it.name == originalName }
        if (idx >= 0) {
            list[idx] = updated
        } else {
            list.add(0, updated)
        }
        persistLocalProducts(list)
    }

    private fun deleteProductLocal(targetName: String) {
        val list = readLocalProducts()
        list.removeAll { it.name == targetName }
        persistLocalProducts(list)
    }

    private fun saveProductImageUri(productName: String, uri: String?) {
        val prefs = requireContext().getSharedPreferences("product_images_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            if (uri.isNullOrBlank()) remove(productName) else putString(productName, uri)
        }.apply()
    }

    private fun getProductImageUri(productName: String): String? {
        val prefs = requireContext().getSharedPreferences("product_images_prefs", Context.MODE_PRIVATE)
        return prefs.getString(productName, null)
    }
}
