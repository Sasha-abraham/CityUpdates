package com.sasha.cityupdates.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sasha.cityupdates.R
import com.sasha.cityupdates.adapters.UpdateAdapter
import com.sasha.cityupdates.models.Update



class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UpdateAdapter
    private var allUpdates = mutableListOf<Update>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = UpdateAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        adapter.setOnItemClickListener { update ->
            val intent = Intent(this, UpdateDetailActivity::class.java)
            intent.putExtra("title", update.title)
            intent.putExtra("description", update.description)
            intent.putExtra("category", update.category)
            intent.putExtra("area", update.area)
            intent.putExtra("postedBy", update.postedBy)
            intent.putExtra("postId", update.id)
            intent.putExtra("userId", update.userId)
            startActivity(intent)
        }


        // Load updates from Firestore in real time
        db.collection("updates")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    allUpdates = snapshot.documents.mapNotNull {
                        it.toObject(Update::class.java)
                    }.toMutableList()
                    adapter.setUpdates(allUpdates)
                }
            }

        // Category filter buttons
        findViewById<Button>(R.id.btnAll).setOnClickListener { adapter.setUpdates(allUpdates) }
        findViewById<Button>(R.id.btnWater).setOnClickListener { filterBy("Water") }
        findViewById<Button>(R.id.btnPower).setOnClickListener { filterBy("Power") }
        findViewById<Button>(R.id.btnRoads).setOnClickListener { filterBy("Roads") }
        findViewById<Button>(R.id.btnFloods).setOnClickListener { filterBy("Floods") }
        findViewById<Button>(R.id.btnEvents).setOnClickListener { filterBy("Events") }

        // Post new update
        findViewById<FloatingActionButton>(R.id.fabPost).setOnClickListener {
            startActivity(Intent(this, PostUpdateActivity::class.java))
        }

        // Logout
        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun filterBy(category: String) {
        val filtered = allUpdates.filter { it.category == category }
        adapter.setUpdates(filtered)
    }
}