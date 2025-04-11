package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
            Log.d("StudentDetails", "Received Student ID: $id")
            finish()
            return
        }

        if (id != -1) {
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(this@StudentDetails)
                val fetchedStudent = db.studentDao().getStudentById(id)

                if (fetchedStudent == null) {
                    Toast.makeText(this@StudentDetails, "Student not found", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                    return@launch
                }

                student = fetchedStudent
                displayStudentDetails()
                setupRecyclerView()
                fetchGuardiansForStudent(student.studentDocId)
            }
        } else {
            Log.e("StudentDetails", "Invalid studentID")
        }


        binding.addG.setOnClickListener {
            val intent = Intent(this, AddGuardian::class.java).apply {
                putExtra("id", id) // Pass student ID
                putExtra("studentDocumentID", student.studentDocId) // Pass Firestore document ID
            }
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun displayStudentDetails() {
        binding.nameS.text = "Name: ${student.Sname}"
        binding.rollno.text = "Reg no: ${student.reg}"
        binding.ClassS.text = "Class: ${student.studentClass}"
        binding.sectionS.text = "Section: ${student.section}"
    }

    private fun setupRecyclerView() {
        adapter = RecyclerViewAdapter(
            guardiansList as MutableList<Any>,
            lifecycleScope,
            onDeleteClick = { guardian ->
                deleteGuardian(guardian)
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
        if (!::student.isInitialized || student.studentDocId.isEmpty()) {
            Toast.makeText(this, "No associated Firestore record", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("guardians")
            .whereEqualTo("studentDocumentID", student.studentDocId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                guardiansList.clear()

                for (document in querySnapshot.documents) {
                    val guardian = document.toObject(Guardian::class.java)?.apply {
                        guardianDocId = document.id
                    }
                    guardian?.let { guardiansList.add(it) }
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load guardians", Toast.LENGTH_SHORT).show()
            }
    }


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
                        Toast.makeText(this@StudentDetails, "Guardian deleted", Toast.LENGTH_SHORT)
                            .show()
                        guardiansList.remove(guardian) // Remove from local list
                        adapter.notifyDataSetChanged() // Refresh RecyclerView
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this@StudentDetails,
                            "Failed to delete guardian",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(this@StudentDetails, "Error deleting guardian", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    override fun onResume() {
        super.onResume()

        if (!::student.isInitialized) return

        val db = AppDatabase.getDatabase(this@StudentDetails)

        lifecycleScope.launch {
            db.guardianStudentDao().getStudentWithGuardians(student.studentID).collect { studentWithGuardians ->
                guardiansList.clear()

                // Add guardians to the list
                studentWithGuardians?.guardians?.let { guardiansList.addAll(it) }

                Log.d("StudentDetails", "Room ID: ${student.studentID}, Firestore ID: ${student.studentDocId}")
                Log.d("StudentDetails", "Guardians fetched: ${guardiansList.size}")

                // Perform additional background logging if needed
                CoroutineScope(Dispatchers.IO).launch {
                    studentWithGuardians?.let {
                        Log.d("Room", "Student: ${it.student}")
                        it.guardians.forEach { guardian ->
                            Log.d("Room", "Guardian: ${guardian.Gname}")
                        }
                    }
                }

                // Update the adapter
                adapter.notifyDataSetChanged()

                // Fallback to Firestore if Room has no guardians
                if (guardiansList.isEmpty()) {
                    fetchGuardiansFromFirestore()
                }
            }
        }
    }

    private fun fetchGuardiansForStudent(studentDocId: String) {
        firestore.collection("students").document(studentDocId)
            .get()
            .addOnSuccessListener { studentDoc ->
                val guardianIds = studentDoc.get("guardians") as? List<String> ?: emptyList()

                if (guardianIds.isEmpty()) {
                    Toast.makeText(this, "No guardians linked", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                firestore.collection("guardians")
                    .whereIn(FieldPath.documentId(), guardianIds)
                    .get()
                    .addOnSuccessListener { guardianDocs ->
                        guardiansList.clear()

                        for (doc in guardianDocs) {
                            val guardian = doc.toObject(Guardian::class.java).apply {
                                guardianDocId = doc.id
                            }
                            guardiansList.add(guardian)
                        }

                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to load guardian info", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch student", Toast.LENGTH_SHORT).show()
            }
    }



}


