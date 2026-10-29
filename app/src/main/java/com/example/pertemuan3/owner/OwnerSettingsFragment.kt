package com.example.pertemuan3.owner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.pertemuan3.R
import com.example.pertemuan3.project.MainActivity
import com.example.pertemuan3.project.StoreMapActivity

class OwnerSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_owner_settings, container, false)

        val switchDarkMode: SwitchCompat = view.findViewById(R.id.switchDarkMode)
        val etStoreName: EditText = view.findViewById(R.id.etStoreName)
        val etOpenTime: EditText = view.findViewById(R.id.etOpenTime)
        val etCloseTime: EditText = view.findViewById(R.id.etCloseTime)
        val btnSaveStoreSettings: Button = view.findViewById(R.id.btnSaveStoreSettings)
        val btnOpenMap: Button = view.findViewById(R.id.btnOpenMapOwner)
        val btnLogout: Button = view.findViewById(R.id.btnOwnerLogout)

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

        val storePrefs = requireContext().getSharedPreferences("store_location", Context.MODE_PRIVATE)
        etStoreName.setText(storePrefs.getString("store_name", "Toko herbal HPAI"))
        etOpenTime.setText(storePrefs.getString("open_time", "08:00"))
        etCloseTime.setText(storePrefs.getString("close_time", "20:00"))

        btnSaveStoreSettings.setOnClickListener {
            val name = etStoreName.text.toString().trim()
            val open = etOpenTime.text.toString().trim()
            val close = etCloseTime.text.toString().trim()

            storePrefs.edit().apply {
                putString("store_name", name)
                putString("open_time", open)
                putString("close_time", close)
                apply()
            }
            Toast.makeText(requireContext(), "Pengaturan toko berhasil disimpan!", Toast.LENGTH_SHORT).show()
        }

        btnOpenMap.setOnClickListener {
            val intent = Intent(requireContext(), StoreMapActivity::class.java).apply {
                putExtra("is_owner_mode", true)
            }
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }

        return view
    }
}
