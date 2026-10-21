package com.example.pertemuan3.project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.pertemuan3.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        if (savedInstanceState == null) {
            loadFragment(HomeTabFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_beranda -> {
                    loadFragment(HomeTabFragment())
                    true
                }
                R.id.nav_katalog -> {
                    loadFragment(CatalogTabFragment())
                    true
                }
                R.id.nav_keranjang -> {
                    loadFragment(CartTabFragment())
                    true
                }
                R.id.nav_profil -> {
                    loadFragment(ProfileTabFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.dashboardFragmentContainer, fragment)
            .commit()
    }
}
