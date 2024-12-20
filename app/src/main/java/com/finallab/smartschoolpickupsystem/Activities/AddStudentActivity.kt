package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityAddStudentBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddStudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStudentBinding
    private val firestore = FirebaseFirestore.getInstance() // Initialize Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.regS.setOnClickListener {
            if (binding.Sname.editText?.text.toString().isEmpty() ||
                binding.reg.editText?.text.toString().isEmpty() ||
                binding.studentClass.editText?.text.toString().isEmpty() ||
                binding.section.editText?.text.toString().isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                val student = Student(
                    Sname = binding.Sname.editText?.text.toString(),
                    reg = binding.reg.editText?.text.toString(),
                    studentClass = binding.studentClass.editText?.text.toString(),
                    section = binding.section.editText?.text.toString(),
                    studentDocId = "" // Initially set as empty
                )

                // Save to Firestore first
                saveStudentToFirestore(student)
            }
        }
        binding.backButton.setOnClickListener{
            super.onBackPressed()
        }
    }

    private fun saveStudentToFirestore(student: Student) {
        val studentMap = hashMapOf(
            "Sname" to student.Sname,
            "reg" to student.reg,
            "studentClass" to student.studentClass,
            "section" to student.section
        )

        firestore.collection("students")
            .add(studentMap)
            .addOnSuccessListener { documentReference ->

                val studentDocumentID = documentReference.id

                Toast.makeText(this, "Student Registered in Firestore", Toast.LENGTH_SHORT).show()

                student.studentDocId = studentDocumentID

                updateStudentInLocalDatabase(student)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateStudentInLocalDatabase(student: Student) {

        AppDatabase.getDatabase(this).studentDao().insert(student) // You can use update() if the student already exists
    }
}
