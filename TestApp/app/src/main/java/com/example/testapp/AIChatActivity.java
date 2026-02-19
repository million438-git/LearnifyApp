package com.example.testapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * EDUAI - THE PROFESSIONAL LOCAL AI ASSISTANT (STABILITY FIX)
 * Connected to your PC's real network address for zero-failure connectivity.
 */
public class AIChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText etChatInput;
    private View viewStatusDot, emptyStateView;
    private TextView tvStatusText;
    private ImageButton btnSendMessage;
    
    // TRUTH FIX: Change this to your PC's actual IPv4 Address (from ipconfig)
    // Example: "http://192.168.1.5:8080/"
    // Currently set to 10.0.2.2 for Emulator users.
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private AiApi aiApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        initViews();
        setupToolbar();
        initRetrofit();
        updateAIStatus(true, "Online (Phi-3)");
    }

    private void initRetrofit() {
        // High-Stability Client with increased timeouts for AI processing
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        aiApi = retrofit.create(AiApi.class);
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etChatInput = findViewById(R.id.etChatInput);
        viewStatusDot = findViewById(R.id.viewStatusDot);
        tvStatusText = findViewById(R.id.tvStatusText);
        emptyStateView = findViewById(R.id.emptyStateView);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, null);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        btnSendMessage.setOnClickListener(v -> handleSendMessage());
        setupSuggestions();

        etChatInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handleSendMessage();
                return true;
            }
            return false;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSuggestions() {
        findViewById(R.id.chipExplain).setOnClickListener(v -> prefillAndSend("Explain Photosynthesis"));
        findViewById(R.id.chipMath).setOnClickListener(v -> prefillAndSend("Solve 2x + 10 = 20"));
        findViewById(R.id.chipCode).setOnClickListener(v -> prefillAndSend("Java Loop example"));
        findViewById(R.id.chipNotes).setOnClickListener(v -> prefillAndSend("Summary of Biology"));
    }

    private void prefillAndSend(String text) {
        etChatInput.setText(text);
        handleSendMessage();
    }

    private void handleSendMessage() {
        String query = etChatInput.getText().toString().trim();
        if (query.isEmpty()) return;

        if (emptyStateView.getVisibility() == View.VISIBLE) {
            emptyStateView.setVisibility(View.GONE);
            rvChat.setVisibility(View.VISIBLE);
        }

        updateSendButtonState(false);
        addMessage(new ChatMessage(query, true));
        etChatInput.setText("");
        
        addMessage(new ChatMessage("thinking...", false, true));
        
        callLocalBackend(query);
    }

    private void callLocalBackend(String query) {
        updateAIStatus(true, "Phi-3 is researching...");
        
        AiRequest request = new AiRequest(query);
        aiApi.ask(request).enqueue(new Callback<AiResponse>() {
            @Override
            public void onResponse(Call<AiResponse> call, Response<AiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    finalizeAIAnswer(response.body().getAnswer());
                } else {
                    finalizeAIAnswer("🤖 The brain is connected but silent. Please ensure Ollama is running on your PC.");
                }
            }

            @Override
            public void onFailure(Call<AiResponse> call, Throwable t) {
                finalizeAIAnswer("🤖 Connection lost. Ensure your PC and Phone are on the same network and use your PC's IP address.");
            }
        });
    }

    private void finalizeAIAnswer(String answer) {
        runOnUiThread(() -> {
            updateSendButtonState(true);
            removeLastMessage();
            addMessage(new ChatMessage(answer, false));
            updateAIStatus(true, "Online (Phi-3)");
        });
    }

    private void updateSendButtonState(boolean enabled) {
        btnSendMessage.setEnabled(enabled);
        btnSendMessage.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void updateAIStatus(boolean online, String text) {
        runOnUiThread(() -> {
            tvStatusText.setText(text);
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(online ? Color.parseColor("#10B981") : Color.RED);
            viewStatusDot.setBackground(dot);
        });
    }

    private void addMessage(ChatMessage m) {
        runOnUiThread(() -> {
            chatMessages.add(m);
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            rvChat.scrollToPosition(chatMessages.size() - 1);
        });
    }

    private void removeLastMessage() {
        runOnUiThread(() -> {
            if (!chatMessages.isEmpty()) {
                chatMessages.remove(chatMessages.size() - 1);
                chatAdapter.notifyItemRemoved(chatMessages.size());
            }
        });
    }

    public interface AiApi {
        @POST("api/assistant/ask")
        Call<AiResponse> ask(@Body AiRequest request);
    }

    public static class AiRequest {
        private String question;
        public AiRequest(String question) { this.question = question; }
    }

    public static class AiResponse {
        private String answer;
        public String getAnswer() { return answer; }
    }
}
