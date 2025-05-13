package com.finallab.smartschoolpickupsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.Calendar;
import java.util.Map;
import java.util.LinkedHashMap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.util.Pair;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;


import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

public class ReportActivity extends AppCompatActivity {
    ProgressBar progressBarReports;
    private FirebaseFirestore db;
    private ImageView backarrow;
    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<PickUpReport> reportList = new ArrayList<>();
    private BarChart delayChart;




    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Log.d("ReportDebug", "PickupReportActivity launched");
        Toast.makeText(this, "ReportActivity started", Toast.LENGTH_SHORT).show();
        recyclerView = findViewById(R.id.reportRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportAdapter(reportList, this, false);
        recyclerView.setAdapter(adapter);
        progressBarReports = findViewById(R.id.progressBarReports);
        delayChart = findViewById(R.id.delayChart);

        backarrow=findViewById(R.id.back_arrow);

        backarrow.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            progressBarReports.setVisibility(View.GONE);
            Log.e("ReportDebug", "FirebaseAuth user is null");
            return;
        }

        String guardianUID = currentUser.getUid();
        db.collection("guardians").document(guardianUID)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> studentDocIds = (List<String>) doc.get("students");

                        if (studentDocIds != null && !studentDocIds.isEmpty()) {
                            fetchReports(studentDocIds);
                        } else {
                            progressBarReports.setVisibility(View.GONE);
                        }
                    } else {
                        progressBarReports.setVisibility(View.GONE);
                        Toast.makeText(this, "Guardian not registered", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBarReports.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load reports", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchReports(List<String> studentDocIds) {
        int chunkSize = 10;
        for (int i = 0; i < studentDocIds.size(); i += chunkSize) {
            List<String> chunk = studentDocIds.subList(i, Math.min(i + chunkSize, studentDocIds.size()));

            db.collection("pick_up_activities")
                    .whereIn("studentId", chunk)

                    .get()
                    .addOnSuccessListener(querySnapshots -> {
                        Map<String, Integer> delayData = new LinkedHashMap<>();

                        for (DocumentSnapshot doc : querySnapshots) {
                            String studentName = doc.getString("studentName");
                            String deviation = doc.getString("deviation");
                            String pickedBy = doc.getString("guardianName");
                            String pickedByUID = doc.getString("guardianId");
                            Timestamp ts = doc.getTimestamp("timestamp");

                            PickUpReport report = new PickUpReport(studentName, ts, deviation, pickedBy, pickedByUID);
                            reportList.add(report);

                            if (deviation != null && deviation.contains("minutes")) {
                                try {
                                    String[] parts = deviation.split(" ");
                                    int minutes = Integer.parseInt(parts[0]);
                                    String formattedTime = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                                            .format(ts.toDate());

                                    delayData.put(formattedTime, minutes);
                                } catch (Exception ignored) {}
                            }
                        }

                        adapter.notifyDataSetChanged();
                        progressBarReports.setVisibility(View.GONE);
                        showDelayBarChart(delayData);


                    });
        }
    }

    private void showDelayBarChart(Map<String, Integer> delayData) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int index = 0;

        for (Map.Entry<String, Integer> entry : delayData.entrySet()) {
            int delay = entry.getValue();
            entries.add(new BarEntry(index, delay));
            labels.add(entry.getKey());


            if (delay > 30) {
                colors.add(Color.RED);
            } else {
                colors.add(Color.BLUE);
            }
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Delay in Minutes");
        dataSet.setColors(colors); // Set individual colors per bar

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        delayChart.setData(barData);
        delayChart.setFitBars(true);
        delayChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        delayChart.getXAxis().setGranularity(1f);
        delayChart.getXAxis().setGranularityEnabled(true);
        delayChart.getXAxis().setLabelRotationAngle(-45);
        delayChart.invalidate();
    }

}

