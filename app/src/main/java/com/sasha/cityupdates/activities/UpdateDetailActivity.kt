package com.sasha.cityupdates.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
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
        val urgency = intent.getStringExtra("urgency") ?: "Low"
        val flagCount = intent.getIntExtra("flagCount", 0)
        @Suppress("UNCHECKED_CAST")
        val flaggedBy = intent.getStringArrayListExtra("flaggedBy") ?: arrayListOf<String>()

        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailDescription).text = description
        findViewById<TextView>(R.id.tvDetailCategory).text = category
        findViewById<TextView>(R.id.tvDetailArea).text = "📍 $area"
        findViewById<TextView>(R.id.tvDetailPostedBy).text = "👤 $postedBy"
        findViewById<TextView>(R.id.tvDetailFlagCount).text = if (flagCount > 0) "🚩 $flagCount flags" else ""

        // Urgency color
        val tvUrgency = findViewById<TextView>(R.id.tvDetailUrgency)
        val (urgencyColor, urgencyLabel) = when (urgency) {
            "Critical" -> Pair("#F44336", "🔴 Critical")
            "High"     -> Pair("#FF9800", "🟠 High")
            "Medium"   -> Pair("#FFC107", "🟡 Medium")
            else       -> Pair("#4CAF50", "🟢 Low")
        }
        tvUrgency.text = urgencyLabel
        tvUrgency.setBackgroundColor(Color.parseColor(urgencyColor))

        val prefManager = PreferenceManager(this)
        val currentUserId = prefManager.getUserId()
        val db = FirebaseFirestore.getInstance()

        // Delete — only visible to post owner
        val btnDelete = findViewById<Button>(R.id.btnDelete)
        if (postUserId == currentUserId && postId.isNotEmpty()) {
            btnDelete.visibility = View.VISIBLE
            btnDelete.setOnClickListener {
                db.collection("updates").document(postId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Update deleted!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to delete: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            btnDelete.visibility = View.GONE
        }

        // Flag — hidden for own posts, disabled if already flagged
        val btnFlag = findViewById<Button>(R.id.btnFlag)
        when {
            postUserId == currentUserId -> {
                btnFlag.visibility = View.GONE
            }
            flaggedBy.contains(currentUserId) -> {
                btnFlag.text = "✅ Already Flagged"
                btnFlag.isEnabled = false
            }
            else -> {
                btnFlag.setOnClickListener {
                    if (postId.isEmpty()) return@setOnClickListener
                    db.collection("updates").document(postId)
                        .update(
                            "flagCount", FieldValue.increment(1),
                            "flaggedBy", FieldValue.arrayUnion(currentUserId)
                        )
                        .addOnSuccessListener {
                            Toast.makeText(this, "Post flagged!", Toast.LENGTH_SHORT).show()
                            btnFlag.text = "✅ Flagged"
                            btnFlag.isEnabled = false
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }
}