package com.sasha.cityupdates.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.sasha.cityupdates.PreferenceManager
import com.sasha.cityupdates.R
import com.sasha.cityupdates.adapters.CommentAdapter
import com.sasha.cityupdates.models.Comment

class UpdateDetailActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var prefManager: PreferenceManager
    private lateinit var commentAdapter: CommentAdapter
    private var postId = ""
    private var currentUserId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_detail)

        db = FirebaseFirestore.getInstance()
        prefManager = PreferenceManager(this)
        currentUserId = prefManager.getUserId()

        // Get data from intent
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val category = intent.getStringExtra("category") ?: ""
        val area = intent.getStringExtra("area") ?: ""
        val postedBy = intent.getStringExtra("postedBy") ?: ""
        postId = intent.getStringExtra("postId") ?: ""
        val postUserId = intent.getStringExtra("userId") ?: ""
        val urgency = intent.getStringExtra("urgency") ?: "Low"
        val flagCount = intent.getIntExtra("flagCount", 0)
        val upvoteCount = intent.getIntExtra("upvoteCount", 0)
        val isResolved = intent.getBooleanExtra("isResolved", false)
        @Suppress("UNCHECKED_CAST")
        val flaggedBy = intent.getStringArrayListExtra("flaggedBy") ?: arrayListOf<String>()
        @Suppress("UNCHECKED_CAST")
        val upvotedBy = intent.getStringArrayListExtra("upvotedBy") ?: arrayListOf<String>()

        // Bind views
        findViewById<TextView>(R.id.tvDetailTitle).text = title
        findViewById<TextView>(R.id.tvDetailDescription).text = description
        findViewById<TextView>(R.id.tvDetailCategory).text = category
        findViewById<TextView>(R.id.tvDetailArea).text = "📍 $area"
        findViewById<TextView>(R.id.tvDetailPostedBy).text = "👤 $postedBy"
        findViewById<TextView>(R.id.tvDetailFlagCount).text = if (flagCount > 0) "🚩 $flagCount flags" else ""

        // Resolved badge
        val tvResolved = findViewById<TextView>(R.id.tvDetailResolved)
        tvResolved.visibility = if (isResolved) View.VISIBLE else View.GONE

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

        // Upvote button
        val btnUpvote = findViewById<Button>(R.id.btnUpvote)
        btnUpvote.text = "👍 $upvoteCount"
        if (upvotedBy.contains(currentUserId)) {
            btnUpvote.text = "👍 $upvoteCount ✓"
            btnUpvote.isEnabled = false
        }
        btnUpvote.setOnClickListener {
            if (postId.isEmpty()) return@setOnClickListener
            db.collection("updates").document(postId)
                .update(
                    "upvoteCount", FieldValue.increment(1),
                    "upvotedBy", FieldValue.arrayUnion(currentUserId)
                )
                .addOnSuccessListener {
                    btnUpvote.text = "👍 ${upvoteCount + 1} ✓"
                    btnUpvote.isEnabled = false
                    Toast.makeText(this, "Upvoted!", Toast.LENGTH_SHORT).show()
                }
        }

        // Resolve button — only for post owner
        val btnResolve = findViewById<Button>(R.id.btnResolve)
        if (postUserId == currentUserId && !isResolved) {
            btnResolve.visibility = View.VISIBLE
            btnResolve.setOnClickListener {
                db.collection("updates").document(postId)
                    .update("isResolved", true)
                    .addOnSuccessListener {
                        tvResolved.visibility = View.VISIBLE
                        btnResolve.visibility = View.GONE
                        Toast.makeText(this, "Marked as resolved!", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Delete button — only for post owner
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

        // Flag button
        val btnFlag = findViewById<Button>(R.id.btnFlag)
        when {
            postUserId == currentUserId -> btnFlag.visibility = View.GONE
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

        // Back button
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        // Comments RecyclerView
        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        commentAdapter = CommentAdapter(mutableListOf())
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter

        // Load comments in real time
        if (postId.isNotEmpty()) {
            db.collection("updates").document(postId)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val comments = snapshot.documents.mapNotNull {
                            it.toObject(Comment::class.java)
                        }
                        commentAdapter.setComments(comments)
                    }
                }
        }

        // Send comment
        val etComment = findViewById<EditText>(R.id.etComment)
        findViewById<Button>(R.id.btnSendComment).setOnClickListener {
            val text = etComment.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (postId.isEmpty()) return@setOnClickListener

            val commentRef = db.collection("updates").document(postId)
                .collection("comments").document()

            val comment = hashMapOf(
                "id" to commentRef.id,
                "text" to text,
                "postedBy" to prefManager.getUserName(),
                "userId" to currentUserId,
                "timestamp" to System.currentTimeMillis()
            )

            commentRef.set(comment)
                .addOnSuccessListener {
                    etComment.text.clear()
                    // Update comment count on the post
                    db.collection("updates").document(postId)
                        .update("commentCount", FieldValue.increment(1))
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show()
                }
        }
    }
}