package com.example.pertemuan3.owner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        val btnOpenMap: Button = view.findViewById(R.id.btnOpenMapOwner)
        val btnLogout: Button = view.findViewById(R.id.btnOwnerLogout)

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
