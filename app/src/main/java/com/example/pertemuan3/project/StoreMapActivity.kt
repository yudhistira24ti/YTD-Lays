package com.example.pertemuan3.project

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.pertemuan3.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class StoreMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var isOwnerMode = false
    private var selectedLatLng: LatLng? = null

    // Default store location: Yogyakarta / Jln. Dumai (-7.7956, 110.3695)
    private var defaultLat = -7.7956
    private var defaultLng = 110.3695

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_map)

        isOwnerMode = intent.getBooleanExtra("is_owner_mode", false)

        val toolbar: Toolbar = findViewById(R.id.toolbarMap)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isOwnerMode) "Atur Lokasi Toko" else "Lokasi Toko HPAI"

        val tvInstruction: TextView = findViewById(R.id.tvMapInstruction)
        val btnSave: Button = findViewById(R.id.btnSaveLocation)

        if (isOwnerMode) {
            tvInstruction.visibility = View.VISIBLE
            btnSave.visibility = View.VISIBLE
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnSave.setOnClickListener {
            selectedLatLng?.let { latLng ->
                val prefs = getSharedPreferences("store_location", MODE_PRIVATE)
                prefs.edit().apply {
                    putFloat("store_lat", latLng.latitude.toFloat())
                    putFloat("store_lng", latLng.longitude.toFloat())
                    apply()
                }
                Toast.makeText(this, "Lokasi toko berhasil disimpan!", Toast.LENGTH_SHORT).show()
                finish()
            } ?: Toast.makeText(this, "Pilih lokasi terlebih dahulu pada peta", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val prefs = getSharedPreferences("store_location", MODE_PRIVATE)
        val lat = prefs.getFloat("store_lat", defaultLat.toFloat()).toDouble()
        val lng = prefs.getFloat("store_lng", defaultLng.toFloat()).toDouble()
        val storeName = prefs.getString("store_name", "Toko Herbal HPAI") ?: "Toko Herbal HPAI"

        val storeLocation = LatLng(lat, lng)
        selectedLatLng = storeLocation

        mMap.addMarker(MarkerOptions().position(storeLocation).title(storeName))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 15f))

        if (isOwnerMode) {
            mMap.setOnMapClickListener { latLng ->
                mMap.clear()
                mMap.addMarker(MarkerOptions().position(latLng).title(storeName))
                selectedLatLng = latLng
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
