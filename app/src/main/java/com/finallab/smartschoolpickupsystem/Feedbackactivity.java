package com.finallab.smartschoolpickupsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import okhttp3.*;

import okhttp3.OkHttpClient;

public class Feedbackactivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageView notificationIcon, backArrow;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private static final String DIALOGFLOW_PROJECT_ID = "smart-school-pick-up-sys-c73a9";
    private static final String NLP_API_KEY = "AIzaSyBBujRr2r9d-1jxQxTswDtaM2v8h1rGx50";






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedbackactivity);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        progressBar = findViewById(R.id.qrProgressBar);
        notificationIcon = findViewById(R.id.notification_icon);
        backArrow = findViewById(R.id.back_arrow1);

        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        progressBar.setVisibility(View.GONE);

        sendButton.setOnClickListener(v -> {
            String text = messageEditText.getText().toString().trim();
            if (text.isEmpty()) return;

            showUserMessage(text);
            messageEditText.setText("");
            progressBar.setVisibility(View.VISIBLE);

            detectIntent(this, text, intent ->
                    analyzeSentiment(text, (sentiment, score) ->
                            saveFeedback(text, intent, sentiment, score)
                    )
            );
        });

        notificationIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, GuardianNotificationactivity.class));
        });

        backArrow.setOnClickListener(v -> finish());
    }

    private void showUserMessage(String text) {
        chatMessages.add(new ChatMessage(text, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void showBotMessage(String text) {
        chatMessages.add(new ChatMessage(text, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void saveFeedback(String text, String intent, String sentiment, float score) {
        String uid = auth.getCurrentUser().getUid();
        db.collection("guardians").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Map<String, Object> feedback = new HashMap<>();
                    feedback.put("guardianUID", uid);
                    feedback.put("guardianName", doc.getString("Gname"));
                    feedback.put("guardianCNIC", doc.getString("CNIC"));
                    feedback.put("feedbackText", text);
                    feedback.put("intent", intent);
                    feedback.put("sentiment", sentiment);
                    feedback.put("score", score);
                    feedback.put("timestamp", FieldValue.serverTimestamp());
                    feedback.put("status", "Pending");
                    feedback.put("adminReply", "");

                    db.collection("feedback").add(feedback)
                            .addOnSuccessListener(ref -> {
                                progressBar.setVisibility(View.GONE);
                                showBotMessage("Thanks! We've received your feedback.");
                            });
                });
    }

    private void detectIntent(Context context, String text, IntentCallback callback) {
        String sessionId = UUID.randomUUID().toString();
        String token = getAccessToken(context);
        if (token == null) {
            callback.onIntentDetected("Unknown");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String url = "https://dialogflow.googleapis.com/v2/projects/"
                + DIALOGFLOW_PROJECT_ID + "/agent/sessions/" + sessionId + ":detectIntent";

        try {
            JSONObject textInput = new JSONObject();
            textInput.put("text", text);
            textInput.put("languageCode", "en");

            JSONObject queryInput = new JSONObject();
            queryInput.put("text", textInput);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("queryInput", queryInput);

            RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    showBotMessage("I'm having trouble understanding. Try again later.");
                    callback.onIntentDetected("Unknown");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String res = response.body().string();
                        JSONObject json = new JSONObject(res);
                        String intent = json.getJSONObject("queryResult").getJSONObject("intent").getString("displayName");
                        showBotMessage("Intent detected: " + intent);
                        callback.onIntentDetected(intent);
                    } catch (Exception e) {
                        showBotMessage("Couldn't parse the response.");
                        callback.onIntentDetected("Unknown");
                    }
                }
            });
        } catch (JSONException e) {
            callback.onIntentDetected("Unknown");
        }
    }

    private void analyzeSentiment(String text, SentimentCallback callback) {
        OkHttpClient client = new OkHttpClient();
        try {
            JSONObject document = new JSONObject();
            document.put("type", "PLAIN_TEXT");
            document.put("content", text);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("document", document);
            bodyJson.put("encodingType", "UTF8");

            RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url("https://language.googleapis.com/v1/documents:analyzeSentiment?key=" + NLP_API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onSentimentAnalyzed("Neutral", 0f);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        float score = (float) json.getJSONObject("documentSentiment").getDouble("score");
                        String sentiment = score > 0.25 ? "Positive" : score < -0.25 ? "Negative" : "Neutral";
                        showBotMessage("Sentiment: " + sentiment);
                        callback.onSentimentAnalyzed(sentiment, score);
                    } catch (Exception e) {
                        callback.onSentimentAnalyzed("Neutral", 0f);
                    }
                }
            });
        } catch (Exception e) {
            callback.onSentimentAnalyzed("Neutral", 0f);
        }
    }

    private String getAccessToken(Context context) {
        try {
            InputStream stream = context.getAssets().open("smart-school-pick-up-sys-c73a9-4cb71b2d6df2.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface IntentCallback {
        void onIntentDetected(String intent);
    }

    public interface SentimentCallback {
        void onSentimentAnalyzed(String sentiment, float score);
    }
}
