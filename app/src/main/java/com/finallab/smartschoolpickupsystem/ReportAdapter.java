package com.finallab.smartschoolpickupsystem;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private List<PickUpReport> reportList;
    private Context context;
    private boolean isAdmin;

    public ReportAdapter(List<PickUpReport> reportList, Context context, boolean isAdmin) {
        this.reportList = reportList;
        this.context = context;
        this.isAdmin = isAdmin;
    }

    public void updateReports(List<PickUpReport> updatedReports) {
        this.reportList = updatedReports;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentNameText, timestampText, deviationText, pickedByText, pickupMethodText;

        ImageView guardianAvatar, manualIcon;
        View reportCard;

        public ViewHolder(View itemView) {
            super(itemView);
            studentNameText = itemView.findViewById(R.id.studentNameText);
            timestampText = itemView.findViewById(R.id.timestampText);
            deviationText = itemView.findViewById(R.id.deviationText);
            pickedByText = itemView.findViewById(R.id.pickedByText);
            guardianAvatar = itemView.findViewById(R.id.guardianAvatar);
            manualIcon = itemView.findViewById(R.id.manualIcon);
            reportCard = itemView.findViewById(R.id.reportCard);
        }
    }

    @NonNull
    @Override
    public ReportAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isAdmin ? R.layout.item_admin_report : R.layout.item_report;
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ViewHolder holder, int position) {
        PickUpReport report = reportList.get(position);

        holder.studentNameText.setText(report.getStudentName());

        if (report.getTimestamp() != null) {
            Date date = report.getTimestamp().toDate();  // Convert Firestore Timestamp to Date
            String formatted = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date);
            holder.timestampText.setText("Picked at: " + formatted);
        } else {
            holder.timestampText.setText("Picked at: N/A");
        }

        if (report.getReportText() != null && report.getReportText().toLowerCase().contains("manual")) {
            holder.reportCard.setBackgroundColor(Color.parseColor("#FFF9C4")); // Light yellow
            holder.manualIcon.setVisibility(View.VISIBLE);
        } else {
            holder.reportCard.setBackgroundColor(Color.WHITE);
            holder.manualIcon.setVisibility(View.GONE);
        }

        if (isAdmin) {
            holder.deviationText.setVisibility(View.VISIBLE);
            holder.deviationText.setText("Pickup Type: " + report.getMethod());
            holder.pickedByText.setText("Picked by: " + report.getPickedBy());
        } else {
            holder.deviationText.setVisibility(View.GONE);
            holder.pickedByText.setText("Picked by you");
        }

        if (holder.guardianAvatar != null) {
            holder.guardianAvatar.setImageResource(R.drawable.guardian);
        }
    }



    @Override
    public int getItemCount() {
        return reportList.size();
    }
}
