package com.finallab.smartschoolpickupsystem.Guard

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.finallab.smartschoolpickupsystem.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class GuardPhoneLoginActivity : AppCompatActivity() {

    private lateinit var phoneInput: EditText
    private lateinit var otpInput: EditText
    private lateinit var sendOtpBtn: Button
    private lateinit var verifyOtpBtn: Button

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var loadingDialog: androidx.appcompat.app.AlertDialog
    private lateinit var rootView: LinearLayout

    private var resendCooldown: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guard_phone_login)

        // App Check for testing
        System.setProperty("debug.firebase.app.check.debug_token", "9A6AD159-37E5-4291-B03C-B42637231DDF")
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        phoneInput = findViewById(R.id.phoneInput)
        otpInput = findViewById(R.id.otpInput)
        sendOtpBtn = findViewById(R.id.sendOtpBtn)
        verifyOtpBtn = findViewById(R.id.verifyOtpBtn)
        rootView = findViewById(R.id.rootLayout)

        setupLoadingDialog()

        sendOtpBtn.setOnClickListener {
            val rawInput = phoneInput.text.toString().trim()
            val formattedPhone = normalizePhone(rawInput)

            if (formattedPhone == null) {
                showCustomToast("Invalid phone number. Enter 0300... or +92300...")
            } else {
                sendVerificationCode(formattedPhone, false)
            }
        }

        sendOtpBtn.setOnLongClickListener {
            val rawInput = phoneInput.text.toString().trim()
            val formattedPhone = normalizePhone(rawInput)

            if (formattedPhone == null) {
                showCustomToast("Invalid phone number format.")
            } else if (resendToken != null) {
                sendVerificationCode(formattedPhone, true)
            } else {
                showCustomToast("Send OTP first before resending")
            }
            true
        }

        verifyOtpBtn.setOnClickListener {
            val code = otpInput.text.toString().trim()
            if (code.length == 6 && verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithCredential(credential)
            } else {
                showCustomToast("Enter valid 6-digit OTP")
            }
        }
    }

    private fun normalizePhone(input: String): String? {
        val trimmed = input.replace(" ", "")
        return when {
            trimmed.startsWith("+92") && trimmed.length == 13 -> trimmed
            trimmed.startsWith("03") && trimmed.length == 11 -> "+92" + trimmed.substring(1)
            else -> null
        }
    }

    private fun sendVerificationCode(phoneNumber: String, isResend: Boolean) {
        loadingDialog.show()

        val optionsBuilder = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(phoneAuthCallbacks)

        if (isResend && resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken!!)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            showCustomToast("Verification completed. Please enter OTP manually.")
        }

        override fun onVerificationFailed(e: FirebaseException) {
            loadingDialog.dismiss()
            showCustomToast("Verification Failed: ${e.message}")
        }

        override fun onCodeSent(vid: String, token: PhoneAuthProvider.ForceResendingToken) {
            loadingDialog.dismiss()
            verificationId = vid
            resendToken = token
            showCustomToast("OTP sent successfully.")
            sendOtpBtn.isEnabled = false
            startResendCooldown()
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        loadingDialog.show()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                loadingDialog.dismiss()
                if (task.isSuccessful) {
                    val formattedPhone = normalizePhone(phoneInput.text.toString().trim()) ?: return@addOnCompleteListener
                    db.collection("guards")
                        .whereEqualTo("phone", formattedPhone)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty) {
                                val name = snapshot.documents[0].getString("name") ?: "Guard"
                                showCustomToast("Welcome Guard: $name")
                                val intent = Intent(this, ScannerActivity::class.java)
                                intent.putExtra("guardPhone", formattedPhone)
                                startActivity(intent)

                                finish()
                            } else {
                                showCustomToast("No guard registered with this number")
                            }
                        }
                        .addOnFailureListener {
                            showCustomToast("Error accessing guard data: ${it.message}")
                        }
                } else {
                    showCustomToast("Login failed: ${task.exception?.message}")
                }
            }
    }

    private fun setupLoadingDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_loader, null)
        loadingDialog = MaterialAlertDialogBuilder(this)
            .setView(view)
            .setCancelable(false)
            .create()
    }

    private fun showCustomToast(message: String) {
        val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.light_background, theme))
            .setTextColor(resources.getColor(R.color.black, theme))
        val snackbarTextView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackbarTextView.maxLines = 5
        snackbar.show()
    }

    private fun startResendCooldown() {
        resendCooldown?.cancel()
        resendCooldown = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                sendOtpBtn.text = "Resend in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                sendOtpBtn.text = "Send OTP"
                sendOtpBtn.isEnabled = true
            }
        }.start()
    }

    override fun onDestroy() {
        resendCooldown?.cancel()
        super.onDestroy()
    }
}
