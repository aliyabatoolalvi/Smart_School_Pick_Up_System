package com.finallab.smartschoolpickupsystem.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.ActivityGuardianDetailsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuardianDetails : AppCompatActivity() {

    private lateinit var binding: ActivityGuardianDetailsBinding
    private var guardian: Guardian? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardianDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBackButton()
        val guardianId = intent.getIntExtra("id", -1)
        if (guardianId == -1) {
            showErrorAndExit("Invalid guardian ID")
            return
        }

        loadGuardianDetails(guardianId)
    }

    // ✅ Set up the back button click listener
    private fun setupBackButton() {
        binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    // ✅ Load guardian details from Room Database
    private fun loadGuardianDetails(guardianId: Int) {
        lifecycleScope.launch {
            try {
                val guardianResult = withContext(Dispatchers.IO) {
                    AppDatabase.getDatabase(this@GuardianDetails).guardianDao().getGuardianById(guardianId)
                }

                guardian = guardianResult // Assigning the result properly

                guardian?.let {
                    displayGuardianDetails(it)
                } ?: showErrorAndExit("Guardian not found")
            } catch (e: Exception) {
                Log.e("GuardianDetails", "Error fetching guardian: ${e.message}")
                showErrorAndExit("Error: Unable to fetch guardian details")
            }
        }
    }


    // ✅ Display the guardian's information
    private fun displayGuardianDetails(guardian: Guardian) {
        with(binding) {
            textView5.text = "Name: \n${guardian.Gname}"
            textView7.text = "Phone: \n${guardian.number}"
            textView8.text = "CNIC: \n${guardian.CNIC}"
            textView9.text = "Email: \n${guardian.Email}"
        }

        setupClickableContact(binding.textView7, guardian.number, "tel:")
        setupClickableContact(binding.textView9, guardian.Email, "mailto:")

        loadQRCode(guardian.QRcodeBase64)
    }

    // Set up click listeners for phone and email
    private fun setupClickableContact(textView: TextView, contactInfo: String?, prefix: String) {
        contactInfo?.let {
            textView.setTextColor(ContextCompat.getColor(this, R.color.black))
            textView.setOnClickListener { openContactIntent(prefix, contactInfo) } // Pass the string explicitly
        }
    }

    // ✅ Open dialer or email app
    private fun openContactIntent(prefix: String, contactInfo: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("$prefix$contactInfo")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("GuardianDetails", "Error opening contact intent: ${e.message}")
            showToast("Error: Unable to open contact")
        }
    }

    // ✅ Decode and display QR code if available
    private fun loadQRCode(qrCodeBase64: String?) {
        if (qrCodeBase64.isNullOrEmpty()) {
            showToast("Error: No QR code found")
            return
        }

        val qrCodeBitmap = decodeBase64ToBitmap(qrCodeBase64)
        if (qrCodeBitmap != null) {
            binding.guardianQrCode.setImageBitmap(qrCodeBitmap)
        } else {
            Log.e("GuardianDetails", "Failed to decode QR code")
            showToast("Error: Invalid QR code data")
        }
    }

    // ✅ Decode Base64 string to Bitmap
    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            Log.e("GuardianDetails", "Error decoding Base64: ${e.message}")
            null
        }
    }

    // ✅ Show error message and close activity
    private fun showErrorAndExit(message: String) {
        showToast(message)
        finish()
    }

    // ✅ Display a toast message
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
