package com.example.pertemuan3.project

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.pertemuan3.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val toolbar: Toolbar = findViewById(R.id.toolbarRegister)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Daftar Akun SiMaju"

        val etName: EditText = findViewById(R.id.etName)
        val etEmail: EditText = findViewById(R.id.etRegEmail)
        val etPassword: EditText = findViewById(R.id.etRegPassword)
        val btnRegisterSubmit: Button = findViewById(R.id.btnRegisterSubmit)

        btnRegisterSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Pendaftaran Berhasil! Silakan Masuk.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
