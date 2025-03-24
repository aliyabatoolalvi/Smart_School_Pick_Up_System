package com.finallab.smartschoolpickupsystem.Activities

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityAddStudentBinding
import com.google.firebase.auth.FirebaseAuth
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

    private val guardianList = mutableListOf<String>()
    private val selectedGuardians = mutableListOf<String>()

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

    }

    /** Fetch guardians from Firestore and populate dropdown */
    private fun fetchAvailableGuardians() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let {
            firestore.collection("guardians")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    guardianList.clear()
                    guardianList.add("None")

                    for (document in documents) {
                        document.getString("Gname")?.let {
                            guardianList.add(it)
                        }
                    }

                    if (guardianList.isEmpty()) guardianList.add("No Guardians Found")

                    setupGuardianDropdown()
                }
                .addOnFailureListener {
                    showToast("Failed to fetch guardians.")
                }
        }
    }

    /** Set up the guardian dropdown with proper behavior */
    /** Set up the guardian dropdown with proper behavior */
    private fun setupGuardianDropdown() {
        val guardianAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, guardianList)
        parentDropdown.apply {
            setAdapter(guardianAdapter)
            setOnClickListener { showDropDown() }

            // Handle selection from dropdown
            setOnItemClickListener { _, _, position, _ ->
                val selectedGuardian = guardianList[position]

                // Show "Add Guardian" button if "No Guardians Found" or "None" is selected
                binding.addGuardianButton.visibility = if (selectedGuardian == "No Guardians Found" || selectedGuardian == "None") {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                // Add valid guardian to the selected list
                if (selectedGuardian !in selectedGuardians && selectedGuardian != "No Guardians Found" && selectedGuardian != "None") {
                    selectedGuardians.add(selectedGuardian)
                    setText(selectedGuardians.joinToString(", "), false)
                }

                // Clear hint on selection
                binding.parentDropdownLayout.hint = ""
            }

            // Restore hint if no selection is made
            setOnDismissListener {
                if (text.isEmpty()) {
                    binding.parentDropdownLayout.hint = "Select Guardian"
                }
            }
        }

        // Ensure the button is hidden by default
        binding.addGuardianButton.visibility = View.GONE

        // Navigate to AddGuardian Activity
        binding.addGuardianButton.setOnClickListener {
            val intent = Intent(this@AddStudentActivity, AddGuardian::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_GUARDIAN)
        }

        // Ensure dropdown updates when new data arrives
        guardianAdapter.notifyDataSetChanged()
    }


    /** Set up the class dropdown */
    /** Set up the class dropdown */
    private fun setupClassDropdown() {
        val classAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, classList)
        classDropdown.apply {
            setAdapter(classAdapter)
            setOnClickListener { showDropDown() }

            // Hide hint when an item is selected
            setOnItemClickListener { _, _, _, _ ->
                binding.classDropdownLayout.hint = ""
            }

            // Show hint again if no class is selected
            setOnDismissListener {
                if (text.isEmpty()) {
                    binding.classDropdownLayout.hint = "Select Class"
                }
            }
        }

        // Set initial hint
        binding.classDropdownLayout.hint = "Select Class"
    }

    /** Handles result from AddGuardian Activity */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_GUARDIAN && resultCode == RESULT_OK) {
            lifecycleScope.launch {
                fetchAvailableGuardians()
                setupGuardianDropdown() // Ensure refresh after returning
            }
        }
    }


    /** Validate inputs and register the student */
    private fun registerStudent() {
        val studentName = binding.namestudent.editText?.text.toString().trim()
        val rollNo = binding.rollnostu.editText?.text.toString().trim()
        val studentClass = classDropdown.text.toString().trim()
        val section = binding.secstu.editText?.text.toString().trim()

        if (studentName.isEmpty() || rollNo.isEmpty() || studentClass.isEmpty() || section.isEmpty() || selectedGuardians.isEmpty()) {
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
            firestoreId = "",
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            studentDocId = "",
            guardians = selectedGuardians
        )

        if (isNetworkConnected()) {
            checkStudentExists(student)
        } else {
            saveStudentOffline(student)
            showToast("No internet connection. Student data saved locally.")
        }
    }

    /** Save student to Firestore and update Room DB */
    private fun saveStudentToFirestore(student: Student) {
        binding.progressBar.visibility = View.VISIBLE

        firestore.collection("students").add(student.toMap())
            .addOnSuccessListener { documentReference ->
                student.studentDocId = documentReference.id
                documentReference.update("studentDocId", student.studentDocId)

                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getDatabase(this@AddStudentActivity).studentDao().insertStudent(student)
                    linkStudentWithGuardians(student.studentDocId, selectedGuardians)

                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        showToast("Student Registered!")
                        clearInputFields()
                    }
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                showToast("Error registering student: ${it.message}")
            }
    }

    /** Link student with their guardians in Firestore */
    private fun linkStudentWithGuardians(studentId: String, guardians: List<String>) {
        lifecycleScope.launch {
            val batch = firestore.batch()
            val guardianReferences = mutableListOf<Map<String, String>>() // Store guardian details (ID + name)

            guardians.forEach { guardianName ->
                firestore.collection("guardians")
                    .whereEqualTo("Gname", guardianName)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            val guardianId = document.id

                            // Add student ID to guardian document
                            batch.update(document.reference, "studentIds", FieldValue.arrayUnion(studentId))

                            // Collect guardian's ID and name
                            guardianReferences.add(mapOf("id" to guardianId, "name" to guardianName))
                        }

                        // Commit batch after processing all guardians
                        batch.commit()

                        // Update student with linked guardian IDs and names
                        updateStudentWithGuardianReferences(studentId, guardianReferences)
                    }
                    .addOnFailureListener { e ->
                        showToast("Error linking guardian: ${e.message}")
                    }
            }
        }
    }
    private fun updateStudentWithGuardianReferences(studentId: String, guardianReferences: List<Map<String, String>>) {
        firestore.collection("students")
            .document(studentId)
            .update("guardians", guardianReferences)
            .addOnSuccessListener {
                showToast("Student linked with guardians successfully!")
            }
            .addOnFailureListener { e ->
                showToast("Error updating student with guardians: ${e.message}")
            }
    }


    /** Save student data offline if no network */
    private fun saveStudentOffline(student: Student) {
        lifecycleScope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase(this@AddStudentActivity).studentDao().insertStudent(student)
        }
    }
    private fun checkStudentExists(student: Student) {
        firestore.collection("students")
            .whereEqualTo("Sname", student.Sname)
            .whereEqualTo("reg", student.reg)
            .whereEqualTo("userId", student.userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    saveStudentToFirestore(student) // Save only if student doesn't exist
                } else {
                    showToast("Student already exists!")
                    binding.progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                showToast("Error checking student: ${it.message}")
                binding.progressBar.visibility = View.GONE
            }
    }


    /** Sync offline data when network is available */
    /** Sync offline data only when the device gets online */
