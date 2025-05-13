package com.finallab.smartschoolpickupsystem.model.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.finallab.smartschoolpickupsystem.R
import com.finallab.smartschoolpickupsystem.model.DataModels.Feedback

class FeedbackAdapter(
    private var feedbackList: MutableList<Feedback>,
    private val onStatusUpdate: (Feedback) -> Unit
) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val guardianNameText: TextView = view.findViewById(R.id.guardianNameText)
        val feedbackText: TextView = view.findViewById(R.id.feedbackText)
        val sentimentText: TextView = view.findViewById(R.id.sentimentText)
        val statusText: TextView = view.findViewById(R.id.statusText)
        val adminReplyText: TextView = view.findViewById(R.id.adminReplyText)
        val markResolvedBtn: Button = view.findViewById(R.id.markResolvedBtn)
        val statusTick: ImageView = view.findViewById(R.id.statusTick)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val item = feedbackList[position]

        holder.guardianNameText.text = "Guardian: ${item.guardianName}"
        holder.feedbackText.text = "Remarks: ${item.feedbackText}"
        holder.sentimentText.text = "Sentiment: ${item.sentiment}"
        holder.statusText.text = "Status: ${item.status}"

        if (item.status.equals("Resolved", ignoreCase = true)) {
            holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.green))
            holder.statusTick.visibility = View.VISIBLE
            holder.markResolvedBtn.visibility = View.GONE

            if (!item.adminReply.isNullOrBlank()) {
                holder.adminReplyText.visibility = View.VISIBLE
                holder.adminReplyText.text = "Admin Reply: ${item.adminReply}"
            } else {
                holder.adminReplyText.visibility = View.GONE
            }
        } else if (item.status.equals("Pending", ignoreCase = true)) {
            // 🔴 Set status text color to red for pending
            holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.red))
            holder.statusTick.visibility = View.GONE
            holder.markResolvedBtn.visibility = View.VISIBLE
            holder.adminReplyText.visibility = View.GONE

            holder.markResolvedBtn.setOnClickListener {
                onStatusUpdate(item)
            }
        } else {
            // Default fallback if other statuses appear
            holder.statusText.setTextColor(holder.itemView.context.getColor(R.color.black))
            holder.statusTick.visibility = View.GONE
            holder.markResolvedBtn.visibility = View.VISIBLE
            holder.adminReplyText.visibility = View.GONE
        }
    }



    override fun getItemCount(): Int = feedbackList.size

    fun updateList(newList: MutableList<Feedback>) {
        feedbackList = newList
            .sortedWith(compareBy(
                { it.status.equals("Resolved", ignoreCase = true) }, // false (Pending) comes before true (Resolved)
                { it.timestamp } // Optional: sort within groups by time
            ))
            .toMutableList()
        notifyDataSetChanged()
    }

}
