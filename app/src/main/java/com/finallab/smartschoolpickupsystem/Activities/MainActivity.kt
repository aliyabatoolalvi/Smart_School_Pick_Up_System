package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.OnStudentDeletedListener
import com.finallab.smartschoolpickupsystem.Recycler.OnItemDeletedListener
import com.finallab.smartschoolpickupsystem.Recycler.RecyclerViewAdapter
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), OnItemDeletedListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.addS.setOnClickListener {
            startActivity(Intent(this, AddStudentActivity::class.java))
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        binding.searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchStudents(query)
                } else {
                    loadStudentData()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })



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
                    val students = withContext(Dispatchers.IO) {
                        AppDatabase.getDatabase(this@MainActivity).studentDao().getStudentsByUserId(userId)

                    }
                    Log.d("StudentData", "Loaded Students: $students")

                    adapter.updateData(students.toMutableList())

                    if (students.isEmpty()) {
                        binding.noResultsText.visibility = View.VISIBLE
                    } else {
                        binding.noResultsText.visibility = View.GONE
                    }


                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error loading students: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchStudents(query: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("students")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    // Filter documents based on the search query
                    val filteredStudents = result.documents.mapNotNull { document ->
                        val name = document.getString("name")
                        if (name != null && name.contains(query, ignoreCase = true)) {
                            // Create a Student object and return it
                            Student(
                                studentID = document.getLong("studentID")?.toInt() ?: 0,
                                Sname = name,
                                reg = document.getString("reg") ?: "",
                                studentClass = document.getString("class") ?: "",
                                section = document.getString("section") ?: "",
                                studentDocId = document.id,
                                // Add the document ID for reference
                                userId = userId
                            )
                        } else null
                    }

                    // Update the adapter's data with the filtered list
                    adapter.updateData(filteredStudents.toMutableList())

                    // Show "no results" message if the list is empty
                    if (filteredStudents.isEmpty()) {
                        binding.noResultsText.visibility = View.VISIBLE
                    } else {
                        binding.noResultsText.visibility = View.GONE
                    }

                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@MainActivity, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }



    override fun onDataUpdated() {
        loadStudentData()
    }
}
