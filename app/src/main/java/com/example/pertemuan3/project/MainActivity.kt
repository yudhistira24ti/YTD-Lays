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
import com.example.pertemuan3.R

class MainActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tabCustomer: TextView = findViewById(R.id.tabCustomer)
        val tabOwner: TextView = findViewById(R.id.tabOwner)
        val etEmail: EditText = findViewById(R.id.etEmail)   // kolom ini dipakai sebagai username
        val etPassword: EditText = findViewById(R.id.etPassword)
        val ivTogglePassword: ImageView = findViewById(R.id.ivTogglePassword)
        val tvForgotPassword: TextView = findViewById(R.id.tvForgotPassword)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val tvRegister: TextView = findViewById(R.id.tvRegister)

        // Tab switching visual simulation
        tabCustomer.setOnClickListener {
            tabCustomer.setBackgroundResource(R.drawable.bg_tab_selected)
            tabCustomer.setTextColor(resources.getColor(R.color.simaju_green))
            tabOwner.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabOwner.setTextColor(resources.getColor(R.color.simaju_tab_unselected_text))
        }

        tabOwner.setOnClickListener {
            tabOwner.setBackgroundResource(R.drawable.bg_tab_selected)
            tabOwner.setTextColor(resources.getColor(R.color.simaju_green))
            tabCustomer.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabCustomer.setTextColor(resources.getColor(R.color.simaju_tab_unselected_text))
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

        // Login Button (2 rule)
        btnLogin.setOnClickListener {
            val username = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan Kata Sandi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                val pref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                val spUsername = pref.getString("username", null)
                val spPassword = pref.getString("password", null)

                // Rule 1: username = password
                val rule1 = username == password
                // Rule 2: cocok dengan username & password di SharedPreferences
                val rule2 = spUsername != null && username == spUsername && password == spPassword

                if (rule1 || rule2) {
                    Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Username atau password salah", Toast.LENGTH_SHORT).show()
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
}