package com.finallab.smartschoolpickupsystem.Activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityAddStudentBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddStudentActivity : AppCompatActivity() {
    lateinit var progressDialog: ProgressDialog
    private lateinit var binding: ActivityAddStudentBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressBar = binding.progressBar // Initialize ProgressBar


        FirebaseFirestore.setLoggingEnabled(true)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("FirestoreDebug", "Current User UID: $userId")


        val progressDialog= ProgressDialog(this)
        progressDialog.setMessage("Please wait!")
        progressDialog.setCancelable(false)
        binding.regS.setOnClickListener {
            if (binding.namestudent.editText?.text.toString().isEmpty() ||
                binding.rollnostu.editText?.text.toString().isEmpty() ||
                binding.stuclass.editText?.text.toString().isEmpty() ||
                binding.secstu.editText?.text.toString().isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {

                if (!isNetworkConnected()){
                    showNotConnectedSnack()
                    return@setOnClickListener
                }
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

                val student = Student(
                    Sname = binding.namestudent.editText?.text.toString(),
                    reg = binding.rollnostu.editText?.text.toString(),
                    studentClass = binding.stuclass.editText?.text.toString(),
                    section = binding.secstu.editText?.text.toString(),
                    studentDocId = "" , // Initially set as empty,
                    userId = userId

                )
                progressBar.visibility = View.VISIBLE // Show loading
                // Save to Firestore first
                saveStudentToFirestore(student)
            }
        }
        binding.backButton.setOnClickListener{
            super.onBackPressed()
        }
    }


    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private fun showNotConnectedSnack(){
        val snackbar = Snackbar.make(binding.root,"No internet connection", Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("Open Settings"){
            startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
        }

        snackbar.setActionTextColor(resources.getColor(android.R.color.holo_red_light))
        snackbar.show()
    }

//    private fun saveStudentToFirestore(student: Student) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid // Get authenticated user ID
//        if (userId == null) {
//            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val studentMap = hashMapOf(
//            "Sname" to student.Sname,
//            "reg" to student.reg,
//            "studentClass" to student.studentClass,
//            "section" to student.section,
//            "userId" to userId // Add userId field
//        )
//
//        firestore.collection("students")
//            .add(studentMap)
//            .addOnSuccessListener { documentReference ->
//                val studentDocumentID = documentReference.id
//                progressBar.visibility = View.GONE // Hide loading
//
//                Toast.makeText(this, "Student Registered in Firestore", Toast.LENGTH_SHORT).show()
//
//                // Update student object
//                student.studentDocId = studentDocumentID
//
//                // Update Firestore document with studentDocId
//                firestore.collection("students").document(studentDocumentID)
//                    .update("studentDocId", studentDocumentID)
//
//                // Save updated student to Room database
//                updateStudentInLocalDatabase(student)
//
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
//
//            .addOnFailureListener { e ->
//                progressBar.visibility = View.GONE // Hide loading on failure
//                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun saveStudentToFirestore(student: Student) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val studentMap = hashMapOf(
            "Sname" to student.Sname,
            "reg" to student.reg,
            "studentClass" to student.studentClass,
            "section" to student.section,
            "userId" to userId
        )

        firestore.collection("students")
            .add(studentMap)
            .addOnSuccessListener { documentReference ->
                val studentDocumentID = documentReference.id // Firestore document ID

                // Update Firestore with document ID
                documentReference.update("studentDocId", studentDocumentID)

                progressBar.visibility = View.GONE
                Toast.makeText(this, "Student Registered in Firestore", Toast.LENGTH_SHORT).show()

                // Save student in Room
                student.studentDocId = studentDocumentID
                student.userId = userId // Assign user ID
                updateStudentInLocalDatabase(student)

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateStudentInLocalDatabase(student: Student) {
        AppDatabase.getDatabase(this).studentDao().insert(student)
    }

}
