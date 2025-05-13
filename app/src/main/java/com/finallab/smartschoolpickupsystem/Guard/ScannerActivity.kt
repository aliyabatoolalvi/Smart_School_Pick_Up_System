package com.finallab.smartschoolpickupsystem.Guard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.finallab.smartschoolpickupsystem.databinding.ActivityScannerBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding
    private val firestore = FirebaseFirestore.getInstance()
    private var guardName: String = ""
    private var guardEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        guardEmail = intent.getStringExtra("guardEmail") ?: ""
        guardName = intent.getStringExtra("guardName") ?: ""

        binding.guardName.text = "Guard name: $guardName"
        binding.guardEmail.text = "Email: $guardEmail"

        binding.mp.setOnClickListener {
            val intent = Intent(this, ManualPickupActivity::class.java)
            intent.putExtra("guardEmail", guardEmail)
            intent.putExtra("guardName", guardName)
            startActivity(intent)
        }

        registerUiListener()
    }

    private fun registerUiListener() {
        binding.sb.setOnClickListener {
            scannerLauncher.launch(
                ScanOptions()
                    .setPrompt("Scan QR Code")
                    .setOrientationLocked(true)
                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    .setCaptureActivity(PortraitCaptureActivity::class.java)
            )
        }
    }

    private val scannerLauncher =
        registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
            if (!result.contents.isNullOrBlank()) {
                val qrCodeValue = result.contents.trim()

                firestore.collection("guardians")
                    .whereEqualTo("QRcodeData", qrCodeValue)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.isEmpty) {
                            ToastUtil.showToast(this, "Guardian not found")
                        } else {
                            val guardianDoc = querySnapshot.documents.first()
                            val guardianId = guardianDoc.id

                            val intent = Intent(this, ScannedStudentListActivity::class.java)
                            intent.putExtra("qrCodeValue", qrCodeValue)
                            intent.putExtra("guardianId", guardianId)
                            intent.putExtra("guardName", guardName)
                            intent.putExtra("guardEmail", guardEmail)
                            startActivity(intent)
                        }
                    }
                    .addOnFailureListener {
                        ToastUtil.showToast(this, "Error checking guardian")
                    }
            } else {
                ToastUtil.showToast(this, "Scan cancelled or invalid")
            }
        }
}


