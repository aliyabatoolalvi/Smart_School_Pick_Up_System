package com.finallab.smartschoolpickupsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Feedbackactivity extends AppCompatActivity {

    private EditText feedbackInput;
    private Button submitFeedbackButton,back_btn;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedbackactivity);

        db = FirebaseFirestore.getInstance();

        feedbackInput = findViewById(R.id.feedbackInput);
        submitFeedbackButton = findViewById(R.id.submitFeedbackButton);
        back_btn = findViewById(R.id.back_button1);

        submitFeedbackButton.setOnClickListener(v -> submitFeedback());
        back_btn.setOnClickListener(v -> finish());
    }

    private void submitFeedback() {
        String feedbackText = feedbackInput.getText().toString().trim();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        if (feedbackText.isEmpty()) {
            Toast.makeText(this, "Please enter feedback", Toast.LENGTH_SHORT).show();
            return;
        }

        String guardianUID = currentUser.getUid();

        db.collection("guardians").document(guardianUID)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        Toast.makeText(this, "Guardian profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String guardianCNIC = document.getString("CNIC");
                    String guardianName = document.getString("Gname");
                    String sentiment = classifyFeedback(feedbackText);

                    Map<String, Object> feedback = new HashMap<>();
                    feedback.put("guardianUID", guardianUID);
                    feedback.put("guardianCNIC", guardianCNIC);
                    feedback.put("guardianName", guardianName);
                    feedback.put("feedbackText", feedbackText);
                    feedback.put("sentiment", sentiment);
                    feedback.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("feedback")
                            .add(feedback)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Feedback submitted (" + sentiment + ")", Toast.LENGTH_SHORT).show();

                                if ("Negative".equals(sentiment)) {
                                    sendAlertToAdmin(guardianCNIC, guardianName, feedbackText);
                                }

                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error submitting feedback: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load guardian info: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String classifyFeedback(String feedback) {
        String[] positiveWords = {"good", "great", "excellent", "amazing", "awesome", "happy", "love", "satisfied", "nice", "wonderful"};
        String[] negativeWords = {"bad", "poor", "worst", "terrible", "hate", "sad", "angry", "unsatisfied", "problem", "issue"};

        int positiveCount = 0, negativeCount = 0;
        String[] words = feedback.toLowerCase().split("\\s+");

        for (String word : words) {
            for (String pos : positiveWords) {
                if (word.contains(pos)) positiveCount++;
            }
            for (String neg : negativeWords) {
                if (word.contains(neg)) negativeCount++;
            }
        }

        if (positiveCount > negativeCount) return "Positive";
        else if (negativeCount > positiveCount) return "Negative";
        else return "Neutral";
    }

    private void sendAlertToAdmin(String guardianCNIC, String guardianName, String feedbackText) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("guardianCNIC", guardianCNIC);
        alert.put("guardianName", guardianName);
        alert.put("feedbackText", feedbackText);
        alert.put("type", "NegativeFeedback");
        alert.put("timestamp", FieldValue.serverTimestamp());

        db.collection("alerts")
                .add(alert)
                .addOnSuccessListener(doc -> Log.d("Feedback", "Alert sent"))
                .addOnFailureListener(e -> Log.e("Feedback", "Alert failed: " + e.getMessage()));
    }
}