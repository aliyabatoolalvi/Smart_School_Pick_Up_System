package com.finallab.smartschoolpickupsystem.Guard

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finallab.smartschoolpickupsystem.databinding.GuardItemBinding


class GuardAdapter(
    private val guardList: MutableList<Guard>,
    private val context: Context,
    private val onEdit: (Guard) -> Unit,
    private val onDelete: (Guard) -> Unit
) : RecyclerView.Adapter<GuardAdapter.GuardViewHolder>() {

    class GuardViewHolder(val binding: GuardItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardViewHolder {
        val binding = GuardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuardViewHolder, position: Int) {
        val guard = guardList[position]
        holder.binding.tvName.text = guard.name
        holder.binding.tvEmail.text = guard.email
        holder.binding.tvPhone.text = guard.phone

        // Long press logic
        holder.itemView.setOnLongClickListener {
            val options = arrayOf("Edit", "Delete")
            AlertDialog.Builder(context)
                .setTitle("Select Action")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> onEdit(guard)
                        1 -> confirmDelete(guard)
                    }
                }
                .show()
            true
        }
    }

    private fun confirmDelete(guard: Guard) {
        AlertDialog.Builder(context)
            .setTitle("Delete Confirmation")
            .setMessage("Are you sure you want to delete ${guard.name}?")
            .setPositiveButton("Delete") { _, _ ->
                onDelete(guard)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount() = guardList.size
}

