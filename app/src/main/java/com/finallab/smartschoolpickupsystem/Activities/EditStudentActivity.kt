package com.finallab.smartschoolpickupsystem.Activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityEditStudentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class EditStudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditStudentBinding
    private var student: Student? = null
    private lateinit var firestore: FirebaseFirestore
    private var studentDocId: String = ""
    private var studentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        studentId = intent.getIntExtra("id", -1)
        if (studentId == -1) {
            Toast.makeText(this, "Invalid student ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            student = AppDatabase.getDatabase(this@EditStudentActivity).studentDao().getstudentById(studentId)

            if (student == null) {
                Toast.makeText(this@EditStudentActivity, "Student not found", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            student?.let {
                studentDocId = it.studentDocId
                binding.ns.setText(it.Sname ?: "")
                binding.rs.setText(it.reg ?: "")
                binding.cs.setText(it.studentClass ?: "")
                binding.secs.setText(it.section ?: "")
            }
        }

        binding.updateS.setOnClickListener {
            val updatedName = binding.ns.text.toString()
            val updatedReg = binding.rs.text.toString()
            val updatedClass = binding.cs.text.toString()
            val updatedSection = binding.secs.text.toString()
            val mAuth = FirebaseAuth.getInstance()
            val currentUser = mAuth.currentUser
            val userId: String? = currentUser?.uid

            lifecycleScope.launch {
                student?.let {
                    val updatedStudent = userId?.let { uid ->
                        Student(studentId, updatedName, updatedReg, updatedClass, updatedSection, studentDocId, uid)
                    }
                    updatedStudent?.let { student ->
                        AppDatabase.getDatabase(this@EditStudentActivity).studentDao().update(student)
                        updateFirestoreStudent(studentDocId, student)
                    }
                }
            }
        }

        binding.cancel.setOnClickListener {
            finish()
        }
    }
    
    private fun updateFirestoreStudent(docId: String, student: Student) {
        if (docId.isNotEmpty()) {
            firestore.collection("students").document(docId)
                .update(
                    "Sname", student.Sname,
                    "reg", student.reg,
                    "studentClass", student.studentClass,
                    "section", student.section
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update student", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Invalid document ID", Toast.LENGTH_SHORT).show()
        }
    }
}