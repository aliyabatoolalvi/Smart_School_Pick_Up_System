package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.OnStudentDeletedListener
import com.finallab.smartschoolpickupsystem.Recycler.RecyclerViewAdapter
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityStudentDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class StudentDetails : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDetailsBinding
    private lateinit var student: Student
    private val firestore = FirebaseFirestore.getInstance()
    private val guardiansList = mutableListOf<Guardian>()
    private lateinit var adapter: RecyclerViewAdapter
    private var onStudentDeletedListener: OnStudentDeletedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get student ID from Intent
        val id = intent.getIntExtra("id", -1)
        if (id == -1) {
            Toast.makeText(this, "Invalid student ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch student details and guardians in a coroutine
        lifecycleScope.launch {
            student = AppDatabase.getDatabase(this@StudentDetails).studentDao().getStudentById(id)!!
            displayStudentDetails()
            setupRecyclerView()
            fetchGuardiansFromFirestore() // Fetch guardians linked to this student
        }

        // Navigate to AddGuardian Activity
        binding.addG.setOnClickListener {
            val intent = Intent(this, AddGuardian::class.java).apply {
                putExtra("id", id) // Pass student ID
                putExtra("studentDocumentID", student.studentDocId) // Pass Firestore document ID
            }
            startActivity(intent)
        }

        // Handle back navigation
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // Display student details in UI
    private fun displayStudentDetails() {
        binding.nameS.text = "Name: ${student.Sname}"
        binding.rollno.text = "Reg no: ${student.reg}"
        binding.ClassS.text = "Class: ${student.studentClass}"
        binding.sectionS.text = "Section: ${student.section}"
    }

    // Setup RecyclerView with Guardian Adapter
    private fun setupRecyclerView() {
        adapter = RecyclerViewAdapter(
            guardiansList as MutableList<Any>,
            lifecycleScope,
            onDeleteClick = { guardian ->
                deleteGuardian(guardian) // Handle Guardian Deletion
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
    fun setOnStudentDeletedListener(listener: OnStudentDeletedListener?) {
        this.onStudentDeletedListener = listener
    }
    // Fetch guardians from Firestore
    private fun fetchGuardiansFromFirestore() {
        if (student.studentDocId.isEmpty()) {
            Toast.makeText(this, "No associated Firestore record", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("guardians")
            .whereEqualTo("studentId", student.studentDocId) // Match guardians linked to this student
            .get()
            .addOnSuccessListener { querySnapshot ->
                guardiansList.clear() // Clear old data

                // Map Firestore documents to Guardian objects
                for (document in querySnapshot.documents) {
                    val guardian = document.toObject(Guardian::class.java)?.apply {
                        guardianDocId = document.id // Store Firestore Document ID for deletion
                    }
                    guardian?.let { guardiansList.add(it) }
                }

                adapter.notifyDataSetChanged() // Update RecyclerView
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load guardians", Toast.LENGTH_SHORT).show()
            }
    }

    // Delete Guardian from Firestore and Room
    private fun deleteGuardian(guardian: Guardian) {
        lifecycleScope.launch {
            try {
                // Delete from Room Database
                AppDatabase.getDatabase(this@StudentDetails).guardianDao()
                    .deleteGuardian(guardian)

                // Notify Listener
                onStudentDeletedListener?.onDataUpdated()

                // Delete from Firestore
                firestore.collection("guardians")
                    .document(guardian.guardianDocId) // Ensure guardianDocId is stored
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this@StudentDetails, "Guardian deleted", Toast.LENGTH_SHORT).show()
                        guardiansList.remove(guardian) // Remove from local list
                        adapter.notifyDataSetChanged() // Refresh RecyclerView
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@StudentDetails, "Failed to delete guardian", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(this@StudentDetails, "Error deleting guardian", Toast.LENGTH_SHORT).show()
            }
        }
    }



    // Keep Room and Firestore Data Synced on Resume
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            AppDatabase.getDatabase(this@StudentDetails).guardianDao()
                .getGuardiansByStudentId(student.studentID)
                .collect { guardians ->
                    guardiansList.clear()
                    guardiansList.addAll(guardians)
                    adapter.notifyDataSetChanged()
                    fetchGuardiansFromFirestore() // Ensure Firestore sync is up-to-date
                }
        }
    }
}
