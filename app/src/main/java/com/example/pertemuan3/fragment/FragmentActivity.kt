package com.example.pertemuan3.fragment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.pertemuan3.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class FragmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        val toolbar: Toolbar = findViewById(R.id.toolbarFragment)
        setSupportActionBar(toolbar)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), "Home")
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment(), "Home")
                    true
                }
                R.id.nav_message -> {
                    loadFragment(MessageFragment(), "Message")
                    true
                }
                R.id.nav_more -> {
                    loadFragment(MoreFragment(), "More")
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        supportActionBar?.title = title
    }
}
