package com.finallab.smartschoolpickupsystem.Activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finallab.smartschoolpickupsystem.PickUpReport
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.ReportAdapter
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AdminReport : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var exportPdfButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var barChart: BarChart
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var filterSpinner: Spinner
    private lateinit var db: FirebaseFirestore
    private var emptyText: TextView? = null
    private var allReports: List<PickUpReport> = ArrayList()

    private val pdfSaveLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri: Uri? = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            savePdfToUri(uri)
        } else {
            Toast.makeText(this, "Export canceled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_report)

        recyclerView = findViewById(R.id.recyclerViewReports)
        exportPdfButton = findViewById(R.id.btnExportPdf)
        progressBar = findViewById(R.id.progressBarReports)
        barChart = findViewById(R.id.barChart)
        filterSpinner = findViewById(R.id.methodFilter)
        emptyText = findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(this)
        reportAdapter = ReportAdapter(ArrayList(), this,true)
        recyclerView.adapter = reportAdapter

        db = FirebaseFirestore.getInstance()

        setupSpinner()
        exportPdfButton.setOnClickListener { launchPdfExportIntent() }

        loadAllReports()
    }
    private fun launchPdfExportIntent() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "AdminPickupReport.pdf")
        }
        pdfSaveLauncher.launch(intent)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(this, R.array.filter_methods, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilter(parent.getItemAtPosition(position).toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun applyFilter(method: String) {
        val filtered = when (method) {
            "QR scan" -> allReports.filter { it.reportText?.contains("QR scan", true) == true }
            "Manual entry" -> allReports.filter { it.reportText?.contains("Manual CNIC", true) == true }
            else -> allReports
        }
        reportAdapter.updateReports(filtered)
    }

    private fun loadAllReports() {
        progressBar.visibility = View.VISIBLE
        db.collection("pick_up_activities")
            .orderBy("timestamp", Query.Direction.DESCENDING) // 🔄 updated field
            .get()
            .addOnSuccessListener { snapshot ->
                progressBar.visibility = View.GONE
                val reports = snapshot.documents.mapNotNull { it.toObject(PickUpReport::class.java) }
                allReports = reports
                if (reports.isEmpty()) {
                    showEmptyState("No reports available")
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyText?.visibility = View.GONE
                    reportAdapter.updateReports(reports)
                    generateBarChart(reports)
                    detectLatePickups(reports)
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                showEmptyState("Error loading reports: ${it.message}")
            }
    }

    private fun generateBarChart(reports: List<PickUpReport>) {
        val manualCounts = TreeMap<String, Int>()
        val qrCounts = TreeMap<String, Int>()
        val format = SimpleDateFormat("EEE", Locale.getDefault())

        for (report in reports) {
            val day = report.timestamp?.toDate()?.let { format.format(it) } ?: continue
            when {
                report.method.equals("Manual CNIC", true) -> {
                    manualCounts[day] = (manualCounts[day] ?: 0) + 1
                }
                report.method.equals("QR scan", true) -> {
                    qrCounts[day] = (qrCounts[day] ?: 0) + 1
                }
            }
        }

        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val manualEntries = mutableListOf<BarEntry>()
        val qrEntries = mutableListOf<BarEntry>()

        days.forEachIndexed { index, day ->
            manualEntries.add(BarEntry(index.toFloat(), manualCounts[day]?.toFloat() ?: 0f))
            qrEntries.add(BarEntry(index.toFloat(), qrCounts[day]?.toFloat() ?: 0f))
        }

        val manualDataSet = BarDataSet(manualEntries, "Manual Pickups").apply {
            color = Color.parseColor("#FF7043")
        }
        val qrDataSet = BarDataSet(qrEntries, "QR Pickups").apply {
            color = Color.parseColor("#42A5F5")
        }

        val groupSpace = 0.3f
        val barSpace = 0.05f
        val barWidth = 0.3f

        val barData = BarData(manualDataSet, qrDataSet).apply {
            this.barWidth = barWidth
        }

        // Setup XAxis
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelCount = days.size
        xAxis.valueFormatter = IndexAxisValueFormatter(days)

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = true

        // Proper grouping
        barChart.data = barData
        barChart.xAxis.axisMinimum = 0f
        barChart.xAxis.axisMaximum = 0f + barData.getGroupWidth(groupSpace, barSpace) * days.size
        barChart.groupBars(0f, groupSpace, barSpace)
        barChart.invalidate()
    }


    private fun detectLatePickups(reports: List<PickUpReport>) {
        val format = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())

        for (report in reports) {
            try {
                val date = report.timestamp?.toDate() ?: continue
                val cal = Calendar.getInstance()
                cal.time = date
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                if (hour >= 16) {
                    println("Late pickup detected: ${report.studentName} at $hour:00")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun savePdfToUri(uri: Uri) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        var y = 50
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        paint.textSize = 14f
        canvas.drawText("Admin Pickup Report", 50f, y.toFloat(), paint)
        y += 30
        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        for (report in allReports) {
            val date = report.timestamp?.toDate() ?: Date()
            val line = report.reportText ?: "${report.studentName} - ${format.format(date)}"
            canvas.drawText(line, 50f, y.toFloat(), paint)
            y += 25
            if (y > 800) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50
            }
        }

        pdfDocument.finishPage(page)

        try {
            val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                pdfDocument.writeTo(outputStream)
                outputStream.close()
                Toast.makeText(this, "PDF saved to chosen location", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Unable to open file location", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }

//    private fun insertDummyPickup() {
//        val pickup = hashMapOf(
//            "studentName" to "Ali Khan",
//            "guardianName" to "Mr. Ahmed",
//            "studentId" to "studentDocId123",
//            "guardianId" to "guardianDocId456",
//            "guardianUID" to "authUid_ABC123",
//            "CNIC" to "35201-1234567-1",
//            "pickUpTime" to com.google.firebase.Timestamp.now(),
//            "reportText" to "On May 2, 2025 at 2:30 PM, Ali Khan was picked up by Mr. Ahmed using QR scan.",
//            "method" to "QR scan"
//        )
//        db.collection("pick_up_activities")
//            .add(pickup)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Dummy pickup logged", Toast.LENGTH_SHORT).show()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

    private fun showEmptyState(message: String) {
        recyclerView.visibility = View.GONE
        emptyText?.text = message
        emptyText?.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
