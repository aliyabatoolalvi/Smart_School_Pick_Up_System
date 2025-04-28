package com.finallab.smartschoolpickupsystem.Recycler

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.finallab.smartschoolpickupsystem.Activities.EditGuardianActivity
import com.finallab.smartschoolpickupsystem.Activities.EditStudentActivity
import com.finallab.smartschoolpickupsystem.Activities.GuardianDetails
import com.finallab.smartschoolpickupsystem.Activities.MainActivity
import com.finallab.smartschoolpickupsystem.Activities.StudentDetails
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.GuardianItemBinding
import com.finallab.smartschoolpickupsystem.databinding.StudentItemBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface OnItemDeletedListener {
    fun onDataUpdated()
}

class RecyclerViewAdapter(
    private val items: MutableList<Any>,
    private val lifecycleScope: CoroutineScope,
    private val listener: OnItemDeletedListener? = null,
    private var isLoading: Boolean = false,

    private val onDeleteClick: ((Guardian) -> Unit)? = null

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val STUDENT_TYPE = 0
        private const val GUARDIAN_TYPE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == STUDENT_TYPE) {
            val binding = StudentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            StudentViewHolder(binding)
        } else {
            val binding = GuardianItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            GuardianViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is Student) STUDENT_TYPE else GUARDIAN_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StudentViewHolder -> bindStudent(holder, position)
            is GuardianViewHolder -> bindGuardian(holder, position)
        }
    }

    fun updateData(newData: MutableList<Any>?, isLoading: Boolean = false) {
        this.isLoading = isLoading // Update the loading flag
        items.clear() // Clear current data

        if (isLoading) {
            // Notify RecyclerView to show a loading state
            notifyItemInserted(items.size)
        } else {
            newData?.let { items.addAll(it) }
            notifyDataSetChanged() // Notify when data is updated
        }
    }


    // --- Student Binding ---
    private fun bindStudent(holder: StudentViewHolder, position: Int) {
        val student = items[position] as Student
        holder.binding.namestu.text = student.Sname
        holder.binding.classstu.text = "Class: ${student.studentClass}"
        holder.binding.sectionstu.text = "Section: ${student.section}"

        holder.binding.editS.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditStudentActivity::class.java).apply {
                putExtra("studentID", student.studentID)
                putExtra("studentDocumentID", student.studentDocId)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.binding.delS.setOnClickListener {
            showDeleteConfirmation(holder, student, position)
        }

        holder.itemView.setOnClickListener {
            Log.d("RecyclerViewAdapter", "Clicked student: ${student.studentID}")

            val intent = Intent(holder.itemView.context, StudentDetails::class.java).apply {
                putExtra("id", student.studentID)
                putExtra("studentDocumentID", student.studentDocId)

            }
            holder.itemView.context.startActivity(intent)
        }

        Log.d("RecyclerViewAdapter", "Navigating to StudentDetails with studentID: ${student.studentID}")
    }

    private fun showDeleteConfirmation(holder: RecyclerView.ViewHolder, student: Student, position: Int) {
        AlertDialog.Builder(holder.itemView.context)
            .setTitle("Delete Confirmation")
            .setMessage("Are you sure you want to delete this student?")
            .setPositiveButton("Yes") { _, _ -> deleteStudent(holder, student, position) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteStudent(holder: RecyclerView.ViewHolder, student: Student, position: Int) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(holder.itemView.context).studentDao()
                db.deleteStudent(student)

                if (student.studentDocId.isNotEmpty()) {
                    FirebaseFirestore.getInstance().collection("students").document(student.studentDocId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Deleted from Firestore", Toast.LENGTH_SHORT).show()
                            listener?.onDataUpdated()
                        }
                        .addOnFailureListener {
                            Toast.makeText(holder.itemView.context, "Failed to delete from Firestore", Toast.LENGTH_SHORT).show()
                        }
                }

                items.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(holder.itemView.context, "Student deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }


        }
    }

    // --- Guardian Binding ---
    private fun bindGuardian(holder: GuardianViewHolder, position: Int) {
        val guardian = items[position] as Guardian
        holder.binding.namegu.text = guardian.Gname
        holder.binding.CNICgu.text = "CNIC: ${guardian.CNIC}"

        holder.binding.delG.setOnClickListener {
            showGuardianDeleteConfirmation(holder, guardian, position)
        }

        holder.binding.editG.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditGuardianActivity::class.java).apply {
                putExtra("guardianID", guardian.guardianID)
                putExtra("guardianDocumentID", guardian.guardianDocId)
            }
            holder.itemView.context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, GuardianDetails::class.java).apply {
                putExtra("guardianID", guardian.guardianID)
                putExtra("guardianDocumentID", guardian.guardianDocId)
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    private fun showGuardianDeleteConfirmation(holder: GuardianViewHolder, guardian: Guardian, position: Int) {
        AlertDialog.Builder(holder.itemView.context)
            .setTitle("Delete Confirmation")
            .setMessage("Are you sure you want to delete this guardian?")
            .setPositiveButton("Yes") { _, _ ->
                deleteGuardian(holder, guardian, position)
                onDeleteClick?.invoke(guardian)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteGuardian(holder: GuardianViewHolder, guardian: Guardian, position: Int) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getDatabase(holder.itemView.context).guardianDao()
                db.deleteGuardian(guardian)

                if (!guardian.guardianDocId.isNullOrEmpty()) {
                    FirebaseFirestore.getInstance().collection("guardians").document(guardian.guardianDocId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Deleted from Firestore", Toast.LENGTH_SHORT).show()
                            listener?.onDataUpdated()
                        }
                        .addOnFailureListener {
                            Toast.makeText(holder.itemView.context, "Failed to delete from Firestore", Toast.LENGTH_SHORT).show()
                        }
                }

                items.removeAt(position)
                notifyItemRemoved(position)
                Toast.makeText(holder.itemView.context, "Guardian deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(holder.itemView.context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class StudentViewHolder(val binding: StudentItemBinding) : RecyclerView.ViewHolder(binding.root)
    class GuardianViewHolder(val binding: GuardianItemBinding) : RecyclerView.ViewHolder(binding.root)
}
