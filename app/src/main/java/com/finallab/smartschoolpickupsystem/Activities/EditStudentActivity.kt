package com.finallab.smartschoolpickupsystem.Activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityEditStudentBinding
import com.finallab.smartschoolpickupsystem.model.Repository.GuardianStudentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class EditStudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditStudentBinding
    private var student: Student? = null
    private lateinit var repository: GuardianStudentRepository
    private lateinit var firestore: FirebaseFirestore

    private var studentId: Int = -1
    private var studentDocId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        repository = GuardianStudentRepository(database)
        firestore = FirebaseFirestore.getInstance()

        studentId = intent.getIntExtra("studentID", -1)
        studentDocId = intent.getStringExtra("studentDocumentID") ?: ""

        if (studentId == -1 || studentDocId.isEmpty()) {
            showToast("Invalid student ID or document ID")
            finish()
            return
        }

        loadStudentData()

        binding.updateS.setOnClickListener { updateStudent() }
        binding.cancel.setOnClickListener { finish() }
    }

    private fun loadStudentData() {
        lifecycleScope.launch {
            student = repository.getStudentById(studentId)
            student?.let {
                binding.ns.setText(it.Sname ?: "")
                binding.rs.setText(it.reg ?: "")
                binding.cs.setText(it.studentClass ?: "")
                binding.secs.setText(it.section ?: "")
            } ?: run {
                showToast("Student not found")
                finish()
            }
        }
    }

    private fun updateStudent() {
        val updatedName = binding.ns.text.toString().trim()
        val updatedReg = binding.rs.text.toString().trim()
        val updatedClass = binding.cs.text.toString().trim()
        val updatedSection = binding.secs.text.toString().trim()

        if (updatedName.isEmpty() || updatedReg.isEmpty() || updatedClass.isEmpty() || updatedSection.isEmpty()) {
            showToast("All fields are required")
            return
        }

        lifecycleScope.launch {
            student?.let {
                val updatedStudent = it.copy(
                    Sname = updatedName,
                    reg = updatedReg,
                    studentClass = updatedClass,
                    section = updatedSection
                )

                try {
                    // Update in Room
                    repository.updateStudent(updatedStudent)

                    // Update in Firestore
                    updateFirestoreStudent(studentDocId, updatedStudent)

                    showToast("Student updated successfully")
                    finish()

                } catch (e: Exception) {
                    showToast("Failed to update student: ${e.message}")
                }
            }
        }
    }

    private fun updateFirestoreStudent(docId: String, student: Student) {
        firestore.collection("students").document(docId)
            .update(
                "Sname", student.Sname,
                "reg", student.reg,
                "studentClass", student.studentClass,
                "section", student.section
            )
            .addOnSuccessListener {
                showToast("Success to update Firestore")
            }
            .addOnFailureListener { e ->
                showToast("Failed to update Firestore: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
