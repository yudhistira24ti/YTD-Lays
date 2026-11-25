package com.example.pertemuan3.project

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.example.pertemuan3.R
import com.example.pertemuan3.service.SupabaseService
import kotlinx.coroutines.launch
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    // Registrasi publik dikhususkan hanya untuk pelanggan (customer)
    private val selectedRole: String = "customer"

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val headerBar: View = findViewById(R.id.registerHeaderBar)
        val scrollContent: View = findViewById(R.id.scrollRegisterContent)
        val btnBack: ImageView = findViewById(R.id.btnRegisterBack)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerRootLayout)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerBar.updatePadding(top = systemBars.top)
            scrollContent.updatePadding(bottom = systemBars.bottom)
            insets
        }

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

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
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                etConfirmPassword.error = "Password tidak sama"
                etConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            btnRegisterSubmit.isEnabled = false
            btnRegisterSubmit.text = "Mendaftarkan..."

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
                putString("role", selectedRole)
                putBoolean("is_owner", false)
                apply()
            }

            // Kirim ke Supabase tabel 'users'
            lifecycleScope.launch {
                SupabaseService.registerUser(name, email, password, selectedRole) { success, msg ->
                    btnRegisterSubmit.isEnabled = true
                    btnRegisterSubmit.text = "Daftar Sekarang"
                    Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@RegisterActivity, ValidasiActivity::class.java).apply {
                        putExtra("registered_role", selectedRole)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
