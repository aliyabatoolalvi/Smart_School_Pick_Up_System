package com.finallab.smartschoolpickupsystem.Guard

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.databinding.ActivityScannedStudentListBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import com.google.firebase.Timestamp
import com.google.type.Date

import java.util.Locale

class ScannedStudentListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannedStudentListBinding
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: StudentAdapter
    private val students = mutableListOf<Student>()
    private var tts: TextToSpeech? = null

    private var guardName: String = "Unknown Guard"
    private var guardId: String = "UnknownGuardId"

    private lateinit var fetchDialog: ProgressDialog
    private lateinit var reportDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannedStudentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchDialog = ProgressDialog(this).apply {
            setMessage("Fetching student records...")
            setCancelable(false)
            show()
        }

        reportDialog = ProgressDialog(this).apply {
            setMessage("Sending pick-up activity report...")
            setCancelable(false)
        }

        val guardEmail = intent.getStringExtra("guardEmail") ?: ""
        if (guardEmail.isNotEmpty()) {
            firestore.collection("guards")
                .whereEqualTo("email", guardEmail)
                .get()
                .addOnSuccessListener { docs ->
                    if (!docs.isEmpty) {
                        val doc = docs.documents.first()
                        guardName = doc.getString("name") ?: "Unknown Guard"
                        guardId = doc.id
                    }
                }
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }

        binding.confirmbt.setOnClickListener {
            if (students.isEmpty()) {
                ToastUtil.showToast(this, "No students to report")
                return@setOnClickListener
            }

            reportDialog.show()

            val guardianName = binding.parentName.text.toString().removePrefix("Guardian: ").trim()
            val method = if (intent.hasExtra("qrCodeValue")) "QR scan" else "Manual CNIC"
            val guardianId = intent.getStringExtra("guardianId") ?: "Unknown"
            val now = Timestamp.now()

            // Define fixed school off time (1:30 PM today)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val schoolOffTime = sdf.parse(sdf.format(java.util.Date()).split(" ")[0] + " 13:30")!!

            val delayMillis = java.util.Date().time - schoolOffTime.time
            val delayMinutes = delayMillis / 60000
            val delayDescription = when {
                delayMinutes > 0 -> "$delayMinutes minutes late"
                delayMinutes < 0 -> "${-delayMinutes} minutes early"
                else -> "On time"
            }

            val reports = students.map { student ->
                val reportText = "On ${
                    SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US).format(now.toDate())
                }, ${student.Sname} was picked up by $guardianName via $method ($delayDescription)."

                hashMapOf(
                    "guardianId" to guardianId,
                    "guardianName" to guardianName,
                    "studentId" to student.studentDocId,
                    "studentName" to student.Sname,
                    "timestamp" to now,
                    "method" to method,
                    "reportText" to reportText,
                    "guardName" to guardName,
                    "guardId" to guardId,
                    "deviation" to delayDescription
                )
            }

            val batch = firestore.batch()
            val reportsCollection = firestore.collection("pick_up_activities")
            reports.forEach { data ->
                val docRef = reportsCollection.document()
                batch.set(docRef, data)
            }

            batch.commit()
                .addOnSuccessListener {
                    reportDialog.dismiss()
                    startActivity(Intent(this, ReportSuccessActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    reportDialog.dismiss()
                    ToastUtil.showToast(this, "Failed to send pickup report")
                }
        }

        adapter = StudentAdapter(students, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val manualStudentIds = intent.getStringArrayListExtra("studentIds")
        val manualGuardianName = intent.getStringExtra("guardianName")

        if (!manualStudentIds.isNullOrEmpty()) {
            binding.parentName.text = "Guardian: ${manualGuardianName ?: "Unknown"}"
            fetchStudentsByIds(manualStudentIds)
        } else {
            val qrCodeValue = intent.getStringExtra("qrCodeValue")?.trim().orEmpty()
                .replace("\n", "").replace("\r", "")
            if (qrCodeValue.isEmpty()) {
                fetchDialog.dismiss()
                ToastUtil.showToast(this, "Invalid QR code")
                finish()
                return
            }
            fetchStudentsForGuardian(qrCodeValue)
        }

        binding.btnAnnounce.setOnClickListener { announceStudents() }
    }

    private fun fetchStudentsForGuardian(qrCodeValue: String) {
        firestore.collection("guardians")
            .whereEqualTo("QRcodeData", qrCodeValue)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    fetchDialog.dismiss()
                    ToastUtil.showToast(this, "Guardian not found")
                    return@addOnSuccessListener
                }

                val doc = result.documents.first()
                val studentIds = doc.get("students") as? List<String> ?: emptyList()
                val guardianName = doc.getString("Gname") ?: "Unknown Guardian"
                binding.parentName.text = "Guardian: $guardianName"
                fetchStudentsByIds(studentIds)
            }
            .addOnFailureListener {
                fetchDialog.dismiss()
                ToastUtil.showToast(this, "Failed to find guardian")
            }
    }

    private fun fetchStudentsByIds(studentIds: List<String>) {
        if (studentIds.isEmpty()) {
            fetchDialog.dismiss()
            ToastUtil.showToast(this, "No students linked to this guardian")
            return
        }

        firestore.collection("students")
            .whereIn(FieldPath.documentId(), studentIds)
            .get()
            .addOnSuccessListener { result ->
                students.clear()
                for (doc in result) {
                    val student = doc.toObject(Student::class.java)
                    student.studentDocId = doc.id
                    students.add(student)
                }
                adapter.notifyDataSetChanged()
                fetchDialog.dismiss()
            }
            .addOnFailureListener {
                fetchDialog.dismiss()
                ToastUtil.showToast(this, "Failed to fetch students")
            }
    }

    private fun announceStudents() {
        if (students.isEmpty()) {
            ToastUtil.showToast(this, "No students to announce")
            return
        }

        for (student in students) {
            val message = "${student.Sname}, Class ${student.studentClass}, Section ${student.section}"
            tts?.speak(message, TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}






