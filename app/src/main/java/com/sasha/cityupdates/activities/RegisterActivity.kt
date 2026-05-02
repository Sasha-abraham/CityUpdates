package com.sasha.cityupdates.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sasha.cityupdates.PreferenceManager
import com.sasha.cityupdates.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var spinnerArea: Spinner

    private val areas = listOf(
        "Select your area",
        "Koramangala", "Indiranagar", "Whitefield",
        "Jayanagar", "Malleshwaram", "HSR Layout",
        "Electronic City", "Hebbal", "Rajajinagar"
    )

    // Approximate coordinates for each area in Bengaluru
    private val areaCoordinates = mapOf(
        "Koramangala" to Pair(12.9352, 77.6245),
        "Indiranagar" to Pair(12.9784, 77.6408),
        "Whitefield" to Pair(12.9698, 77.7500),
        "Jayanagar" to Pair(12.9250, 77.5938),
        "Malleshwaram" to Pair(13.0035, 77.5710),
        "HSR Layout" to Pair(12.9116, 77.6474),
        "Electronic City" to Pair(12.8399, 77.6770),
        "Hebbal" to Pair(13.0450, 77.5950),
        "Rajajinagar" to Pair(12.9910, 77.5530)
    )

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        spinnerArea = findViewById(R.id.spinnerArea)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)
        val btnDetectLocation = findViewById<Button>(R.id.btnDetectLocation)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, areas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerArea.adapter = adapter

        // Auto detect location button
        btnDetectLocation.setOnClickListener {
            detectLocation()
        }

        // Try to detect location automatically on open
        detectLocation()

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val area = spinnerArea.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (area == "Select your area") {
                Toast.makeText(this, "Please select your area", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val userId = result.user!!.uid
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "area" to area,
                        "userId" to userId
                    )

                    db.collection("users").document(userId).set(user)
                        .addOnSuccessListener {
                            val prefManager = PreferenceManager(this)
                            prefManager.saveUser(name, email, area, userId)
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun detectLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        Toast.makeText(this, "Detecting your location...", Toast.LENGTH_SHORT).show()

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val nearestArea = findNearestArea(location.latitude, location.longitude)
                    val areaIndex = areas.indexOf(nearestArea)
                    if (areaIndex >= 0) {
                        spinnerArea.setSelection(areaIndex)
                        Toast.makeText(this, "📍 Area detected: $nearestArea", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Could not detect location. Please select manually.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Location error. Please select manually.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun findNearestArea(lat: Double, lon: Double): String {
        var nearestArea = "Koramangala"
        var minDistance = Double.MAX_VALUE

        for ((area, coords) in areaCoordinates) {
            val distance = calculateDistance(lat, lon, coords.first, coords.second)
            if (distance < minDistance) {
                minDistance = distance
                nearestArea = area
            }
        }
        return nearestArea
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            detectLocation()
        } else {
            Toast.makeText(this, "Location permission denied. Please select area manually.", Toast.LENGTH_SHORT).show()
        }
    }
}