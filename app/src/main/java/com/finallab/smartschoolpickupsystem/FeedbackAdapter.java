package com.finallab.smartschoolpickupsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>{

    private final List<feedback> feedbackList;
    private final OnFeedbackLongClickListener longClickListener;

    public interface OnFeedbackLongClickListener {
        void onLongClick(feedback item);
    }

    public FeedbackAdapter(List<feedback> feedbackList, OnFeedbackLongClickListener listener) {
        this.feedbackList = feedbackList;
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_item, parent, false);
        return new FeedbackViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        feedback item = feedbackList.get(position);

        holder.feedbackText.setText("Feedback: " + item.getFeedbackText());
        holder.status.setText("Status: " + item.getStatus());
        holder.adminReply.setText("Reply: " + (item.getAdminReply() == null ? "N/A" : item.getAdminReply()));

        if (item.getTimestamp() != null) {
            String dateStr = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(item.getTimestamp().toDate());
            holder.timestamp.setText("Date: " + dateStr);
        } else {
            holder.timestamp.setText("Date: N/A");
        }
        // ✅ Enable long-click to trigger delete
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onLongClick(item);
            return true;
        });
    }



    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView feedbackText, status, adminReply, timestamp;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            feedbackText = itemView.findViewById(R.id.feedbackText);
            status = itemView.findViewById(R.id.feedbackStatus);
            adminReply = itemView.findViewById(R.id.feedbackReply);
            timestamp = itemView.findViewById(R.id.feedbackTimestamp);
        }
    }
}

