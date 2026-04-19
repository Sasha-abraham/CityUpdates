package com.sasha.cityupdates.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sasha.cityupdates.PreferenceManager
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

        val prefManager = PreferenceManager(this)

        // Show user's area in header
        val tvUserArea = findViewById<TextView>(R.id.tvUserArea)
        tvUserArea.text = "📍 ${prefManager.getUserArea()}"

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
            intent.putExtra("urgency", update.urgency)
            intent.putExtra("flagCount", update.flagCount)
            intent.putStringArrayListExtra("flaggedBy", ArrayList(update.flaggedBy))
            startActivity(intent)
        }

        // Load all updates in real time — no filter by user
        db.collection("updates")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    allUpdates = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Update::class.java)
                    }.filter { it.flagCount < 3 }.toMutableList()
                    adapter.setUpdates(allUpdates)
                }
            }

        // Area filter buttons
        findViewById<Button>(R.id.btnAll).setOnClickListener { adapter.setUpdates(allUpdates) }
        findViewById<Button>(R.id.btnKoramangala).setOnClickListener { filterByArea("Koramangala") }
        findViewById<Button>(R.id.btnIndiranagar).setOnClickListener { filterByArea("Indiranagar") }
        findViewById<Button>(R.id.btnWhitefield).setOnClickListener { filterByArea("Whitefield") }
        findViewById<Button>(R.id.btnJayanagar).setOnClickListener { filterByArea("Jayanagar") }
        findViewById<Button>(R.id.btnMalleshwaram).setOnClickListener { filterByArea("Malleshwaram") }
        findViewById<Button>(R.id.btnHSR).setOnClickListener { filterByArea("HSR Layout") }
        findViewById<Button>(R.id.btnElectronic).setOnClickListener { filterByArea("Electronic City") }
        findViewById<Button>(R.id.btnHebbal).setOnClickListener { filterByArea("Hebbal") }
        findViewById<Button>(R.id.btnRajajinagar).setOnClickListener { filterByArea("Rajajinagar") }

        findViewById<FloatingActionButton>(R.id.fabPost).setOnClickListener {
            startActivity(Intent(this, PostUpdateActivity::class.java))
        }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun filterByArea(area: String) {
        val filtered = allUpdates.filter { it.area == area }
        adapter.setUpdates(filtered)
    }
}