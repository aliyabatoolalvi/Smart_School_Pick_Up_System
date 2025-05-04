package com.finallab.smartschoolpickupsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.finallab.smartschoolpickupsystem.Room.AppDatabase;
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

public class ReportActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView emptyText;
    private Button backButton;
    private AppDatabase localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        recyclerView = findViewById(R.id.recyclerViewReports);
        progressBar = findViewById(R.id.progressBarReports);
        emptyText = findViewById(R.id.emptyText);
        backButton = findViewById(R.id.backbtn2);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new ReportAdapter(new ArrayList<>(), false); // false = not admin
        recyclerView.setAdapter(reportAdapter);

        db = FirebaseFirestore.getInstance();

        backButton.setOnClickListener(v -> finish());

        loadReports();
    }

    private void loadReports() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String guardianUID = currentUser.getUid();
        progressBar.setVisibility(View.VISIBLE);

        db.collection("pick_up_activities")
                .whereEqualTo("guardianUID", guardianUID)
                .orderBy("pickUpTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    progressBar.setVisibility(View.GONE);
                    List<PickUpReport> reports = new ArrayList<>();
                    List<PickUpReport> entities = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        PickUpReport report = doc.toObject(PickUpReport.class);
                        if (report != null) {
                            reports.add(report);

                            PickUpReport entity = new PickUpReport();
                            entity.setStudentName(report.getStudentName());
                            entity.setTimestamp(report.getTimestamp());
                            entity.setReportText(report.getReportText());
                            entity.setCNIC(report.getCNIC());
                            entities.add(entity);
                        }
                    }

                    if (reports.isEmpty()) {
                        showEmptyState("🚫 No reports found");
                    } else {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        reportAdapter.updateReports(reports);

                        new Thread(() -> {
                            AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "guardian_local_db").build();
                            db.pickUpReportDao().insertReports(entities);
                        }).start();
                    }
                })
                .addOnFailureListener(e -> {
                    new Thread(() -> {
                        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "guardian_local_db").build();
                        List<PickUpReport> cached = db.pickUpReportDao().getAllReports();

                        List<PickUpReport> fallbackList = new ArrayList<>();
                        for (PickUpReport r : cached) {
                            PickUpReport report = new PickUpReport();
                            report.setStudentName(r.getStudentName());
                            report.setTimestamp(r.getTimestamp());
                            report.setReportText(r.getReportText());
                            report.setCNIC(r.getCNIC());
                            fallbackList.add(report);
                        }

                        runOnUiThread(() -> {
                            reportAdapter.updateReports(fallbackList);
                            recyclerView.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Showing offline data", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                });
    }

    private void exportToPdf() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        int y = 50;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(14f);
        canvas.drawText("Pickup Report", 50, y, paint);
        y += 30;

        for (PickUpReport report : reportAdapter.getReports()) {
            String line = report.getReportText() != null ? report.getReportText() :
                    report.getStudentName() + " - " + new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(report.getTimestamp());
            canvas.drawText(line, 50, y, paint);
            y += 25;

            if (y > 800) {
                pdfDocument.finishPage(page);
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
        }

        pdfDocument.finishPage(page);

        try {
            File file = new File(getExternalFilesDir(null), "PickupReport.pdf");
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save PDF", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }

    private void showEmptyState(String message) {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setText(message);
        emptyText.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
