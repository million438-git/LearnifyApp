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
            
            // Process with Offline Engine (Python BM25)
            processOffline(text);
        });
    }

    private void processOffline(String text) {
        new Thread(() -> {
            try {
                if (Python.isStarted()) {
                    Python py = Python.getInstance();
                    PyObject engine = py.getModule("voice_assistant_engine");
                    String jsonResult = engine.callAttr("process_voice_input", text).toString();
                    
                    JSONObject result = new JSONObject(jsonResult);
                    String type = result.getString("type");
                    String content = result.getString("content");

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
        // Handled by manager or custom logic
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
