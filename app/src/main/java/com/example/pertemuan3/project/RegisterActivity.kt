package com.example.pertemuan3.project

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.pertemuan3.R
import java.util.Calendar

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
        val etTanggalLahir: EditText = findViewById(R.id.etTanggalLahir)
        val rgGender: RadioGroup = findViewById(R.id.rgGender)
        val etUsername: EditText = findViewById(R.id.etUsername)
        val etPassword: EditText = findViewById(R.id.etRegPassword)
        val etConfirmPassword: EditText = findViewById(R.id.etConfirmPassword)
        val btnRegisterSubmit: Button = findViewById(R.id.btnRegisterSubmit)

        // DatePicker untuk tanggal lahir
        etTanggalLahir.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    etTanggalLahir.setText("%02d/%02d/%d".format(day, month + 1, year))
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnRegisterSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val tglLahir = etTanggalLahir.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            val gender = when (rgGender.checkedRadioButtonId) {
                R.id.rbLaki -> "Laki-laki"
                R.id.rbPerempuan -> "Perempuan"
                else -> ""
            }

            if (name.isEmpty() || email.isEmpty() || tglLahir.isEmpty() ||
                gender.isEmpty() || username.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()
            ) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                etConfirmPassword.error = "Password tidak sama"
            } else {
                // Simpan ke SharedPreferences
                val pref = getSharedPreferences("user_data", Context.MODE_PRIVATE)
                pref.edit().apply {
                    putString("nama", name)
                    putString("email", email)
                    putString("tanggal_lahir", tglLahir)
                    putString("gender", gender)
                    putString("username", username)
                    putString("password", password)
                    putString("confirm_password", confirmPassword)
                    apply()
                }


                startActivity(Intent(this, ValidasiActivity::class.java))
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}