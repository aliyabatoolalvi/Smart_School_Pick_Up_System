package com.finallab.smartschoolpickupsystem.Activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.model.Adapters.FeedbackAdapter
import com.finallab.smartschoolpickupsystem.model.DataModels.Feedback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminFeedbackActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedbackAdapter
    private lateinit var db: FirebaseFirestore
    private var feedbackList = mutableListOf<Feedback>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_feedback)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerViewFeedback)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FeedbackAdapter(feedbackList) { feedback ->
            updateStatusToResolved(feedback)
        }
        recyclerView.adapter = adapter

        fetchFeedback()
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
//        val filterSpinner = findViewById<Spinner>(R.id.filterSpinner)
//
//        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                val selected = parent.getItemAtPosition(position).toString()
//                filterFeedback(selected)
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {}
//        }
        val dropdown = findViewById<AutoCompleteTextView>(R.id.filterDropdown)
        val options = listOf("All", "Pending", "Resolved")

        val adapter = ArrayAdapter(this, R.layout.dropdown_item, options)
        dropdown.setAdapter(adapter)
        dropdown.setText("All", false)

        dropdown.setOnItemClickListener { _, _, position, _ ->
            filterFeedback(options[position])
        }



    }

    private fun fetchFeedback() {
        db.collection("feedback")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                this.feedbackList = snapshot.documents.mapNotNull { it.toObject(Feedback::class.java) }.toMutableList()

                val selectedFilter = findViewById<AutoCompleteTextView>(R.id.filterDropdown).text.toString()
                filterFeedback(selectedFilter)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading feedback", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateStatusToResolved(feedback: Feedback) {
//        val inputEditText = EditText(this).apply {
//            hint = "Write your reply to guardian"
//            setPadding(30, 20, 30, 20)
//
//            // 🎨 Set custom outline color here
//            backgroundTintList = getColorStateList(R.color.background_color)
//        }
//
//        val container = FrameLayout(this).apply {
//            val params = FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT
//            ).apply {
//                setMargins(50, 0, 50, 0)
//            }
//            addView(inputEditText, params)
//        }
//
//        MaterialAlertDialogBuilder(this)
//            .setTitle("Resolve Feedback")
//            .setView(container)
//            .setPositiveButton("Send & Resolve") { _, _ ->
//                val reply = inputEditText.text.toString().trim()
//                if (reply.isEmpty()) {
//                    Toast.makeText(this, "Please enter a reply", Toast.LENGTH_SHORT).show()
//                    return@setPositiveButton
//                }
        val view = layoutInflater.inflate(R.layout.dialog_admin_reply, null)
        val replyInput = view.findViewById<EditText>(R.id.editAdminReply)

        MaterialAlertDialogBuilder(this)
            .setTitle("Admin Reply")
            .setView(view)
            .setPositiveButton("Send & Resolve") { _, _ ->
                val reply = replyInput.text.toString().trim()
                val query = FirebaseFirestore.getInstance().collection("feedback")
                    .whereEqualTo("timestamp", feedback.timestamp)
                    .whereEqualTo("guardianUID", feedback.guardianUID)

                query.get().addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val docId = result.documents[0].id
                        FirebaseFirestore.getInstance().collection("feedback").document(docId)
                            .update(
                                mapOf(
                                    "status" to "Resolved",
                                    "adminReply" to reply
                                )
                            )
                            .addOnSuccessListener {
                                Toast.makeText(this, "Marked as Resolved", Toast.LENGTH_SHORT).show()
                                fetchFeedback()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update feedback", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Feedback not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun filterFeedback(filter: String) {
        val filteredList = when (filter) {
            "Pending" -> feedbackList.filter { it.status.equals("Pending", ignoreCase = true) }
            "Resolved" -> feedbackList.filter { it.status.equals("Resolved", ignoreCase = true) }
            else -> feedbackList
        }
        adapter.updateList(filteredList.toMutableList())
    }


}