//    private fun syncOfflineData() {
//        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        // Check if the current network is available (compatible with API 23+)
//        val isConnected = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            cm.activeNetwork?.let { network ->
//                cm.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
//            } ?: false
//        } else {
//            @Suppress("DEPRECATION")
//            cm.activeNetworkInfo?.isConnected == true
//        }
//
//        if (isConnected) {
//            lifecycleScope.launch(Dispatchers.IO) {
//                val studentDao = AppDatabase.getDatabase(this@AddStudentActivity).studentDao()
//                val offlineStudents = studentDao.getAllStudents()
//
//                if (offlineStudents.isNotEmpty()) {
//                    offlineStudents.forEach { student ->
//                        saveStudentToFirestore(student)
//
//                        // Safely delete after successful sync
//                        withContext(Dispatchers.IO) {
//                            studentDao.deleteStudent(student)
//                        }
//                    }
//
//                    // Show success message on the main thread
//                    withContext(Dispatchers.Main) {
//                        showToast("Offline data synced successfully!")
//                    }
//                }
//            }
//        } else {
//            showToast("No internet connection. Data will sync when online.")
//        }
//    }


    /** Check if the device is connected to the internet */
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.getNetworkCapabilities(cm.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                cm.getNetworkCapabilities(cm.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    }

    /** Helper function to show a toast message */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /** Clear input fields after successful registration */
    private fun clearInputFields() {
        binding.namestudent.editText?.setText("")
        binding.rollnostu.editText?.setText("")
        classDropdown.setText("")
        binding.secstu.editText?.setText("")
        parentDropdown.setText("")
        selectedGuardians.clear()
    }
}
