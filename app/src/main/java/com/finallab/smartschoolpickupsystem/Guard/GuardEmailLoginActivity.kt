package com.finallab.smartschoolpickupsystem.Guard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finallab.smartschoolpickupsystem.Guard.GuardListActivity
import com.finallab.smartschoolpickupsystem.databinding.ActivityGuardEmailLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GuardEmailLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardEmailLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardEmailLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.loginBtn.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                loginWithEmail(email, password)
            }
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Optionally check if user is a guard
                val userId = auth.currentUser?.uid ?: return@addOnSuccessListener
                db.collection("guards")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (!snapshot.isEmpty) {
                            Toast.makeText(this, "Welcome, Guard", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, GuardListActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "No guard account found for this email", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
