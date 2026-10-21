package com.example.pertemuan3.tugas_per3

import com.example.pertemuan3.R
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val btnMyProject: Button = findViewById(R.id.btnMyProject)
        btnMyProject.setOnClickListener {
            startActivity(Intent(this, DetailActivity::class.java))
        }
    }
}