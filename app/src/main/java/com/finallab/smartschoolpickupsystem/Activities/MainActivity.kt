package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.OnStudentDeletedListener
import com.finallab.smartschoolpickupsystem.Recycler.RecyclerViewAdapter
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), OnStudentDeletedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the RecyclerView
        setupRecyclerView()

        // Navigate to AddStudentActivity
        binding.addS.setOnClickListener {
            startActivity(Intent(this, AddStudentActivity::class.java))
        }

        // Handle the back button
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Updated for modern APIs
        }

    }

    override fun onResume() {
        super.onResume()
        loadStudentData()
    }

    private fun setupRecyclerView() {
        adapter = RecyclerViewAdapter(mutableListOf(), lifecycleScope, this)
        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = adapter
    }

    private fun loadStudentData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            lifecycleScope.launch {
                try {
                    // Fetch student data in the background
                    val students = withContext(Dispatchers.IO) {
                        AppDatabase.getDatabase(this@MainActivity).studentDao().getStudentsByUserId(userId)

                    }
                    Log.d("StudentData", "Loaded Students: $students")

                    // Update the adapter with new data
                    adapter.updateData(students.toMutableList())

                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error loading students: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onDataUpdated() {
        loadStudentData()
    }
}
