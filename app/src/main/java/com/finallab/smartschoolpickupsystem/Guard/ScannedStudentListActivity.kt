package com.finallab.smartschoolpickupsystem.Guard

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.databinding.ActivityScannedStudentListBinding
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ScannedStudentListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannedStudentListBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: StudentAdapter
    private val students = mutableListOf<Student>()
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannedStudentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize TextToSpeech
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }

        val rawQR = intent.getStringExtra("qrCodeValue") ?: ""
        val qrCodeValue = rawQR.trim().replace("\n", "").replace("\r", "")

        if (qrCodeValue.isEmpty()) {
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("QR_SCAN", "Scanned QRcodeData: '$qrCodeValue'")

        adapter = StudentAdapter(students, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        fetchStudentsForGuardian(qrCodeValue)

        // Announce button click
        binding.btnAnnounce.setOnClickListener {
            announceStudents()
        }
    }

    private fun fetchStudentsForGuardian(qrCodeValue: String) {
        firestore.collection("guardians")
            .whereEqualTo("QRcodeData", qrCodeValue)
            .get()
            .addOnSuccessListener { result ->
                Log.d("QR_SCAN", "Query matched ${result.size()} guardian(s)")

                if (result.isEmpty) {
                    Toast.makeText(this, "Guardian not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val doc = result.documents.first()
                val studentIds = doc.get("students") as? List<String> ?: emptyList()

                // ✅ Get and display guardian name
                val guardianName = doc.getString("Gname") ?: "Unknown Guardian"
                binding.parentName.text = "Guardian: $guardianName"

                val guardianPhone = doc.getString("number") ?: ""
                Log.d("QR_SCAN", "Guardian name: $guardianName, phone: $guardianPhone")
                Log.d("QR_SCAN", "Linked student IDs: $studentIds")

                fetchStudentsByIds(studentIds)
            }
            .addOnFailureListener { e ->
                Log.e("QR_SCAN", "Firestore query failed", e)
                Toast.makeText(this, "Failed to find guardian", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchStudentsByIds(studentIds: List<String>) {
        if (studentIds.isEmpty()) {
            Toast.makeText(this, "No students linked to this guardian", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("students")
            .whereIn(FieldPath.documentId(), studentIds)
            .get()
            .addOnSuccessListener { result ->
                students.clear()
                for (docSnap in result) {
                    val student = docSnap.toObject(Student::class.java)
                    student.studentDocId = docSnap.id
                    students.add(student)
                }
                adapter.notifyDataSetChanged()
                Log.d("QR_SCAN", "Loaded ${students.size} student(s)")
            }
            .addOnFailureListener { e ->
                Log.e("QR_SCAN", "Failed to fetch students", e)
                Toast.makeText(this, "Failed to fetch students", Toast.LENGTH_SHORT).show()
            }
    }

    private fun announceStudents() {
        if (students.isEmpty()) {
            Toast.makeText(this, "No students to announce", Toast.LENGTH_SHORT).show()
            return
        }

        for (student in students) {
            val message = "${student.Sname}, Class ${student.studentClass}, Section ${student.section}"
            tts?.speak(message, TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.stop()
        tts?.shutdown()
    }
}





