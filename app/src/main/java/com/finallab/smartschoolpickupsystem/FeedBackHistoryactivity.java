package com.finallab.smartschoolpickupsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FeedBackHistoryactivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FeedbackAdapter adapter;
    private List<feedback> feedbackList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back_historyactivity);
        recyclerView = findViewById(R.id.feedbackHistoryRecycler);
        progressBar = findViewById(R.id.progressBarHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FeedbackAdapter(feedbackList, this::confirmDelete);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadFeedbackHistory();
    }

    private void loadFeedbackHistory() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("feedback")
                .whereEqualTo("guardianUID", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) return;

                    feedbackList.clear();
                    for (DocumentSnapshot doc : value) {
                        feedback item = doc.toObject(feedback.class);
                        item.setId(doc.getId()); // optional, if feedback.java has setId()
                        feedbackList.add(item);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
    private void confirmDelete(feedback item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Feedback")
                .setMessage("Are you sure you want to delete this feedback?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("feedback")
                            .document(item.getId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();

                                
                                int index = feedbackList.indexOf(item);
                                if (index != -1) {
                                    feedbackList.remove(index);
                                    adapter.notifyItemRemoved(index);
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}
