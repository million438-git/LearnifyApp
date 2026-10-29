package com.million.learnify;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import org.json.JSONObject;

public class VoiceAssistantActivity extends AppCompatActivity implements VoiceStudyManager.VoiceCallback {

    private VoiceStudyManager voiceManager;
    private TextView tvStatus, tvQuery, tvResponse;
    private FloatingActionButton fabMic;
    private boolean isListening = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_assistant);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvStatus = findViewById(R.id.tvStatus);
        tvQuery = findViewById(R.id.tvQuery);
        tvResponse = findViewById(R.id.tvResponse);
        fabMic = findViewById(R.id.fabMic);

        voiceManager = new VoiceStudyManager(this, this);

        fabMic.setOnClickListener(v -> {
            if (!isListening) {
                startListening();
            } else {
                stopListening();
            }
        });
    }

    private void startListening() {
        isListening = true;
        tvStatus.setText("Listening...");
        tvStatus.setTextColor(getResources().getColor(R.color.danger_red));
        voiceManager.startListening();
    }

    private void stopListening() {
        isListening = false;
        tvStatus.setText("Processing...");
        tvStatus.setTextColor(getResources().getColor(R.color.primary_indigo));
    }

    @Override
    public void onCommandRecognized(String text) {
        runOnUiThread(() -> {
            tvQuery.setText(text);
            isListening = false;
            tvStatus.setText("Thinking...");
        });
    }

    public void processOffline(String text) {
        new Thread(() -> {
            try {
                if (Python.isStarted()) {
                    Python py = Python.getInstance();
                    PyObject engine = py.getModule("voice_assistant_engine");
                    String jsonResult = engine.callAttr("process_voice_input", text).toString();
                    
                    JSONObject result = new JSONObject(jsonResult);
                    String type = result.optString("type", "chat");
                    String content = result.optString("content", "");

                    runOnUiThread(() -> {
                        tvResponse.setText(content);
                        voiceManager.speak(content);
                        tvStatus.setText("Speaking...");
                    });
                }
            } catch (Exception e) {
                Log.e("VoiceAssistant", "Offline processing error", e);
            }
        }).start();
    }

    @Override
    public void onActionRequired(String action, String page, String extra) {
        runOnUiThread(() -> {
            if ("navigate".equals(action) && page != null) {
                try {
                    Class<?> targetClass = Class.forName("com.million.learnify." + page);
                    android.content.Intent intent = new android.content.Intent(this, targetClass);
                    if (extra != null) intent.putExtra("SUBJECT_NAME", extra);
                    tvResponse.setText("Opening " + (extra != null ? extra : page) + "...");
                    tvStatus.setText("Navigating...");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("VoiceAssistant", "Navigation failed", e);
                }
            } else if ("answer".equals(action) && extra != null) {
                tvResponse.setText(extra);
                tvStatus.setText("Answer ready");
            } else if ("chat".equals(action) && extra != null) {
                tvResponse.setText(extra);
                tvStatus.setText("Speaking...");
            } else if ("command".equals(action) && page != null) {
                handleQuickCommand(page);
            }
        });
    }

    private void handleQuickCommand(String cmd) {
        if ("home".equalsIgnoreCase(cmd) || "dashboard".equalsIgnoreCase(cmd)) {
            startActivity(new android.content.Intent(this, DashboardActivity.class));
        } else if ("notes".equalsIgnoreCase(cmd)) {
            startActivity(new android.content.Intent(this, SubjectActivity.class));
        } else if ("settings".equalsIgnoreCase(cmd)) {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
        }
    }

    @Override
    public void onSpeechFinished() {
        runOnUiThread(() -> {
            tvStatus.setText("Ready to help");
            tvStatus.setTextColor(getResources().getColor(R.color.primary_indigo));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceManager != null) {
            voiceManager.shutdown();
        }
    }
}
