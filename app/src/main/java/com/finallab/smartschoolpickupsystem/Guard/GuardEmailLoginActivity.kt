package com.finallab.smartschoolpickupsystem.Guard

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.finallab.smartschoolpickupsystem.R

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.finallab.smartschoolpickupsystem.Guard.ScannerActivity
import com.finallab.smartschoolpickupsystem.databinding.ActivityGuardEmailLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class GuardEmailLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardEmailLoginBinding

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var loadingDialog: AlertDialog

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardEmailLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        MaterialAlertDialogBuilder(this)
            .setTitle("Login Method")
            .setMessage("Please enter your email or phone number below to continue.")
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Help") { dialog, _ ->
                Toast.makeText(this, "Use phone (03xxxxxxxxx) or email like abc@gmail.com", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .setBackground(getDrawable(R.drawable.dialog_bg))
            .show()

        
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupLoadingDialog()


        binding.emailInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(input: CharSequence?, start: Int, before: Int, count: Int) {
                val text = input.toString().trim()

                if (isPhone(text)) {
                    binding.otpLayout.visibility = View.VISIBLE
                    binding.sendOtpBtn.visibility = View.VISIBLE
                    binding.verifyOtpBtn.visibility = View.VISIBLE
                    binding.passwordLayout.visibility = View.GONE
                    binding.loginBtn.visibility = View.GONE
                } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()) {
                    // Show password field
                    binding.otpLayout.visibility = View.GONE
                    binding.sendOtpBtn.visibility = View.GONE
                    binding.verifyOtpBtn.visibility = View.GONE
                    binding.passwordLayout.visibility = View.VISIBLE

                    // Show login button only when password is also filled
                    binding.passwordInput.addTextChangedListener(object : TextWatcher {
                        override fun afterTextChanged(p0: Editable?) {}
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                        override fun onTextChanged(p: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            binding.loginBtn.visibility =
                                if (!p.isNullOrBlank()) View.VISIBLE else View.GONE
                        }
                    })
                } else {
                    binding.otpLayout.visibility = View.GONE
                    binding.sendOtpBtn.visibility = View.GONE
                    binding.verifyOtpBtn.visibility = View.GONE
                    binding.passwordLayout.visibility = View.GONE
                    binding.loginBtn.visibility = View.GONE
                }
            }
        })


        binding.sendOtpBtn.setOnClickListener {
            val phone = normalizePhone(binding.emailInput.text.toString())
            if (phone == null) {
                showToast("Invalid phone number")
            } else {
                sendVerificationCode(phone)
            }
        }

        binding.verifyOtpBtn.setOnClickListener {
            val code = binding.otpInput.text.toString().trim()
            if (verificationId != null && code.length == 6) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithPhoneCredential(credential)
            } else {
                showToast("Invalid OTP")
            }
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInWithEmail(email, password)
            } else {
                showToast("Fill in both email and password")
            }
        }
    }

//    private fun signInWithEmail(email: String, password: String) {
//        loadingDialog.show()
//        mAuth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                loadingDialog.dismiss()
//                if (task.isSuccessful) {
//                    val uid = mAuth.currentUser?.uid ?: return@addOnCompleteListener
//                    db.collection("guards").document(uid).get()
//                        .addOnSuccessListener {
//                            if (it.exists()) {
//                                showToast("Welcome ${it.getString("name")}")
//                                startActivity(Intent(this, ScannerActivity::class.java))
//                                finish()
//                            } else {
//                                showToast("No guard profile found")
//                            }
//                        }
//                } else {
//                    showToast("Login failed: ${task.exception?.message}")
//                }
//            }
//    }
    private fun signInWithEmail(email: String, password: String) {
        loadingDialog.show()
        db.collection("guards")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                loadingDialog.dismiss()
                if (!querySnapshot.isEmpty) {
                    val doc = querySnapshot.documents[0]
                    val storedPassword = doc.getString("password")
                    val guardName = doc.getString("name") ?: "Guard"

                    if (storedPassword?.let { AESEncryption.decrypt(it) } == password) {
                        showToast("Welcome $guardName")
                        val intent = Intent(this, ScannerActivity::class.java)
                        intent.putExtra("guardEmail", email)
                        intent.putExtra("guardName", guardName)
                        startActivity(intent)
                        finish()
                    } else {
                        showToast("Incorrect password")
                    }
                } else {
                    showToast("No guard registered with this email")
                }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                showToast("Error: ${it.message}")
            }
    }


    private fun sendVerificationCode(phoneNumber: String) {
        loadingDialog.show()
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(phoneAuthCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            showToast("Verification auto-complete. Enter OTP manually.")
        }

        override fun onVerificationFailed(e: FirebaseException) {
            loadingDialog.dismiss()
            showToast("Verification failed: ${e.message}")
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            loadingDialog.dismiss()
            verificationId = id
            resendToken = token
            showToast("OTP sent")
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        loadingDialog.show()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                loadingDialog.dismiss()
                if (task.isSuccessful) {
                    val phone = normalizePhone(binding.emailInput.text.toString())!!
                    db.collection("guards")
                        .whereEqualTo("phone", phone)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            if (!snapshot.isEmpty) {
                                showToast("Welcome Guard")
                                startActivity(Intent(this, ScannerActivity::class.java))
                                finish()
                            } else {
                                showToast("No guard registered with this number")
                            }
                        }
                } else {
                    showToast("Login failed: ${task.exception?.message}")
                }
            }
    }

    private fun isPhone(input: String): Boolean {
        return input.matches(Regex("^03[0-9]{9}$")) || input.matches(Regex("^\\+923[0-9]{9}$"))
    }

    private fun normalizePhone(input: String): String? {
        return when {
            input.startsWith("+92") && input.length == 13 -> input
            input.startsWith("03") && input.length == 11 -> "+92${input.substring(1)}"
            else -> null
        }
    }

    private fun setupLoadingDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_loader, null)
        loadingDialog = MaterialAlertDialogBuilder(this)
            .setView(view)  // ✔ pass View, not layout ID
            .setCancelable(false)
            .create()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
