package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.DataModels.GuardianStudentCrossRef
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityAddStudentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AddStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStudentBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private lateinit var parentDropdown: AutoCompleteTextView
    private lateinit var classDropdown: AutoCompleteTextView

    private var selectedGuardianId: String = ""

    // ✅ Get stored admin UID
    private val currentUserId by lazy {
        getSharedPreferences("AdminPrefs", MODE_PRIVATE).getString("admin_userId", null)
    }
    companion object {
        private const val REQUEST_CODE_ADD_GUARDIAN = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        parentDropdown = binding.gdropdown
        classDropdown = binding.stuClass

        setupClassDropdown()
        fetchAvailableGuardians()

        binding.regS.setOnClickListener { registerStudent() }
        binding.backButton.setOnClickListener { onBackPressed() }
        binding.addGuardianButton.setOnClickListener {
            startActivityForResult(Intent(this, AddGuardian::class.java), REQUEST_CODE_ADD_GUARDIAN)
        }
    }

    private fun setupClassDropdown() {
        val classes = listOf("Nursery", "KG", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        val classAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, classes)

        classDropdown.apply {
            setAdapter(classAdapter)
            setOnClickListener { showDropDown() }
        }
    }

    private fun registerStudent() {
        val studentName = binding.namestudent.editText?.text.toString().trim()
        val rollNo = binding.rollnostu.editText?.text.toString().trim()
        val studentClass = binding.stuClass.text.toString().trim()
        val section = binding.secstu.editText?.text.toString().trim()

        if (studentName.isEmpty() || rollNo.isEmpty() || studentClass.isEmpty() || section.isEmpty()) {
            showToast("Please fill in all fields")
            return
        }

        val student = Student(
            Sname = studentName,
            reg = rollNo,
            studentClass = studentClass,
            section = section,
            userId = currentUserId ?: "",
            studentDocId = ""
        )

        saveStudentToFirestore(student)
    }

    private fun saveStudentToFirestore(student: Student) {
        firestore.collection("students")
            .add(student.toMap())
            .addOnSuccessListener { documentReference ->
                student.studentDocId = documentReference.id
                documentReference.update("studentDocId", student.studentDocId)

                if (selectedGuardianId.isNotEmpty()) {
                    linkGuardianToStudent(selectedGuardianId, student.studentDocId)
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(this@AddStudentActivity)
                    val studentId = db.studentDao().insertStudent(student).toInt()
                    student.studentID = studentId

                    if (selectedGuardianId.isNotEmpty()) {
                        try {
                            val docSnapshot = firestore.collection("guardians")
                                .document(selectedGuardianId)
                                .get()
                                .await()

                            val guardian = docSnapshot.toObject(Guardian::class.java)
                            guardian?.let {
                                it.guardianDocId = docSnapshot.id
                                db.guardianDao().insertGuardian(it)
                                db.guardianStudentDao().insertGuardianStudentCrossRef(
                                    GuardianStudentCrossRef(it.guardianID, studentId)
                                )
                            }
                        } catch (e: Exception) {
                            showToast("Error syncing Guardian locally: ${e.message}")
                        }
                    }

                    withContext(Dispatchers.Main) {
                        showToast("Student Registered Successfully!")
                        clearInputFields()
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
            .addOnFailureListener { e ->
                showToast("Error saving student: ${e.message}")
            }
    }

    private fun linkGuardianToStudent(guardianDocId: String, studentDocId: String) {
        val guardianRef = firestore.collection("guardians").document(guardianDocId)
        val studentRef = firestore.collection("students").document(studentDocId)

        guardianRef.get()
            .addOnSuccessListener { guardianSnapshot ->
                if (guardianSnapshot.exists()) {
                    firestore.runTransaction { transaction ->
                        val guardianDoc = transaction.get(guardianRef)
                        val studentDoc = transaction.get(studentRef)

                        val currentStudents = guardianDoc.get("students") as? List<String> ?: emptyList()
                        transaction.update(guardianRef, "students", currentStudents + studentDocId)

                        val currentGuardians = studentDoc.get("guardians") as? List<String> ?: emptyList()
                        transaction.update(studentRef, "guardians", currentGuardians + guardianDocId)

                    }.addOnSuccessListener {
                        showToast("Guardian linked to student successfully!")
                    }.addOnFailureListener { e ->
                        showToast("Error linking guardian: ${e.message}")
                    }
                } else {
                    showToast("Guardian not found. Cannot link.")
                }
            }
            .addOnFailureListener { e ->
                showToast("Failed to fetch guardian: ${e.message}")
            }
    }

    private fun fetchAvailableGuardians() {
        val userId = currentUser?.uid
        if (userId.isNullOrEmpty()) {
            showToast("User not logged in.")
            return
        }

        firestore.collection("guardians")
            .whereEqualTo("userId", userId) // ✅ Now using UID string
            .get()
            .addOnSuccessListener { snapshot ->
                val guardians = snapshot.documents
                    .filter { !it.getString("Gname").isNullOrBlank() && !it.getString("CNIC").isNullOrBlank() }
                    .distinctBy { it.getString("CNIC") }

                updateGuardianDropdown(guardians)
            }
            .addOnFailureListener {
                updateGuardianDropdown(emptyList())
            }
    }

    private fun updateGuardianDropdown(guardians: List<com.google.firebase.firestore.DocumentSnapshot>) {
        val items = mutableListOf<Any>().apply {
            addAll(guardians)
            add("None")
        }

        val adapter = object : ArrayAdapter<Any>(this, android.R.layout.simple_list_item_1, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = (convertView ?: LayoutInflater.from(context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)) as TextView

                val item = getItem(position)
                textView.text = if (item is com.google.firebase.firestore.DocumentSnapshot)
                    item.getString("Gname") ?: "Unknown"
                else
                    "None"

                return textView
            }
        }

        parentDropdown.setAdapter(adapter)
        parentDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedItem = adapter.getItem(position)
            if (selectedItem is com.google.firebase.firestore.DocumentSnapshot) {
                selectedGuardianId = selectedItem.id
                parentDropdown.setText(selectedItem.getString("Gname") ?: "", false)

                // ❌ Disable "Add Guardian" button when existing guardian selected
                binding.addGuardianButton.isEnabled = false
                binding.addGuardianButton.alpha = 0.5f
            } else {
                selectedGuardianId = ""
                parentDropdown.setText("None", false)

                // ✅ Enable "Add Guardian" button only when "None" is selected
                binding.addGuardianButton.isEnabled = true
                binding.addGuardianButton.alpha = 1f
            }
        }

        // Default state: show add button if no guardian selected yet
        if (selectedGuardianId.isEmpty()) {
            binding.addGuardianButton.isEnabled = true
            binding.addGuardianButton.alpha = 1f
        } else {
            binding.addGuardianButton.isEnabled = false
            binding.addGuardianButton.alpha = 0.5f
        }
    }


    private fun clearInputFields() {
        binding.namestudent.editText?.setText("")
        binding.rollnostu.editText?.setText("")
        classDropdown.setText("")
        binding.secstu.editText?.setText("")
        parentDropdown.setText("")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_GUARDIAN && resultCode == RESULT_OK) {
            fetchAvailableGuardians()
        }
    }
}
