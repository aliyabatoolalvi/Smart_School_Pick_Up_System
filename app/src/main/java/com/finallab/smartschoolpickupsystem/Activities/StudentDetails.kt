package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.DataModels.GuardianStudentCrossRef
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.OnDataChangedListener
import com.finallab.smartschoolpickupsystem.Recycler.OnItemDeletedListener
import com.finallab.smartschoolpickupsystem.Recycler.RecyclerViewAdapter
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.Utilities
import com.finallab.smartschoolpickupsystem.databinding.ActivityStudentDetailsBinding
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


class StudentDetails : AppCompatActivity() {
    private lateinit var binding: ActivityStudentDetailsBinding
    private lateinit var student: Student
    private val firestore = FirebaseFirestore.getInstance()
    private val guardiansList = mutableListOf<Guardian>()
    private lateinit var adapter: RecyclerViewAdapter
    private var onItemDeletedListener: OnItemDeletedListener? = null
    private var dataChangedListener: OnDataChangedListener? = null
    fun setOnDataChangedListener(listener: OnDataChangedListener?) {
        this.dataChangedListener = listener
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.getIntExtra("id", -1)
        if (id == -1) {
            Toast.makeText(this, "Invalid student ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapter = RecyclerViewAdapter(
            guardiansList as MutableList<Any>,
            lifecycleScope,
            onDeleteClick = { guardian -> deleteGuardian(guardian) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@StudentDetails)
            val fetchedStudent = db.studentDao().getStudentById(id)

            if (fetchedStudent == null) {
                Toast.makeText(this@StudentDetails, "Student not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            student = fetchedStudent
            displayStudentDetails()
            loadGuardians()
        }

        binding.addG.setOnClickListener {
            val intent = Intent(this, AddGuardian::class.java).apply {
                putExtra("id", id)
                putExtra("studentDocumentID", student.studentDocId)
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

    fun setOnItemDeletedListener(listener: OnItemDeletedListener?) {
        this.onItemDeletedListener = listener
    }

    private fun deleteGuardian(guardian: Guardian) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(this@StudentDetails)
                db.guardianDao().deleteGuardian(guardian)

                firestore.collection("guardians")
                    .document(guardian.guardianDocId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this@StudentDetails, "Guardian deleted", Toast.LENGTH_SHORT).show()
                        guardiansList.remove(guardian)
                        adapter.notifyDataSetChanged()

                        onItemDeletedListener?.onDataUpdated()

                        val listener = dataChangedListener
                        listener?.onDataUpdated()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@StudentDetails, "Failed to delete guardian", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(this@StudentDetails, "Error deleting guardian", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (::student.isInitialized) {
            loadGuardians()
        }
    }

    private fun loadGuardians() {
        guardiansList.clear()

        if (Utilities.isNetworkConnected(this)) {
            fetchGuardiansFromFirestore(student.studentDocId)
        } else {
            loadGuardiansFromRoom(student.studentID)
        }
    }

    private fun loadGuardiansFromRoom(studentID: Int) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            db.guardianStudentDao().getStudentWithGuardians(studentID).collect { studentWithGuardians ->
                guardiansList.clear()
                studentWithGuardians?.guardians?.let {
                    guardiansList.addAll(it)
                }
                Log.d("StudentDetails", "Loaded guardians offline: ${guardiansList.size}")
                adapter.notifyDataSetChanged()

                if (guardiansList.isEmpty()) {
                    Toast.makeText(this@StudentDetails, "No guardians available offline", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchGuardiansFromFirestore(studentDocId: String) {
        firestore.collection("students").document(studentDocId)
            .get()
            .addOnSuccessListener { studentDoc ->
                val guardianIds = studentDoc.get("guardians") as? List<String> ?: emptyList()

                if (guardianIds.isEmpty()) {
                    Toast.makeText(this, "No guardians linked", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                firestore.collection("guardians")
                    .whereIn(FieldPath.documentId(), guardianIds)
                    .get()
                    .addOnSuccessListener { guardianDocs ->
                        lifecycleScope.launch {
                            val db = AppDatabase.getDatabase(this@StudentDetails)

                            guardiansList.clear()
                            for (doc in guardianDocs) {
                                val guardian = doc.toObject(Guardian::class.java).apply {
                                    guardianDocId = doc.id
                                }
                                guardiansList.add(guardian)

                                // Save to Room
                                db.guardianDao().insertGuardian(guardian)
                                db.guardianStudentDao().insertGuardianStudentCrossRef(
                                    GuardianStudentCrossRef(
                                        guardian.guardianID,
                                        student.studentID
                                    )
                                )
                            }
                            Log.d("StudentDetails", "Loaded guardians online: ${guardiansList.size}")
                            adapter.notifyDataSetChanged()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to load guardians from server", Toast.LENGTH_SHORT).show()
                        adapter.notifyDataSetChanged()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch student info", Toast.LENGTH_SHORT).show()
            }
    }
}
