package com.example.pertemuan3.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException

object SupabaseService {
    // URL Supabase yang sudah diperbaiki sesuai project dashboard
    private const val SUPABASE_URL = "https://xwlrkphhpcrbyhqefnnm.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_FBBb1DNVaWBsTVLq9Q4MOQ_0OgWSYLU"

    private const val TAG = "SupabaseService"

    private fun openConnection(endpoint: String, method: String, hasBody: Boolean = false): HttpURLConnection {
        val url = URL(endpoint)
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("apikey", SUPABASE_KEY)
            // Header Authorization: Bearer hanya dipakai jika key berbentuk JWT (eyJ...)
            if (SUPABASE_KEY.startsWith("eyJ")) {
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
            }
            if (hasBody) {
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }
        }
    }

    private fun formatExceptionMessage(e: Exception): String {
        return when (e) {
            is UnknownHostException -> "Host Supabase tidak ditemukan (cek SUPABASE_URL)"
            else -> e.message ?: "Koneksi ke Supabase gagal"
        }
    }

    // 1. REGISTER: Insert ke tabel 'users' (dengan field role)
    suspend fun registerUser(
        name: String,
        email: String,
        pass: String,
        role: String = "customer",
        onResult: (Boolean, String) -> Unit
    ) {
        val userObj = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("password", pass)
            put("role", role)
        }
        val body = JSONArray().put(userObj).toString()
        executeMutation("POST", "$SUPABASE_URL/rest/v1/users", body) { success, error ->
            if (success) {
                onResult(true, "Registrasi berhasil ke Supabase!")
            } else {
                onResult(false, "Gagal: $error")
            }
        }
    }

    // 1B. LOGIN: Verifikasi user dari tabel 'users' berdasarkan email atau name/username
    suspend fun loginUser(identifier: String, pass: String, onResult: (Boolean, JSONObject?, String?) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val encoded = URLEncoder.encode(identifier, "UTF-8").replace("+", "%20")
                // Query: cari user di mana email == identifier ATAU name == identifier
                val endpoint = "$SUPABASE_URL/rest/v1/users?or=(email.eq.$encoded,name.eq.$encoded)&select=*"
                val conn = openConnection(endpoint, "GET")
                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val res = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val array = JSONArray(res)
                    var matchedUser: JSONObject? = null
                    for (i in 0 until array.length()) {
                        val user = array.getJSONObject(i)
                        val storedPass = user.optString("password", "")
                        if (storedPass == pass) {
                            matchedUser = user
                            break
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (matchedUser != null) {
                            onResult(true, matchedUser, null)
                        } else {
                            onResult(false, null, "Email/Username atau Kata Sandi salah")
                        }
                    }
                } else {
                    val err = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).use { r -> r.readText() } }
                    withContext(Dispatchers.Main) { onResult(false, null, err ?: "Gagal terhubung ke database") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResult(false, null, formatExceptionMessage(e)) }
            }
        }
    }

    // 2. PRODUCTS CRUD: Get Products
    suspend fun getProducts(onResult: (JSONArray?) -> Unit) {
        executeGet("$SUPABASE_URL/rest/v1/products?select=*", "get products", onResult)
    }

    // 2. PRODUCTS CRUD: Add Product
    suspend fun addProduct(name: String, price: Double, stock: Int, category: String, imageUrl: String, onResult: (Boolean, String?) -> Unit) {
        val body = JSONArray().put(JSONObject().apply {
            put("name", name)
            put("price", price)
            put("stock", stock)
            put("category", category)
        }).toString()
        executeMutation("POST", "$SUPABASE_URL/rest/v1/products", body, onResult)
    }

    // 2B. PRODUCTS CRUD: Update Product
    suspend fun updateProduct(
        id: String?,
        originalName: String,
        name: String,
        price: Double,
        stock: Int,
        category: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val filter = buildFilter("id", id, "name", originalName)
        val payload = JSONObject().apply {
            put("name", name)
            put("price", price)
            put("stock", stock)
            put("category", category)
        }.toString()
        executeMutation("PATCH", "$SUPABASE_URL/rest/v1/products?$filter", payload, onResult)
    }

    // 2C. PRODUCTS CRUD: Delete Product
    suspend fun deleteProduct(id: String?, name: String, onResult: (Boolean, String?) -> Unit) {
        val filter = buildFilter("id", id, "name", name)
        executeMutation("DELETE", "$SUPABASE_URL/rest/v1/products?$filter", null, onResult)
    }

    // 3. ORDERS CRUD: Get Orders
    suspend fun getOrders(onResult: (JSONArray?) -> Unit) {
        executeGet("$SUPABASE_URL/rest/v1/orders?select=*", "get orders", onResult)
    }

    // 3. ORDERS CRUD: Add Order
    suspend fun addOrder(email: String, itemName: String, totalPrice: Double, status: String, onResult: (Boolean) -> Unit) {
        val body = JSONArray().put(JSONObject().apply {
            put("customer_email", email)
            put("item_name", itemName)
            put("total_price", totalPrice)
            put("status", status)
        }).toString()
        executeMutation("POST", "$SUPABASE_URL/rest/v1/orders", body) { success, _ ->
            onResult(success)
        }
    }

    // 4. CATEGORIES CRUD: Get Categories
    suspend fun getCategories(onResult: (JSONArray?) -> Unit) {
        executeGet("$SUPABASE_URL/rest/v1/categories?select=*", "get categories", onResult)
    }

    // 4. CATEGORIES CRUD: Add Category
    suspend fun addCategory(name: String, description: String, onResult: (Boolean, String?) -> Unit) {
        val body = JSONArray().put(JSONObject().apply {
            put("name", name)
            put("description", description)
        }).toString()
        executeMutation("POST", "$SUPABASE_URL/rest/v1/categories", body, onResult)
    }

    // 4B. CATEGORIES CRUD: Update Category
    suspend fun updateCategory(
        id: String?,
        originalName: String,
        name: String,
        description: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val filter = buildFilter("id", id, "name", originalName)
        val payload = JSONObject().apply {
            put("name", name)
            put("description", description)
        }.toString()
        executeMutation("PATCH", "$SUPABASE_URL/rest/v1/categories?$filter", payload, onResult)
    }

    // 4C. CATEGORIES CRUD: Delete Category
    suspend fun deleteCategory(id: String?, name: String, onResult: (Boolean, String?) -> Unit) {
        val filter = buildFilter("id", id, "name", name)
        executeMutation("DELETE", "$SUPABASE_URL/rest/v1/categories?$filter", null, onResult)
    }

    private fun buildFilter(idKey: String, idVal: String?, fallbackKey: String, fallbackVal: String): String {
        val (key, value) = if (!idVal.isNullOrBlank()) idKey to idVal else fallbackKey to fallbackVal
        val encoded = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
        return "$key=eq.$encoded"
    }

    private suspend fun executeGet(endpoint: String, label: String, onResult: (JSONArray?) -> Unit) {
        withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                conn = openConnection(endpoint, "GET")
                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val jsonArray = JSONArray(response)
                    withContext(Dispatchers.Main) { onResult(jsonArray) }
                } else {
                    val err = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).use { r -> r.readText() } }
                    Log.e(TAG, "Error $label HTTP $responseCode: $err")
                    withContext(Dispatchers.Main) { onResult(null) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error $label", e)
                withContext(Dispatchers.Main) { onResult(null) }
            } finally {
                conn?.disconnect()
            }
        }
    }

    private suspend fun executeMutation(
        method: String,
        endpoint: String,
        jsonBody: String?,
        onResult: (Boolean, String?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            try {
                conn = openConnection(endpoint, method, hasBody = jsonBody != null).apply {
                    setRequestProperty("Prefer", "return=minimal")
                }

                if (jsonBody != null) {
                    conn.outputStream.use { os ->
                        os.write(jsonBody.toByteArray(Charsets.UTF_8))
                    }
                }

                val responseCode = conn.responseCode
                if (responseCode in 200..299) {
                    withContext(Dispatchers.Main) { onResult(true, null) }
                } else {
                    val errorMsg = conn.errorStream?.let {
                        BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() }
                    } ?: "HTTP $responseCode"
                    Log.e(TAG, "Error $method $endpoint: $errorMsg")
                    withContext(Dispatchers.Main) { onResult(false, errorMsg) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error $method $endpoint", e)
                withContext(Dispatchers.Main) { onResult(false, formatExceptionMessage(e)) }
            } finally {
                conn?.disconnect()
            }
        }
    }
}
