package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.DataModels.GuardianStudentCrossRef
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.OnDataChangedListener
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.Recycler.OnItemDeletedListener
import com.finallab.smartschoolpickupsystem.Recycler.RecyclerViewAdapter
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.Utilities
import com.finallab.smartschoolpickupsystem.databinding.ActivityStudentDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudentDetails : AppCompatActivity() {
    private lateinit var binding: ActivityStudentDetailsBinding
    private lateinit var student: Student
    private val firestore = FirebaseFirestore.getInstance()
    private val guardiansList = mutableListOf<Guardian>()
    private lateinit var adapter: RecyclerViewAdapter
    private var onItemDeletedListener: OnItemDeletedListener? = null
    private var dataChangedListener: OnDataChangedListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val studentDocId = intent.getStringExtra("studentDocumentID")
        if (studentDocId.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid student document ID", Toast.LENGTH_SHORT).show()
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

        fetchStudentFromFirestore(studentDocId)

//        binding.addG.setOnClickListener {
//            val intent = Intent(this, AddGuardian::class.java).apply {
//                putExtra("studentDocumentID", student.studentDocId)
//            }
//            startActivity(intent)
//        }
        binding.addG.setOnClickListener {
            val options = arrayOf("Add Existing Guardian", "Add New Guardian")

            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Add Guardian")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> showExistingGuardiansDialog()  // New function
                    1 -> {
                                val intent = Intent(this, AddGuardian::class.java).apply {
                            putExtra("studentDocumentID", student.studentDocId)
                        }
                        startActivity(intent)
                    }
                }
            }
            builder.show()
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    private fun showExistingGuardiansDialog() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        firestore.collection("guardians")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { result ->

                val sortedDocuments = result.documents.sortedBy { it.getString("Gname") ?: "" }

                val guardianNames = mutableListOf<String>()
                val guardianDocIds = mutableListOf<String>()
                val guardianSet = mutableSetOf<String>() // For uniqueness

                for (doc in sortedDocuments) {
                    val guardianName = doc.getString("Gname") ?: "Unknown"
                    val guardianCNIC = doc.getString("CNIC") ?: "No CNIC"
                    val displayText = "$guardianName (CNIC: $guardianCNIC)"

                    if (guardianSet.add(displayText)) {
                        guardianNames.add(displayText)
                        guardianDocIds.add(doc.id)
                    }
                }

                if (guardianNames.isEmpty()) {
                    Toast.makeText(this, "No guardians found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                firestore.collection("students").document(student.studentDocId)
                    .get()
                    .addOnSuccessListener { studentDoc ->
                        val alreadyAttachedGuardianIds = studentDoc.get("guardians") as? List<String> ?: emptyList()

                        val availableGuardianNames = mutableListOf<String>()
                        val availableGuardianDocIds = mutableListOf<String>()

                        for (i in guardianDocIds.indices) {
                            if (!alreadyAttachedGuardianIds.contains(guardianDocIds[i])) {
                                availableGuardianNames.add(guardianNames[i])
                                availableGuardianDocIds.add(guardianDocIds[i])
                            }
                        }

                        if (availableGuardianNames.isEmpty()) {
                            MaterialAlertDialogBuilder(this)
                                .setTitle("No Available Guardians")
                                .setMessage("All guardians are already linked to this student.")
                                .setPositiveButton("OK", null)
                                .show()
                            return@addOnSuccessListener
                        }

                        // 🔥 Now showing custom dialog with searchBox + searchButton
                        val dialogView = layoutInflater.inflate(R.layout.dialog_searchable_list, null)
                        val searchBox = dialogView.findViewById<EditText>(R.id.searchBox)
                        val searchButton = dialogView.findViewById<ImageButton>(R.id.searchButton)
                        val listView = dialogView.findViewById<ListView>(R.id.listView)

                        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, availableGuardianNames)
                        listView.adapter = adapter
                        listView.choiceMode = ListView.CHOICE_MODE_SINGLE

                        var selectedIndex = -1

                        listView.setOnItemClickListener { _, _, position, _ ->
                            selectedIndex = position
                        }

                        // 🛠 Search when user presses enter
                        searchBox.setOnEditorActionListener { _, actionId, _ ->
                            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                                actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {

                                val query = searchBox.text.toString().trim()
                                if (query.isNotEmpty()) {
                                    val filteredNames = availableGuardianNames.filter { it.contains(query, ignoreCase = true) }
                                    adapter.clear()
                                    adapter.addAll(filteredNames)
                                    adapter.notifyDataSetChanged()
                                }
                                hideKeyboard()
                                true
                            } else {
                                false
                            }
                        }

                        // 🛠 Search when user clicks Search button
                        searchButton.setOnClickListener {
                            val query = searchBox.text.toString().trim()
                            if (query.isNotEmpty()) {
                                val filteredNames = availableGuardianNames.filter { it.contains(query, ignoreCase = true) }
                                adapter.clear()
                                adapter.addAll(filteredNames)
                                adapter.notifyDataSetChanged()
                            }
                            hideKeyboard()
                        }

                        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
                        builder.setView(dialogView)
                        builder.setTitle("Select Guardian")

                        builder.setPositiveButton("Attach") { _, _ ->
                            if (selectedIndex >= 0 && selectedIndex < availableGuardianDocIds.size) {
                                val selectedDocId = availableGuardianDocIds[selectedIndex]
                                attachGuardian(selectedDocId)
                            } else {
                                Toast.makeText(this, "Please select a guardian", Toast.LENGTH_SHORT).show()
                            }
                        }
                        builder.setNegativeButton("Cancel", null)

                        val dialog = builder.create()
                        dialog.show()

                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to load student info", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load guardians", Toast.LENGTH_SHORT).show()
            }
    }



    private fun fetchStudentFromFirestore(studentDocId: String) {
        firestore.collection("students").document(studentDocId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fetchedStudent = document.toObject(Student::class.java)
                    if (fetchedStudent != null) {
                        fetchedStudent.studentDocId = document.id

                        lifecycleScope.launch {
                            val db = AppDatabase.getDatabase(this@StudentDetails)
                            db.studentDao().insertStudent(fetchedStudent)

                            student = fetchedStudent
                            displayStudentDetails()
                            loadGuardians()
                        }
                    }
                } else {
                    // Firestore fetch failed — fallback to Room
                    loadStudentFromRoom(studentDocId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch student online", Toast.LENGTH_SHORT).show()
                loadStudentFromRoom(studentDocId)
            }
    }

    private fun loadStudentFromRoom(studentDocId: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@StudentDetails)
            val localStudent = db.studentDao().getStudentByDocId(studentDocId)

            if (localStudent == null) {
                Toast.makeText(this@StudentDetails, "Student not found offline", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }

            student = localStudent
            displayStudentDetails()
            loadGuardians()
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
                        dataChangedListener?.onDataUpdated()
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
                        lifecycleScope.launch(Dispatchers.IO) {
                            val db = AppDatabase.getDatabase(this@StudentDetails)

                            // Clear local guardians of this student
                            db.guardianStudentDao().deleteCrossRefsByStudentId(student.studentID)

                            guardiansList.clear()
                            for (doc in guardianDocs) {
                                val guardian = doc.toObject(Guardian::class.java).apply {
                                    guardianDocId = doc.id
                                }
                                guardiansList.add(guardian)

                                // Save guardian locally
                                db.guardianDao().insertGuardian(guardian)
                                db.guardianStudentDao().insertGuardianStudentCrossRef(
                                    GuardianStudentCrossRef(
                                        guardian.guardianID,
                                        student.studentID
                                    )
                                )
                            }

                            withContext(Dispatchers.Main) {
                                adapter.notifyDataSetChanged()
                            }
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
    private fun attachGuardian(selectedDocId: String) {
        val studentRef = firestore.collection("students").document(student.studentDocId)
        val guardianRef = firestore.collection("guardians").document(selectedDocId)

        firestore.runTransaction { transaction ->

            val studentSnapshot = transaction.get(studentRef)
            val guardianSnapshot = transaction.get(guardianRef)

            val currentGuardians = studentSnapshot.get("guardians") as? List<String> ?: emptyList()

            if (currentGuardians.contains(selectedDocId)) {
                throw Exception("Guardian is already attached to this student!")
            }

            transaction.update(studentRef, "guardians", currentGuardians + selectedDocId)

            val currentStudents = guardianSnapshot.get("students") as? List<String> ?: emptyList()
            transaction.update(guardianRef, "students", currentStudents + student.studentDocId)

        }.addOnSuccessListener {
            Toast.makeText(this, "Guardian attached successfully!", Toast.LENGTH_SHORT).show()
            loadGuardians()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }







}
