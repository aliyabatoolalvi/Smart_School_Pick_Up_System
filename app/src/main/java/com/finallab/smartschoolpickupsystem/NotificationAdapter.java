package com.finallab.smartschoolpickupsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter  extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {
    private final List<GuardianNotifiaction> notifList;
    private final OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onClick(GuardianNotifiaction notification);
    }


    public NotificationAdapter(List<GuardianNotifiaction> notifList, OnNotificationClickListener listener) {

        this.notifList = notifList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notifiaction, parent, false);
        return new NotifViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        GuardianNotifiaction notif = notifList.get(position);
        holder.title.setText(notif.getTitle());
        holder.body.setText(notif.getBody());

        String dateStr = notif.getTimestamp() != null
                ? new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(notif.getTimestamp().toDate())
                : "N/A";
        holder.timestamp.setText(dateStr);

        // ✅ Grey out if already read
        holder.itemView.setAlpha(notif.isSeen() ? 0.5f : 1f);

        holder.itemView.setOnClickListener(v -> listener.onClick(notif));
    }

    @Override
    public int getItemCount() {
        return notifList.size();
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView title, body, timestamp;
        Button markAsReadButton;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notificationTitle);
            body = itemView.findViewById(R.id.notificationBody);
            timestamp = itemView.findViewById(R.id.notificationTimestamp);
            markAsReadButton = itemView.findViewById(R.id.markAsReadButton);

        }
    }
}
