package com.finallab.smartschoolpickupsystem.Recycler

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.finallab.smartschoolpickupsystem.Activities.EditStudentActivity

import com.finallab.smartschoolpickupsystem.Activities.StudentDetails
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.Activities.GuardianDetails
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.databinding.GuardianItemBinding
import com.finallab.smartschoolpickupsystem.databinding.StudentItemBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface OnStudentDeletedListener {
    fun onStudentDeleted()
}

class RecyclerViewAdapter(val items: MutableList<Any>, val lifecycleScope: CoroutineScope,     private val listener: OnStudentDeletedListener? // Add listener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            val binding =
                StudentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return StudentViewHolder(binding)
        } else {
            val binding =
                GuardianItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return GuardianViewHolder(binding)
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        if (items.get(position) is Student) return 0
        return 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is StudentViewHolder) {
            val student = items.get(position) as Student
            holder.binding.namestu.text = student.Sname
            holder.binding.classstu.text = "Class: " + student.studentClass
            holder.binding.sectionstu.text = "Section: " + student.section

//            holder.binding.delS.setOnClickListener {
//                AlertDialog.Builder(holder.itemView.context)
//                    .setTitle("Delete Confirmation")
//                    .setMessage("Are you sure?")
//                    .setPositiveButton("Yes") { _, _ ->
//                        // Use lifecycleScope for database and UI updates
//                        lifecycleScope.launch {
//
//                            AppDatabase.getDatabase(holder.itemView.context).studentDao().delete(student)
//                            items.removeAt(holder.adapterPosition)
//                            notifyItemRemoved(holder.adapterPosition)
//                            Toast.makeText(holder.itemView.context, "Record deleted", Toast.LENGTH_SHORT)
//                                .show()
//                        }
//                    }
//                    .setNegativeButton("Cancel", null)
//                    .show()
//            }

            holder.binding.editS.setOnClickListener {
                val intent = Intent(holder.itemView.context, EditStudentActivity::class.java)
                intent.putExtra("id", student.id)
                intent.putExtra("studentDocumentID", student.studentDocId)
                holder.itemView.context.startActivity(intent)
            }

            holder.binding.delS.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Delete Confirmation")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes") { _, _ ->
                        lifecycleScope.launch {
                            // Delete
                            AppDatabase.getDatabase(holder.itemView.context).studentDao().delete(student)

                            val firestore = FirebaseFirestore.getInstance()
                            val studentDocId = student.studentDocId // Use Firestore document ID

                            if (studentDocId.isNotEmpty()) {
                                firestore.collection("students").document(studentDocId)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(holder.itemView.context, "Deleted from Firestore", Toast.LENGTH_SHORT).show()
                                        listener?.onStudentDeleted() // Notify ProfileFragment

                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(holder.itemView.context, "Firestore deletion failed", Toast.LENGTH_SHORT).show()
                                    }
                            }

                            // Update UI
                            items.removeAt(holder.adapterPosition)
                            notifyItemRemoved(holder.adapterPosition)
                            Toast.makeText(holder.itemView.context, "Record deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }





            holder.itemView.setOnClickListener {
                holder.itemView.context.startActivity(
                    Intent(
                        holder.itemView.context,
                        StudentDetails::class.java
                    ).putExtra("id", student.id)

                )
            }
        }

        if (holder is GuardianViewHolder) {
            val guardian = items[position] as Guardian
            holder.binding.namegu.text = guardian.Gname
            holder.binding.CNICgu.text = "CNIC: " + guardian.CNIC

            // Guardian delete logic
            holder.binding.delG.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Delete Confirmation")
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes") { _, _ ->
                        lifecycleScope.launch {
                            // Delete from Room
                            AppDatabase.getDatabase(holder.itemView.context).guardianDao().delete(guardian)

                            // Delete from Firestore
                            val firestore = FirebaseFirestore.getInstance()
                            firestore.collection("guardians").document(guardian.id.toString())
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(holder.itemView.context, "Guardian deleted from Firestore", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(holder.itemView.context, "Firestore deletion failed", Toast.LENGTH_SHORT).show()
                                }

                            // Update UI
                            items.removeAt(holder.adapterPosition)
                            notifyItemRemoved(holder.adapterPosition)
                            Toast.makeText(holder.itemView.context, "Record deleted", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            holder.itemView.setOnClickListener {
                holder.itemView.context.startActivity(
                    Intent(
                        holder.itemView.context,
                        GuardianDetails::class.java
                    ).putExtra("id", guardian.id)
                )
            }
        }

    }
}

