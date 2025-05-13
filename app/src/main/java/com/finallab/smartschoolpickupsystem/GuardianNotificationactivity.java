package com.finallab.smartschoolpickupsystem;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class GuardianNotificationactivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<GuardianNotifiaction> notificationList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_notificationactivity);
        recyclerView = findViewById(R.id.notificationRecycler);
        progressBar = findViewById(R.id.progressBarNotif);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, this::markAsRead);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadNotifications();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("guardians")
                .document(currentUser.getUid())
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    notificationList.clear();
                    for (DocumentSnapshot doc : value) {
                        GuardianNotifiaction notif = doc.toObject(GuardianNotifiaction.class);
                        if (notif != null) {
                            notif.setId(doc.getId());  // optional if you add an ID field
                            notificationList.add(notif);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void markAsRead(GuardianNotifiaction notif) {
        if (!notif.isSeen()) {
            db.collection("guardians")
                    .document(currentUser.getUid())
                    .collection("notifications")
                    .document(notif.getId())
                    .update("seen", true);
        }
    }
}