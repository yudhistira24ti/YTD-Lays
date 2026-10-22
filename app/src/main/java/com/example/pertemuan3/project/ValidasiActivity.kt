package com.example.pertemuan3.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pertemuan3.R

class ValidasiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_validasi)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvData: TextView = findViewById(R.id.tvData)
        val tvError: TextView = findViewById(R.id.tvError)
        val btnSubmit: Button = findViewById(R.id.btnSubmit)
        val btnKembali: Button = findViewById(R.id.btnKembali)

        val pref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val nama = pref.getString("nama", "") ?: ""
        val email = pref.getString("email", "") ?: ""
        val tglLahir = pref.getString("tanggal_lahir", "") ?: ""
        val gender = pref.getString("gender", "") ?: ""
        val username = pref.getString("username", "") ?: ""
        val password = pref.getString("password", "") ?: ""
        val confirm = pref.getString("confirm_password", "") ?: ""

        // Tampilkan seluruh isian dari SharedPreferences
        tvData.text = """
            Nama: $nama
            Email: $email
            Tanggal Lahir: $tglLahir
            Jenis Kelamin: $gender
            Username: $username
            Password: $password
            Confirm Password: $confirm
        """.trimIndent()

        btnSubmit.setOnClickListener {
            val errors = mutableListOf<String>()

            if (nama.isEmpty() || email.isEmpty() || tglLahir.isEmpty() ||
                gender.isEmpty() || username.isEmpty() ||
                password.isEmpty() || confirm.isEmpty()
            ) {
                errors.add("Semua inputan tidak boleh kosong")
            }
            if (password != confirm) {
                errors.add("Password dan Confirm Password harus sama")
            }

            if (errors.isEmpty()) {
                startActivity(Intent(this, RegistrasiBerhasilActivity::class.java))
                finish()
            } else {
                tvError.text = errors.joinToString("\n") { "• $it" }
                tvError.visibility = View.VISIBLE
                btnKembali.visibility = View.VISIBLE
            }
        }

        // Kembali ke halaman registrasi untuk memperbaiki isian
        btnKembali.setOnClickListener { finish() }
    }
}