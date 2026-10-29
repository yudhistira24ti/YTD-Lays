package com.example.pertemuan3.project

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
import com.example.pertemuan3.owner.OwnerDashboardActivity

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
            tabCustomer.setTextColor(resources.getColor(R.color.simaju_green))
            tabOwner.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabOwner.setTextColor(resources.getColor(R.color.simaju_tab_unselected_text))
        }

        tabOwner.setOnClickListener {
            isOwner = true
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

        // Login Button
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Kata Sandi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                if (isOwner) {
                    val intent = Intent(this, OwnerDashboardActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                }
                finish()
            }
        }

        // Forgot Password
        tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        // Register Link
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
