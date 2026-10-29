package com.example.pertemuan3.owner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.pertemuan3.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class OwnerDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_owner_dashboard)

        val bottomNav: BottomNavigationView = findViewById(R.id.ownerBottomNav)

        if (savedInstanceState == null) {
            loadFragment(OwnerDashboardFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.owner_nav_dashboard -> {
                    loadFragment(OwnerDashboardFragment())
                    true
                }
                R.id.owner_nav_produk -> {
                    loadFragment(OwnerProductsFragment())
                    true
                }
                R.id.owner_nav_kategori -> {
                    loadFragment(OwnerCategoriesFragment())
                    true
                }
                R.id.owner_nav_pesanan -> {
                    loadFragment(OwnerOrdersFragment())
                    true
                }
                R.id.owner_nav_settings -> {
                    loadFragment(OwnerSettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.ownerFragmentContainer, fragment)
            .commit()
    }
}
