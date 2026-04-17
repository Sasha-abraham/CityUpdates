package com.sasha.cityupdates.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.sasha.cityupdates.PreferenceManager
import com.sasha.cityupdates.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefManager = PreferenceManager(this)

        // Wait 2 seconds then decide where to go
        Handler(Looper.getMainLooper()).postDelayed({
            if (prefManager.isLoggedIn()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}