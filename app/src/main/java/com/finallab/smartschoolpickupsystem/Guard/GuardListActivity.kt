package com.finallab.smartschoolpickupsystem.Guard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.databinding.ActivityGuardListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GuardListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardListBinding
    private val db = FirebaseFirestore.getInstance()
    private val guardList = mutableListOf<Guard>()
    private lateinit var adapter: GuardAdapter
    private lateinit var currentAdminUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentAdminUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        Log.d("GuardListActivity", "Current Admin UID: $currentAdminUid")

        binding.rvc.layoutManager = LinearLayoutManager(this)

        adapter = GuardAdapter(
            guardList,
            this,
            onEdit = { guard ->
                val intent = Intent(this, GuardAddActivity::class.java)
                intent.putExtra("isEdit", true)
                intent.putExtra("guardId", guard.id)
                intent.putExtra("guardName", guard.name)
                intent.putExtra("guardEmail", guard.email)
                intent.putExtra("guardPhone", guard.phone)
                intent.putExtra("guardPassword", AESEncryption.decrypt(guard.password))
                startActivity(intent)

            },
            onDelete = { guard ->
                deleteGuardFromFirestore(guard)
            }
        )

        binding.rvc.adapter = adapter

        binding.addrec.setOnClickListener {
            startActivity(Intent(this, GuardAddActivity::class.java))
        }

        binding.scanact.setOnClickListener {
            startActivity(Intent(this, ScannerActivity::class.java))
        }

        fetchGuards()
    }

    override fun onResume() {
        super.onResume()
        fetchGuards()
    }

    private fun fetchGuards() {
        db.collection("guards")
            .whereEqualTo("userId", currentAdminUid)
            .get()
            .addOnSuccessListener { documents ->
                guardList.clear()
                for (document in documents) {
                    val guard = document.toObject(Guard::class.java).apply {
                        id = document.id
                    }
                    guardList.add(guard)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteGuardFromFirestore(guard: Guard) {
        db.collection("guards").document(guard.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "${guard.name} deleted", Toast.LENGTH_SHORT).show()
                fetchGuards()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
    }
}
