package com.sasha.cityupdates.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sasha.cityupdates.PreferenceManager
import com.sasha.cityupdates.R

class SettingsActivity : AppCompatActivity() {

    private val areas = listOf(
        "Select area",
        "Koramangala", "Indiranagar", "Whitefield",
        "Jayanagar", "Malleshwaram", "HSR Layout",
        "Electronic City", "Hebbal", "Rajajinagar"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefManager = PreferenceManager(this)

        // Show current area from SharedPreferences
        val tvCurrentArea = findViewById<TextView>(R.id.tvCurrentArea)
        tvCurrentArea.text = "📍 ${prefManager.getUserArea()}"

        // Set up area spinner
        val spinnerNewArea = findViewById<Spinner>(R.id.spinnerNewArea)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, areas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNewArea.adapter = adapter

        // Pre-select current area in spinner
        val currentArea = prefManager.getUserArea()
        val currentIndex = areas.indexOf(currentArea)
        if (currentIndex >= 0) spinnerNewArea.setSelection(currentIndex)

        findViewById<Button>(R.id.btnSaveArea).setOnClickListener {
            val selectedArea = spinnerNewArea.selectedItem.toString()
            if (selectedArea == "Select area") {
                Toast.makeText(this, "Please select an area", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save new area to SharedPreferences
            prefManager.saveUserArea(selectedArea)
            tvCurrentArea.text = "📍 $selectedArea"
            Toast.makeText(this, "Area updated to $selectedArea!", Toast.LENGTH_SHORT).show()
        }
    }
}