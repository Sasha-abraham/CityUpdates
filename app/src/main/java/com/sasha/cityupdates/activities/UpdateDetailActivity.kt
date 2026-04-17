package com.sasha.cityupdates.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.sasha.cityupdates.PreferenceManager
import com.sasha.cityupdates.R

class UpdateDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_detail)

        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val area = intent.getStringExtra("area") ?: ""
        val postedBy = intent.getStringExtra("postedBy") ?: ""
        val postId = intent.getStringExtra("postId") ?: ""
        val postUserId = intent.getStringExtra("userId") ?: ""

        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailDescription).text = description
        findViewById<TextView>(R.id.tvDetailCategory).text = category
        findViewById<TextView>(R.id.tvDetailArea).text = "📍 $area"
        findViewById<TextView>(R.id.tvDetailPostedBy).text = "👤 $postedBy"

        // Only show delete button if this post belongs to the logged-in user
        val prefManager = PreferenceManager(this)
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        if (postUserId == prefManager.getUserId()) {
            btnDelete.visibility = View.VISIBLE
        } else {
            btnDelete.visibility = View.GONE
        }

        btnDelete.setOnClickListener {
            if (postId.isEmpty()) {
                Toast.makeText(this, "Cannot delete this post", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseFirestore.getInstance().collection("updates")
                .document(postId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Update deleted!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}