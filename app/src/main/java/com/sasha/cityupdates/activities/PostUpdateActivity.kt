package com.sasha.cityupdates.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sasha.cityupdates.R

class PostUpdateActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val categories = listOf(
        "Select category", "Water", "Power", "Roads", "Floods", "Events", "Culture"
    )

    private val areas = listOf(
        "Select area",
        "Koramangala", "Indiranagar", "Whitefield",
        "Jayanagar", "Malleshwaram", "HSR Layout",
        "Electronic City", "Hebbal", "Rajajinagar"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_update)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerArea = findViewById<Spinner>(R.id.spinnerArea)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        val areaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, areas)
        areaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerArea.adapter = areaAdapter

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val area = spinnerArea.selectedItem.toString()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (category == "Select category") {
                Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (area == "Select area") {
                Toast.makeText(this, "Please select an area", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser!!.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    val postedBy = doc.getString("name") ?: "Anonymous"

                    val docRef = db.collection("updates").document()
                    val update = hashMapOf(
                        "id" to docRef.id,
                        "title" to title,
                        "description" to description,
                        "category" to category,
                        "area" to area,
                        "postedBy" to postedBy,
                        "userId" to userId,
                        "timestamp" to System.currentTimeMillis()
                    )

                    docRef.set(update)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Update posted!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to post: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
        }
    }
}