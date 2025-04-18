package com.finallab.smartschoolpickupsystem.Activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finallab.smartschoolpickupsystem.DataModels.GuardianStudentCrossRef
import com.finallab.smartschoolpickupsystem.Database.GuardianStudentDao
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityAddStudentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStudentBinding
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var parentDropdown: AutoCompleteTextView
    private lateinit var classDropdown: AutoCompleteTextView

    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private var selectedGuardian: String? = null
    private var selectedGuardianCNIC: String = ""
    private var selectedGuardianId: String = ""

    private val classList = listOf(
        "Nursery", "KG", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
    )

    companion object {
        private const val REQUEST_CODE_ADD_GUARDIAN = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        parentDropdown = binding.gdropdown
        classDropdown = binding.stuClass

        fetchAvailableGuardians()
        setupClassDropdown()

        binding.regS.setOnClickListener { registerStudent() }
        binding.backButton.setOnClickListener { onBackPressed() }
        binding.addGuardianButton.setOnClickListener {
            startActivityForResult(Intent(this, AddGuardian::class.java), REQUEST_CODE_ADD_GUARDIAN)
        }
    }

    private fun setupClassDropdown() {
        val classAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, classList)
        classDropdown.apply {
            setAdapter(classAdapter)
            setOnClickListener { showDropDown() }
            setOnItemClickListener { _, _, _, _ ->
                binding.classDropdownLayout.hint = ""
            }
            setOnDismissListener {
                if (text.isEmpty()) binding.classDropdownLayout.hint = "Select Class"
            }
        }
    }

    private fun registerStudent() {
        val studentName = binding.namestudent.editText?.text.toString().trim()
        val rollNo = binding.rollnostu.editText?.text.toString().trim()
        val studentClass = classDropdown.text.toString().trim()
        val section = binding.secstu.editText?.text.toString().trim()

        if (studentName.isEmpty() || rollNo.isEmpty() || studentClass.isEmpty() || section.isEmpty()) {
            showToast("Please fill in all fields")
            return
        }

        if (studentClass !in classList) {
            showToast("Invalid class selected")
            return
        }

        val student = Student(
            studentID = 0,
            Sname = studentName,
            reg = rollNo,
            studentClass = studentClass,
            section = section,
            userId = currentUser?.uid ?: "",
            studentDocId = "")

        if (isNetworkConnected()) {
            saveStudentToFirestore(student)
        } else {
            saveStudentOffline(student)
            showToast("No internet connection. Student data saved locally.")
        }
    }

    private fun saveStudentToFirestore(student: Student) {
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("students").add(student.toMap())
            .addOnSuccessListener { documentReference ->
                student.studentDocId = documentReference.id
                documentReference.update("studentDocId", student.studentDocId)

                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(this@AddStudentActivity)
                    val studentId = db.studentDao().insertStudent(student).toInt()
                    student.studentID = studentId

                    if (selectedGuardianCNIC.isNotEmpty()) {
                        val guardian = db.guardianDao().getGuardianByCNIC(selectedGuardianCNIC)
                        guardian?.let {
                            val crossRef = GuardianStudentCrossRef(it.guardianID, studentId)
                            db.guardianStudentDao().insertGuardianStudentCrossRef(crossRef)
                            linkGuardianAndStudentInFirestore(selectedGuardianId, student.studentDocId)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        showToast("Student Registered Successfully!")
                        clearInputFields()
                    }
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                showToast("Error registering student: ${it.message}")
            }
    }

    private fun linkGuardianAndStudentInFirestore(guardianDocId: String, studentDocId: String) {
        val guardianRef = firestore.collection("guardians").document(guardianDocId)
        val studentRef = firestore.collection("students").document(studentDocId)

        firestore.runTransaction { transaction ->
            // First, do all reads
            val guardianSnapshot = transaction.get(guardianRef)
            val studentSnapshot = transaction.get(studentRef)

            // Then, do all writes
            val currentStudents = guardianSnapshot.get("students") as? List<String> ?: emptyList()
            transaction.update(guardianRef, "students", currentStudents + studentDocId)

            val currentGuardians = studentSnapshot.get("guardians") as? List<String> ?: emptyList()
            transaction.update(studentRef, "guardians", currentGuardians + guardianDocId)

            // Return nothing (the transaction will complete successfully)
            null
        }.addOnSuccessListener {
            Log.d("Firestore", "Successfully linked guardian and student")
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error linking guardian and student", e)
            showToast("Error linking accounts. Please try again.")
        }
    }

    private fun saveStudentOffline(student: Student) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@AddStudentActivity)
            val studentId = db.studentDao().insertStudent(student).toInt()

            if (selectedGuardianCNIC.isNotEmpty()) {
                val guardian = db.guardianDao().getGuardianByCNIC(selectedGuardianCNIC)
                guardian?.let {
                    val crossRef = GuardianStudentCrossRef(it.guardianID, studentId)
                    db.guardianStudentDao().insertGuardianStudentCrossRef(crossRef)
                }
            }
        }
    }

    private fun fetchAvailableGuardians() {
        currentUser?.uid?.let { userId ->
            db.collection("guardians")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val guardians = querySnapshot.documents.filter { doc ->
                        doc.getString("Gname") != null && doc.getString("CNIC") != null
                    }
                    updateGuardianDropdown(guardians)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading guardians", Toast.LENGTH_SHORT).show()
                    updateGuardianDropdown(emptyList())
                }
        }
    }

    private fun updateGuardianDropdown(guardians: List<DocumentSnapshot>) {
        val adapter = object : ArrayAdapter<DocumentSnapshot>(
            this,
            R.layout.item_guardian_layout,
            guardians
        ) {
            override fun getCount(): Int = guardians.size + 1 // +1 for "None" option

            override fun getItem(position: Int): DocumentSnapshot? {
                return if (position < guardians.size) guardians[position] else null
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                if (position == guardians.size) {
                    return createMessageView("None", parent)
                }
                return createGuardianView(guardians[position], convertView, parent)
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return getView(position, convertView, parent)
            }

            private fun createMessageView(text: String, parent: ViewGroup): View {
                val view = LayoutInflater.from(context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
                view.findViewById<TextView>(android.R.id.text1).apply {
                    this.text = text
                    setTextColor(Color.GRAY)
                }
                return view
            }

            private fun createGuardianView(
                guardian: DocumentSnapshot,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.item_guardian_layout, parent, false)

                // Add null checks for the TextViews
                view.findViewById<TextView>(R.id.tvGuardianName)?.text =
                    guardian.getString("Gname") ?: "Unknown"

                view.findViewById<TextView>(R.id.tvGuardianCNIC)?.text =
                    "CNIC: ${guardian.getString("CNIC") ?: "Not Available"}"

                return view
            }
        }

        parentDropdown.setAdapter(adapter)
        parentDropdown.threshold = 1

        parentDropdown.setOnItemClickListener { _, _, position, _ ->
            if (position == guardians.size) {
                // "None" selected
                selectedGuardian = null
                selectedGuardianCNIC = ""
                selectedGuardianId = ""
            } else {
                val guardian = guardians[position]
                selectedGuardian = guardian.getString("Gname")
                selectedGuardianCNIC = guardian.getString("CNIC") ?: ""
                selectedGuardianId = guardian.id
            }
        }

        binding.addGuardianButton.visibility = if (guardians.isEmpty()) View.VISIBLE else View.GONE
    }

    fun onTestGuardiansClick(view: View) {
        currentUser?.uid?.let { userId ->
            FirebaseFirestore.getInstance().collection("guardians")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val guardianList = snapshot.documents.joinToString("\n") { doc ->
                        "Name: ${doc["Gname"]}, CNIC: ${doc["CNIC"]}, ID: ${doc.id}"
                    }
                    AlertDialog.Builder(this)
                        .setTitle("Guardians in Database")
                        .setMessage(if (guardianList.isEmpty()) "No guardians found" else guardianList)
                        .setPositiveButton("OK", null)
                        .show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_GUARDIAN && resultCode == RESULT_OK) {
            fetchAvailableGuardians()
        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetwork?.let { network ->
            cm.getNetworkCapabilities(network)?.let { capabilities ->
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            } ?: false
        } ?: false
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun clearInputFields() {
        binding.namestudent.editText?.setText("")
        binding.rollnostu.editText?.setText("")
        classDropdown.setText("")
        binding.secstu.editText?.setText("")
        parentDropdown.setText("")
        selectedGuardian = null
        selectedGuardianCNIC = ""
    }
}