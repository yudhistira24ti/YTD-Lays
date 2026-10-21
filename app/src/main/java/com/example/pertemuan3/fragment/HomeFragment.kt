package com.example.pertemuan3.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.pertemuan3.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val btn2: Button = view.findViewById(R.id.btnPertemuan2)
        val btn3: Button = view.findViewById(R.id.btnPertemuan3)
        val btn4: Button = view.findViewById(R.id.btnPertemuan4)
        val btn5: Button = view.findViewById(R.id.btnPertemuan5)
        val btn6: Button = view.findViewById(R.id.btnPertemuan6)

        btn2.setOnClickListener { Toast.makeText(requireContext(), "Pertemuan 2 diklik", Toast.LENGTH_SHORT).show() }
        btn3.setOnClickListener { Toast.makeText(requireContext(), "Pertemuan 3 diklik", Toast.LENGTH_SHORT).show() }
        btn4.setOnClickListener { Toast.makeText(requireContext(), "Pertemuan 4 diklik", Toast.LENGTH_SHORT).show() }
        btn5.setOnClickListener { Toast.makeText(requireContext(), "Pertemuan 5 diklik", Toast.LENGTH_SHORT).show() }
        btn6.setOnClickListener { Toast.makeText(requireContext(), "Pertemuan 6 diklik", Toast.LENGTH_SHORT).show() }

        return view
    }
}
