package com.million.learnify;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * GpaCalculatorActivity - Unified Academic Analysis.
 * Updated: Now uses unified BuildConfig API Key for consistent EduAI intelligence.
 */
public class GpaCalculatorActivity extends AppCompatActivity {

    private LinearLayout llSubjectsList;
    private TextView tvGpaResult, tvGpaStatusLabel, tvGpaMessage, tvGpaSummaryText, tvAiResponse;
    private View viewStatusIndicator;
    private MaterialCardView cardResult, cardSummary;
    private MaterialButton btnAiInsight;
    private List<View> subjectRows = new ArrayList<>();

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpa_calculator);

        setupToolbar();
        initViews();
        addSubjectRow(); // Add first row automatically
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.gpaToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        llSubjectsList = findViewById(R.id.llSubjectsList);
        tvGpaResult = findViewById(R.id.tvGpaResult);
        tvGpaStatusLabel = findViewById(R.id.tvGpaStatusLabel);
        tvGpaMessage = findViewById(R.id.tvGpaMessage);
        tvGpaSummaryText = findViewById(R.id.tvGpaSummaryText);
        tvAiResponse = findViewById(R.id.tvAiResponse);
        viewStatusIndicator = findViewById(R.id.viewStatusIndicator);
        cardResult = findViewById(R.id.cardResult);
        cardSummary = findViewById(R.id.cardSummary);
        btnAiInsight = findViewById(R.id.btnAiInsight);

        findViewById(R.id.btnAddSubject).setOnClickListener(v -> addSubjectRow());
        findViewById(R.id.btnCalculateGpa).setOnClickListener(v -> calculateGPA());
        btnAiInsight.setOnClickListener(v -> getAiAdvice());
    }

    private void addSubjectRow() {
        View row = LayoutInflater.from(this).inflate(R.layout.item_gpa_row, llSubjectsList, false);
        
        AutoCompleteTextView actvCredits = row.findViewById(R.id.actvCredits);
        String[] credits = {"1", "2", "3", "4", "5"};
        actvCredits.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, credits));

        AutoCompleteTextView actvGrade = row.findViewById(R.id.actvGrade);
        String[] grades = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F"};
        actvGrade.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, grades));

        row.findViewById(R.id.btnRemoveRow).setOnClickListener(v -> {
            if (subjectRows.size() > 1) {
                llSubjectsList.removeView(row);
                subjectRows.remove(row);
            } else {
                Toast.makeText(this, "At least one course is required", Toast.LENGTH_SHORT).show();
            }
        });

        llSubjectsList.addView(row);
        subjectRows.add(row);
    }

    private void calculateGPA() {
        double totalPoints = 0;
        int totalCredits = 0;
        int courseCount = 0;
        double maxP = -1.0;
        double minP = 5.0;
        String highGrade = "N/A";
        String lowGrade = "N/A";

        for (View row : subjectRows) {
            AutoCompleteTextView actvCredits = row.findViewById(R.id.actvCredits);
            AutoCompleteTextView actvGrade = row.findViewById(R.id.actvGrade);

            String creditStr = actvCredits.getText().toString();
            String grade = actvGrade.getText().toString();

            if (!creditStr.isEmpty() && !grade.isEmpty()) {
                int credits = Integer.parseInt(creditStr);
                double points = getGradePoints(grade);
                totalPoints += (points * credits);
                totalCredits += credits;
                courseCount++;

                if (points > maxP) { maxP = points; highGrade = grade; }
                if (points < minP) { minP = points; lowGrade = grade; }
            }
        }

        if (totalCredits > 0) {
            double gpa = totalPoints / totalCredits;
            displayResults(gpa, totalCredits, courseCount, highGrade, lowGrade);
        } else {
            Toast.makeText(this, "Please select credits and grades for your courses", Toast.LENGTH_SHORT).show();
        }
    }

    private double getGradePoints(String grade) {
        switch (grade) {
            case "A+": case "A": return 4.00;
            case "A-": return 3.75;
            case "B+": return 3.50;
            case "B": return 3.00;
            case "B-": return 2.75;
            case "C+": return 2.50;
            case "C": return 2.00;
            case "C-": return 1.75;
            case "D+": return 1.30;
            case "D": return 1.00;
            case "D-": return 0.70;
            default: return 0.00;
        }
    }

    private void displayResults(double gpa, int credits, int count, String high, String low) {
        cardResult.setVisibility(View.VISIBLE);
        cardSummary.setVisibility(View.VISIBLE);
        btnAiInsight.setVisibility(View.VISIBLE);

        tvGpaResult.setText(String.format("%.2f", gpa));
        String summary = "• Total Courses: " + count + 
                         "\n• Total Credits: " + credits + 
                         "\n• Highest Grade: " + high + 
                         "\n• Lowest Grade: " + low;
        tvGpaSummaryText.setText(summary);

        if (gpa >= 3.70) {
            tvGpaStatusLabel.setText("Status: Excellent");
            tvGpaMessage.setText("🌟 You are performing outstandingly. Keep it up!");
            viewStatusIndicator.setBackgroundColor(Color.parseColor("#10B981")); // Green
        } else if (gpa >= 3.00) {
            tvGpaStatusLabel.setText("Status: Very Good");
            tvGpaMessage.setText("🔵 Great job! You are on the right track.");
            viewStatusIndicator.setBackgroundColor(Color.parseColor("#3B82F6")); // Blue
        } else if (gpa >= 2.50) {
            tvGpaStatusLabel.setText("Status: Good");
            tvGpaMessage.setText("🟡 Good effort. Focus a bit more to reach the top!");
            viewStatusIndicator.setBackgroundColor(Color.parseColor("#F59E0B")); // Yellow
        } else if (gpa >= 2.00) {
            tvGpaStatusLabel.setText("Status: Satisfactory");
            tvGpaMessage.setText("🟠 You are passing, but there is room for improvement.");
            viewStatusIndicator.setBackgroundColor(Color.parseColor("#FB923C")); // Orange
        } else if (gpa >= 1.00) {
            tvGpaStatusLabel.setText("Status: Warning");
            tvGpaMessage.setText("🔴 Your performance is below average. Seek help soon.");
            viewStatusIndicator.setBackgroundColor(Color.parseColor("#EF4444")); // Red
        } else {
            tvGpaStatusLabel.setText("Status: Probation");
            tvGpaMessage.setText("⚫ Critical focus required. Please consult your advisor.");
            viewStatusIndicator.setBackgroundColor(Color.parseColor("#111827")); // Dark
        }
    }

    private void getAiAdvice() {
        tvAiResponse.setVisibility(View.VISIBLE);
        tvAiResponse.setText("🤖 EduAI is analyzing your results...");

        String gpaVal = tvGpaResult.getText().toString();
        String summary = tvGpaSummaryText.getText().toString();
        
        String prompt = "Analyze this student's GPA results:\n" + 
                        "Current GPA: " + gpaVal + "\n" + 
                        summary + "\n" +
                        "Give clear, professional advice to improve or maintain this performance. Be highly motivating and student-friendly.";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-4o-mini");
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "user").put("content", prompt));
            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> tvAiResponse.setText("🤖 I couldn't reach my brain right now. Please try again soon!"));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject jsonResp = new JSONObject(response.body().string());
                            String advice = jsonResp.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                            runOnUiThread(() -> tvAiResponse.setText("💡 EduAI Insight:\n" + advice));
                        } catch (Exception e) {
                            runOnUiThread(() -> tvAiResponse.setText("🤖 I had trouble understanding your results. Try again!"));
                        }
                    } else {
                        runOnUiThread(() -> tvAiResponse.setText("🤖 Service temporarily unavailable. Using Offline Fallback: Keep studying hard!"));
                    }
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }
}
