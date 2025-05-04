package com.finallab.smartschoolpickupsystem;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<PickUpReport> reports;
    private boolean isAdmin;

    public ReportAdapter(List<PickUpReport> reports, boolean isAdmin) {
        this.reports = reports;
        this.isAdmin = isAdmin;
    }

    public void updateReports(List<PickUpReport> updated) {
        this.reports = updated;
        notifyDataSetChanged();
    }

    public List<PickUpReport> getReports() {
        return reports;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        PickUpReport report = reports.get(position);

        holder.studentName.setText("👤 " + report.getStudentName());

        if (report.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault());
            holder.pickUpTime.setText("🕒 " + sdf.format(report.getTimestamp()));
        } else {
            holder.pickUpTime.setText("🕒 N/A");
        }

        // 🔹 Show report text if exists
        if (report.getReportText() != null) {
            holder.reportText.setText("📝 " + report.getReportText());
            holder.reportText.setVisibility(View.VISIBLE);
        } else {
            holder.reportText.setVisibility(View.GONE);
        }

        // 🔹 For Admin: Show Guardian ID or CNIC if needed
        if (isAdmin) {
            holder.extraInfo.setText("💂‍♂️ " + "Guard Name: " + report.getGuardName());
            holder.extraInfo.setVisibility(View.VISIBLE);
        } else {
            holder.extraInfo.setVisibility(View.GONE);
        }

        // 🔸 Highlight manual pickups
        if (report.getReportText() != null && report.getReportText().toLowerCase().contains("manual")) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFF9C4")); // Light yellow
            holder.manualIcon.setVisibility(View.VISIBLE);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.manualIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, pickUpTime, reportText, extraInfo;
        CardView cardView;
        ImageView manualIcon;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.reportCard);
            studentName = itemView.findViewById(R.id.studentName);
            pickUpTime = itemView.findViewById(R.id.pickUpTime);
            reportText = itemView.findViewById(R.id.reportText);
            extraInfo = itemView.findViewById(R.id.extraInfo);
            manualIcon = itemView.findViewById(R.id.manualIcon);
        }
    }
}
