package com.example.pertemuan3.owner

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pertemuan3.R
import com.example.pertemuan3.service.SupabaseService
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class OwnerCategoriesFragment : Fragment() {

    private data class CategoryItem(
        val id: String?,
        val fullName: String,
        val description: String,
        val synced: Boolean = true
    )

    private lateinit var containerCategories: LinearLayout
    private lateinit var layoutEmptyCategories: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptyDesc: TextView
    private lateinit var tvCategoryCount: TextView
    private lateinit var pbLoading: ProgressBar
    private lateinit var etSearchCategory: EditText
    private lateinit var ivClearSearch: ImageView

    private val allCategories = mutableListOf<CategoryItem>()
    private var currentQuery: String = ""

    private val availableEmojis = arrayOf(
        "☕", "🍵", "🥛", "💊", "🧪", "🌿", "🍃", "🍯", "🌴", "🫒",
        "🧴", "🧼", "🪥", "✨", "💄", "🌸", "🛍️", "📦", "🏥", "🕋"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_owner_categories, container, false)

        containerCategories = view.findViewById(R.id.containerOwnerCategories)
        layoutEmptyCategories = view.findViewById(R.id.layoutEmptyCategories)
        tvEmptyTitle = view.findViewById(R.id.tvEmptyCategoriesTitle)
        tvEmptyDesc = view.findViewById(R.id.tvEmptyCategoriesDesc)
        tvCategoryCount = view.findViewById(R.id.tvCategoryCount)
        pbLoading = view.findViewById(R.id.pbCategoriesLoading)
        etSearchCategory = view.findViewById(R.id.etSearchCategory)
        ivClearSearch = view.findViewById(R.id.ivClearSearchCategory)
        val btnAddCategory: Button = view.findViewById(R.id.btnAddCategory)

        btnAddCategory.setOnClickListener {
            showCategoryDialog(editingItem = null)
        }

        etSearchCategory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.trim() ?: ""
                ivClearSearch.visibility = if (currentQuery.isEmpty()) View.GONE else View.VISIBLE
                renderCategories()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        ivClearSearch.setOnClickListener {
            etSearchCategory.text.clear()
        }

        renderLocalCategoriesFirst()
        loadCategoriesFromSupabase()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadCategoriesFromSupabase()
    }

    private fun showCategoryDialog(editingItem: CategoryItem?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val tvTitle: TextView = dialogView.findViewById(R.id.tvDialogCategoryTitle)
        val btnClose: ImageView = dialogView.findViewById(R.id.btnDialogCategoryClose)
        val containerEmojiIcons: LinearLayout = dialogView.findViewById(R.id.containerEmojiIcons)
        val etName: EditText = dialogView.findViewById(R.id.etDialogCategoryName)
        val etDesc: EditText = dialogView.findViewById(R.id.etDialogCategoryDesc)
        val btnCancel: Button = dialogView.findViewById(R.id.btnDialogCancel)
        val btnSubmit: Button = dialogView.findViewById(R.id.btnDialogSubmit)

        val isEdit = editingItem != null
        val (initialIcon, initialCleanName) = if (editingItem != null) {
            splitIconAndName(editingItem.fullName)
        } else {
            "🌿" to ""
        }

        var selectedIcon = if (availableEmojis.contains(initialIcon)) initialIcon else "🌿"

        if (isEdit) {
            tvTitle.text = "Edit Kategori"
            btnSubmit.text = "Simpan"
            etName.setText(initialCleanName)
            etName.setSelection(etName.text.length)
            etDesc.setText(editingItem?.description.orEmpty())
        } else {
            tvTitle.text = "Tambah Kategori Baru"
            btnSubmit.text = "Tambah"
        }

        val emojiSize = dpToPx(44)
        val emojiMargin = dpToPx(8)
        val emojiViews = mutableListOf<TextView>()

        for (emoji in availableEmojis) {
            val tvEmoji = TextView(requireContext()).apply {
                text = emoji
                textSize = 20f
                gravity = Gravity.CENTER
                setBackgroundResource(if (emoji == selectedIcon) R.drawable.bg_emoji_selected else R.drawable.bg_badge_neutral)
                layoutParams = LinearLayout.LayoutParams(emojiSize, emojiSize).apply {
                    marginEnd = emojiMargin
                }
                setOnClickListener {
                    selectedIcon = emoji
                    emojiViews.forEach { v ->
                        v.setBackgroundResource(if (v.text == selectedIcon) R.drawable.bg_emoji_selected else R.drawable.bg_badge_neutral)
                    }
                }
            }
            emojiViews.add(tvEmoji)
            containerEmojiIcons.addView(tvEmoji)
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val nameRaw = etName.text.toString().trim()
            val descInput = etDesc.text.toString().trim()
            val desc = if (descInput.isEmpty()) "Kategori Produk HPAI" else descInput

            if (nameRaw.isEmpty()) {
                etName.error = "Nama kategori wajib diisi"
                etName.requestFocus()
                return@setOnClickListener
            }

            val nameWithIcon = "$selectedIcon $nameRaw"
            btnSubmit.isEnabled = false
            btnSubmit.text = "Menyimpan..."

            if (editingItem != null) {
                lifecycleScope.launch {
                    SupabaseService.updateCategory(
                        id = editingItem.id,
                        originalName = editingItem.fullName,
                        name = nameWithIcon,
                        description = desc
                    ) { success, error ->
                        if (!isAdded) return@updateCategory
                        updateCategoryLocal(
                            originalName = editingItem.fullName,
                            updated = CategoryItem(editingItem.id, nameWithIcon, desc, synced = success)
                        )
                        if (success) {
                            Toast.makeText(requireContext(), "Kategori '$nameWithIcon' berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("Categories", "Gagal update ke Supabase: $error")
                            Toast.makeText(requireContext(), "Diperbarui secara lokal (Supabase: ${error ?: "offline"})", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                        loadCategoriesFromSupabase()
                    }
                }
            } else {
                lifecycleScope.launch {
                    SupabaseService.addCategory(nameWithIcon, desc) { success, error ->
                        if (!isAdded) return@addCategory
                        saveCategoryLocal(CategoryItem(null, nameWithIcon, desc, synced = success))
                        if (success) {
                            Toast.makeText(requireContext(), "Kategori '$nameWithIcon' berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("Categories", "Gagal simpan ke Supabase: $error")
                            Toast.makeText(requireContext(), "Tersimpan lokal (Supabase: ${error ?: "offline"})", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                        loadCategoriesFromSupabase()
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

    private fun confirmDeleteCategory(item: CategoryItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kategori")
            .setMessage("Yakin ingin menghapus kategori '${item.fullName}'?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    SupabaseService.deleteCategory(item.id, item.fullName) { success, error ->
                        if (!isAdded) return@deleteCategory
                        deleteCategoryLocal(item.fullName)
                        if (success) {
                            Toast.makeText(requireContext(), "Kategori '${item.fullName}' dihapus", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("Categories", "Gagal hapus di Supabase: $error")
                            Toast.makeText(requireContext(), "Dihapus lokal (Supabase: ${error ?: "offline"})", Toast.LENGTH_SHORT).show()
                        }
                        loadCategoriesFromSupabase()
                    }
                }
            }
            .show()
    }

    private fun renderLocalCategoriesFirst() {
        val localItems = readLocalDetailCategories()
        if (localItems.isNotEmpty()) {
            allCategories.clear()
            allCategories.addAll(localItems)
            renderCategories()
        }
    }

    private fun loadCategoriesFromSupabase() {
        if (allCategories.isEmpty()) {
            pbLoading.visibility = View.VISIBLE
        }

        lifecycleScope.launch {
            SupabaseService.getCategories { jsonArray ->
                if (!isAdded) return@getCategories
                pbLoading.visibility = View.GONE

                val merged = mutableListOf<CategoryItem>()
                val localDetails = readLocalDetailCategories()

                if (jsonArray != null) {
                    val seenNames = mutableSetOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val id = obj.opt("id")?.toString()?.takeIf { it != "null" && it.isNotBlank() }
                        val name = obj.optString("name", "").trim()
                        val desc = obj.optString("description", "").trim().ifEmpty { "Kategori Produk HPAI" }
                        if (name.isNotEmpty()) {
                            merged.add(CategoryItem(id, name, desc, synced = true))
                            seenNames.add(name)
                        }
                    }
                    // Keep unsynced local-only categories (added while offline / RLS blocked)
                    for (local in localDetails) {
                        if (!local.synced && !seenNames.contains(local.fullName)) {
                            merged.add(local)
                            seenNames.add(local.fullName)
                        }
                    }
                    persistLocalCategories(merged)
                } else {
                    merged.addAll(localDetails)
                }

                allCategories.clear()
                allCategories.addAll(merged)
                renderCategories()
            }
        }
    }

    private fun renderCategories() {
        if (!isAdded) return
        containerCategories.removeAllViews()

        val filtered = if (currentQuery.isEmpty()) {
            allCategories
        } else {
            allCategories.filter {
                it.fullName.contains(currentQuery, ignoreCase = true) ||
                    it.description.contains(currentQuery, ignoreCase = true)
            }
        }

        tvCategoryCount.text = "${allCategories.size} Kategori"

        if (filtered.isEmpty()) {
            layoutEmptyCategories.visibility = View.VISIBLE
            if (currentQuery.isNotEmpty()) {
                tvEmptyTitle.text = "Kategori tidak ditemukan"
                tvEmptyDesc.text = "Tidak ada kategori yang cocok dengan \"$currentQuery\"."
            } else {
                tvEmptyTitle.text = "Belum ada kategori"
                tvEmptyDesc.text = "Ketuk '+ Tambah' untuk membuat kategori produk baru."
            }
            return
        }

        layoutEmptyCategories.visibility = View.GONE
        val inflater = LayoutInflater.from(requireContext())

        for (item in filtered) {
            val card = inflater.inflate(R.layout.item_owner_category, containerCategories, false)
            val tvIcon: TextView = card.findViewById(R.id.tvCategoryIcon)
            val tvName: TextView = card.findViewById(R.id.tvCategoryName)
            val tvDesc: TextView = card.findViewById(R.id.tvCategoryDesc)
            val btnEdit: View = card.findViewById(R.id.btnEditCategory)
            val btnDelete: View = card.findViewById(R.id.btnDeleteCategory)

            val (icon, cleanName) = splitIconAndName(item.fullName)
            tvIcon.text = icon
            tvName.text = cleanName
            tvDesc.text = item.description.ifEmpty { "Kategori Produk HPAI" }

            btnEdit.setOnClickListener { showCategoryDialog(editingItem = item) }
            btnDelete.setOnClickListener { confirmDeleteCategory(item) }

            containerCategories.addView(card)
        }
    }

    private fun splitIconAndName(fullName: String): Pair<String, String> {
        val trimmed = fullName.trim()
        val spaceIdx = trimmed.indexOf(' ')
        if (spaceIdx > 0) {
            val prefix = trimmed.substring(0, spaceIdx)
            val rest = trimmed.substring(spaceIdx + 1).trim()
            val hasNonAscii = prefix.any { it.code > 127 }
            if (hasNonAscii && rest.isNotEmpty()) {
                return prefix to rest
            }
        }
        return "🏷️" to trimmed
    }

    private fun readLocalDetailCategories(): MutableList<CategoryItem> {
        val prefs = requireContext().getSharedPreferences("categories_prefs", Context.MODE_PRIVATE)
        val rawJson = prefs.getString("categories_detail_json", null)
        val result = mutableListOf<CategoryItem>()

        if (!rawJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(rawJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val name = obj.optString("name", "").trim()
                    if (name.isNotEmpty()) {
                        result.add(
                            CategoryItem(
                                id = obj.optString("id", "").takeIf { it.isNotBlank() },
                                fullName = name,
                                description = obj.optString("description", "Kategori Produk HPAI"),
                                synced = obj.optBoolean("synced", true)
                            )
                        )
                    }
                }
                return result
            } catch (_: Exception) {
            }
        }

        val legacySet = prefs.getStringSet("categories_list", emptySet()) ?: emptySet()
        for (name in legacySet) {
            result.add(CategoryItem(null, name, "Kategori Produk HPAI", synced = false))
        }
        return result
    }

    private fun persistLocalCategories(items: List<CategoryItem>) {
        val prefs = requireContext().getSharedPreferences("categories_prefs", Context.MODE_PRIVATE)
        val nameSet = LinkedHashSet<String>()
        val jsonArray = JSONArray()
        for (item in items) {
            nameSet.add(item.fullName)
            jsonArray.put(JSONObject().apply {
                put("id", item.id ?: "")
                put("name", item.fullName)
                put("description", item.description)
                put("synced", item.synced)
            })
        }
        prefs.edit()
            .putStringSet("categories_list", nameSet)
            .putString("categories_detail_json", jsonArray.toString())
            .apply()
    }

    private fun saveCategoryLocal(newItem: CategoryItem) {
        val list = readLocalDetailCategories()
        list.removeAll { it.fullName == newItem.fullName }
        list.add(0, newItem)
        persistLocalCategories(list)
    }

    private fun updateCategoryLocal(originalName: String, updated: CategoryItem) {
        val list = readLocalDetailCategories()
        val idx = list.indexOfFirst { it.fullName == originalName }
        if (idx >= 0) {
            list[idx] = updated
        } else {
            list.add(0, updated)
        }
        persistLocalCategories(list)
    }

    private fun deleteCategoryLocal(targetName: String) {
        val list = readLocalDetailCategories()
        list.removeAll { it.fullName == targetName }
        persistLocalCategories(list)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
