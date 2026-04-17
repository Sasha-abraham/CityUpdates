package com.sasha.cityupdates.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.sasha.cityupdates.PreferenceManager
import com.sasha.cityupdates.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val prefManager = PreferenceManager(this)

        // Load from SharedPreferences — no Firebase call needed!
        findViewById<TextView>(R.id.tvName).text = prefManager.getUserName()
        findViewById<TextView>(R.id.tvEmail).text = prefManager.getUserEmail()
        findViewById<TextView>(R.id.tvArea).text = "📍 ${prefManager.getUserArea()}"

        // Show today's date as member since (for demo purposes)
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        findViewById<TextView>(R.id.tvMemberSince).text = dateFormat.format(Date())

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            prefManager.clearUser()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}