package com.example.pertemuan3.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.pertemuan3.R

class ProfileTabFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_tab, container, false)

        val switchDarkMode: SwitchCompat = view.findViewById(R.id.switchCustomerDarkMode)
        val prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        switchDarkMode.isChecked = isDark

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        val btnLogout: Button = view.findViewById(R.id.btnLogoutAccount)
        btnLogout.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        return view
    }
}
