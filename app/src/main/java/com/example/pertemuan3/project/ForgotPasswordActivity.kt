package com.example.pertemuan3.project

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.pertemuan3.R

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val toolbar: Toolbar = findViewById(R.id.toolbarForgot)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Lupa Kata Sandi"

        val etForgotEmail: EditText = findViewById(R.id.etForgotEmail)
        val btnResetSubmit: Button = findViewById(R.id.btnResetSubmit)

        btnResetSubmit.setOnClickListener {
            val email = etForgotEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Masukkan alamat email Anda", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Tautan reset kata sandi telah dikirim ke $email", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
