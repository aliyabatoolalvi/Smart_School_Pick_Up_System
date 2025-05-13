package com.finallab.smartschoolpickupsystem;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
public class SentimentAnalyzer {


        private static final String API_KEY = "AIzaSyBBujRr2r9d-1jxQxTswDtaM2v8h1rGx50"; // Replace this
        private static final String API_URL = "https://language.googleapis.com/v1/documents:analyzeSentiment?key=" + API_KEY;

    public interface SentimentCallback {
        void onResult(String sentimentLabel, float score);
    }

    public static void analyze(String text, SentimentCallback callback) {
        OkHttpClient client = new OkHttpClient();

        try {
            // Create JSON request body
            JSONObject document = new JSONObject();
            document.put("type", "PLAIN_TEXT");
            document.put("content", text);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("document", document);
            bodyJson.put("encodingType", "UTF8");

            RequestBody body = RequestBody.create(bodyJson.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    callback.onResult("Neutral", 0); // fallback
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        float score = (float) json.getJSONObject("documentSentiment").getDouble("score");

                        String label;
                        if (score > 0.25) label = "Positive";
                        else if (score < -0.25) label = "Negative";
                        else label = "Neutral";

                        callback.onResult(label, score);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onResult("Neutral", 0);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            callback.onResult("Neutral", 0);
        }
    }
}