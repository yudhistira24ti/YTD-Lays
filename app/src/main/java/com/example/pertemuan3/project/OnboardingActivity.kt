package com.example.pertemuan3.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.pertemuan3.R
import com.example.pertemuan3.splash.SplashActivity

class OnboardingActivity : AppCompatActivity() {

    private var step = 0

    private val titles = arrayOf(
        "Selamat Datang di SiMaju HPAI",
        "Manajemen Toko & Owner",
        "Lokasi Toko & Google Maps"
    )

    private val descriptions = arrayOf(
        "Aplikasi manajemen penjualan dan belanja produk herbal HPAI terpercaya, berkualitas, dan halal.",
        "Kelola produk, kategori, dan pesanan masuk secara real-time dengan dashboard khusus Owner.",
        "Temukan titik lokasi pusat distribusi dan toko HPAI terdekat langsung melalui integrasi Google Maps interaktif."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("seen_onboarding", false)) {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
            return
        }

        val tvTitle: TextView = findViewById(R.id.tvOnboardingTitle)
        val tvDesc: TextView = findViewById(R.id.tvOnboardingDesc)
        val btnNext: Button = findViewById(R.id.btnOnboardingNext)

        btnNext.setOnClickListener {
            step++
            if (step < titles.size) {
                tvTitle.text = titles[step]
                tvDesc.text = descriptions[step]
                if (step == titles.size - 1) {
                    btnNext.text = "Mulai Aplikasi"
                }
            } else {
                prefs.edit().putBoolean("seen_onboarding", true).apply()
                val intent = Intent(this, SplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
