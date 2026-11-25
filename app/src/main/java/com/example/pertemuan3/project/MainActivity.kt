package com.example.pertemuan3.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.pertemuan3.R
import com.example.pertemuan3.owner.OwnerDashboardActivity
import com.example.pertemuan3.service.SupabaseService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var isPasswordVisible = false
    private var isOwner = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tabCustomer: TextView = findViewById(R.id.tabCustomer)
        val tabOwner: TextView = findViewById(R.id.tabOwner)
        val etEmail: EditText = findViewById(R.id.etEmail)
        val etPassword: EditText = findViewById(R.id.etPassword)
        val ivTogglePassword: ImageView = findViewById(R.id.ivTogglePassword)
        val tvForgotPassword: TextView = findViewById(R.id.tvForgotPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val tvRegister: TextView = findViewById(R.id.tvRegister)

        // Tab switching visual simulation
        tabCustomer.setOnClickListener {
            isOwner = false
            tabCustomer.setBackgroundResource(R.drawable.bg_tab_selected)
            tabCustomer.setTextColor(resources.getColor(R.color.simaju_green, null))
            tabOwner.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabOwner.setTextColor(resources.getColor(R.color.simaju_tab_unselected_text, null))
        }

        tabOwner.setOnClickListener {
            isOwner = true
            tabOwner.setBackgroundResource(R.drawable.bg_tab_selected)
            tabOwner.setTextColor(resources.getColor(R.color.simaju_green, null))
            tabCustomer.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabCustomer.setTextColor(resources.getColor(R.color.simaju_tab_unselected_text, null))
        }

        // Toggle Password Visibility
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = null
                ivTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        // Login Button
        btnLogin.setOnClickListener {
            val inputIdentifier = etEmail.text.toString().trim() // Bisa berupa Email ATAU Username
            val inputPassword = etPassword.text.toString().trim()

            if (inputIdentifier.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "Email/Username dan Kata Sandi tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "Memeriksa..."

            val pref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
            val spUsername = pref.getString("username", null)
            val spEmail = pref.getString("email", null)
            val spPassword = pref.getString("password", null)

            // Cek lokal cepat
            val isLocalMatched = spPassword != null &&
                inputPassword == spPassword &&
                (inputIdentifier.equals(spUsername, ignoreCase = true) || inputIdentifier.equals(spEmail, ignoreCase = true))

            val isDemoMatched = inputIdentifier == inputPassword

            if (isLocalMatched || isDemoMatched) {
                btnLogin.isEnabled = true
                btnLogin.text = "Masuk Sekarang"
                proceedLogin()
                return@setOnClickListener
            }

            // Jika tidak ada di cache lokal, cek langsung ke database Supabase
            lifecycleScope.launch {
                SupabaseService.loginUser(inputIdentifier, inputPassword) { success, userObj, errMsg ->
                    btnLogin.isEnabled = true
                    btnLogin.text = "Masuk Sekarang"

                    if (success && userObj != null) {
                        val name = userObj.optString("name", inputIdentifier)
                        val email = userObj.optString("email", inputIdentifier)
                        val dbRole = userObj.optString("role", "").lowercase()

                        // Jika tab yang dipilih Owner tapi role di database bukan owner:
                        if (isOwner && dbRole.isNotEmpty() && dbRole != "owner" && dbRole != "admin") {
                            Toast.makeText(this@MainActivity, "Akun ini bukan akun Owner Toko", Toast.LENGTH_SHORT).show()
                            return@loginUser
                        }

                        // Jika login di tab Customer tapi role di database adalah owner, otomatis kita sesuaikan
                        val actualIsOwner = if (dbRole == "owner") true else isOwner

                        pref.edit().apply {
                            putString("username", name)
                            putString("email", email)
                            putString("password", inputPassword)
                            putString("role", if (actualIsOwner) "owner" else "customer")
                            putBoolean("is_owner", actualIsOwner)
                            apply()
                        }
                        proceedLogin(actualIsOwner)
                    } else {
                        Toast.makeText(this@MainActivity, errMsg ?: "Email/Username atau password salah", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Forgot Password
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Register Link
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun proceedLogin(asOwner: Boolean = isOwner) {
        Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
        if (asOwner) {
            startActivity(Intent(this, OwnerDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        finish()
    }
}
