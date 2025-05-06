package com.finallab.smartschoolpickupsystem.Guard

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.databinding.ActivityGuardAddBinding
import com.google.firebase.FirebaseException
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class GuardAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGuardAddBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var progressDialog: android.app.ProgressDialog
    private lateinit var mAuth: FirebaseAuth
    private lateinit var sharedPref: SharedPreferences
    private var verificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase App Check (debug mode)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        val isEdit = intent.getBooleanExtra("isEdit", false)

        if (isEdit) {
            binding.etName.setText(intent.getStringExtra("guardName") ?: "")
            binding.etEmail.setText(intent.getStringExtra("guardEmail") ?: "")
            binding.etPhone.setText(intent.getStringExtra("guardPhone")?.replace("+92", "0") ?: "")
            binding.etPassword.setText(intent.getStringExtra("guardPassword") ?: "")
        }

        mAuth = FirebaseAuth.getInstance()
        sharedPref = getSharedPreferences("AdminPrefs", MODE_PRIVATE)

        progressDialog = android.app.ProgressDialog(this)
        progressDialog.setMessage("Processing...")
        progressDialog.setCancelable(false)

        var isPasswordVisible = false
        binding.etPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = binding.etPassword.compoundDrawables[2]
                if (drawable != null) {
                    val drawableStart = binding.etPassword.width - binding.etPassword.paddingEnd - drawable.intrinsicWidth
                    if (event.x >= drawableStart) {
                        isPasswordVisible = !isPasswordVisible
                        val inputType = if (isPasswordVisible)
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        else
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                        binding.etPassword.inputType = inputType
                        binding.etPassword.setSelection(binding.etPassword.text.length)

                        val iconRes = if (isPasswordVisible) R.drawable.visibility else R.drawable.visibility_off
                        val icon = ContextCompat.getDrawable(this, iconRes)
                        icon?.setBounds(0, 0, icon.intrinsicWidth, icon.intrinsicHeight)
                        binding.etPassword.setCompoundDrawables(null, null, icon, null)
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        binding.sendotp.setOnClickListener {
            sendOtpToPhone()
        }

        binding.rbutton.setOnClickListener {
            val otpCode = binding.etConfirmPass.text.toString().trim()
            if (otpCode.length == 6 && verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otpCode)
                progressDialog.setMessage("Verifying OTP...")
                progressDialog.show()
                mAuth.signInWithCredential(credential).addOnSuccessListener {
                    saveGuardAndRestoreAdmin()
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendOtpToPhone() {
        val phone = binding.etPhone.text.toString().trim()

        if (phone.length != 11 || !phone.matches(Regex("^03[0-9]{9}$"))) {
            binding.etPhone.error = "Phone must be 11 digits starting with 03"
            return
        }

        val fullPhone = "+92" + phone.substring(1)
        progressDialog.setMessage("Sending OTP...")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(fullPhone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // No auto verification here; wait for manual input
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressDialog.dismiss()
                    Toast.makeText(this@GuardAddActivity, "Verification Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(vid: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = vid
                    resendToken = token
                    progressDialog.dismiss()
                    Toast.makeText(this@GuardAddActivity, "OTP sent. Enter it below.", Toast.LENGTH_SHORT).show()
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun saveGuardAndRestoreAdmin() {
        val isEdit = intent.getBooleanExtra("isEdit", false)
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val localPhone = binding.etPhone.text.toString().trim()
        val phone = "+92" + localPhone.substring(1)
        val password = binding.etPassword.text.toString().trim()

        val guardId = intent.getStringExtra("guardId") ?: db.collection("guards").document().id
        val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val guard = hashMapOf(
            "id" to guardId,
            "name" to name,
            "email" to email,
            "password" to AESEncryption.encrypt(password),
            "phone" to phone,
            "role" to "guard",
            "userId" to adminUid
        )

        db.collection("guards").document(guardId).set(guard).addOnSuccessListener {
            mAuth.signOut()

            val adminEmail = sharedPref.getString("admin_email", null)
            val adminPassword = sharedPref.getString("admin_password", null)

            if (!adminEmail.isNullOrEmpty() && !adminPassword.isNullOrEmpty()) {
                mAuth.signInWithEmailAndPassword(adminEmail, adminPassword)
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Guard ${if (isEdit) "updated" else "added"} successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, GuardListActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Guard saved. Admin login failed.", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Guard saved. Admin credentials missing.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
