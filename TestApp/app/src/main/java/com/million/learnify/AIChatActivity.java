package com.million.learnify;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * EduAI - PROFESSIONAL STUDENT ASSISTANT
 * Fully integrated with ViewModel for data preservation and OpenAI for intelligence.
 * Includes advanced error reporting for API stability.
 */
public class AIChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private ChatViewModel viewModel;
    private EditText etChatInput;
    private View viewStatusDot, emptyStateView;
    private TextView tvStatusText;
    private ImageButton btnSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        // Initialize ViewModel (The Brain)
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        initViews();
        setupToolbar();
        observeViewModel();
        updateAIStatus(true, "EduAI Online");
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etChatInput = findViewById(R.id.etChatInput);
        viewStatusDot = findViewById(R.id.viewStatusDot);
        tvStatusText = findViewById(R.id.tvStatusText);
        emptyStateView = findViewById(R.id.emptyStateView);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        chatAdapter = new ChatAdapter(new ArrayList<>(), null);
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
        findViewById(R.id.chipExplain).setOnClickListener(v -> prefillAndSend("Explain Photosynthesis step-by-step"));
        findViewById(R.id.chipMath).setOnClickListener(v -> prefillAndSend("How to solve quadratic equations?"));
        findViewById(R.id.chipCode).setOnClickListener(v -> prefillAndSend("Java class example"));
        findViewById(R.id.chipNotes).setOnClickListener(v -> prefillAndSend("Summarize the Water Cycle"));
    }

    private void prefillAndSend(String text) {
        etChatInput.setText(text);
        handleSendMessage();
    }

    private void handleSendMessage() {
        String query = etChatInput.getText().toString().trim();
        if (query.isEmpty()) return;

        viewModel.sendMessage(query);
        etChatInput.setText("");
    }

    private void observeViewModel() {
        // Observe messages and update UI
        viewModel.messages.observe(this, messages -> {
            if (!messages.isEmpty()) {
                emptyStateView.setVisibility(View.GONE);
                rvChat.setVisibility(View.VISIBLE);
                
                chatAdapter = new ChatAdapter(messages, null);
                rvChat.setAdapter(chatAdapter);
                rvChat.scrollToPosition(messages.size() - 1);
            }
        });

        // Observe loading state
        viewModel.isLoading.observe(this, loading -> {
            btnSendMessage.setEnabled(!loading);
            btnSendMessage.setAlpha(loading ? 0.5f : 1.0f);
            if (loading) updateAIStatus(true, "EduAI is processing...");
            else updateAIStatus(true, "EduAI Online");
        });

        // Observe errors
        viewModel.error.observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateAIStatus(boolean online, String text) {
        tvStatusText.setText(text);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(online ? Color.parseColor("#10B981") : Color.RED);
        viewStatusDot.setBackground(dot);
    }

    // --- API INTERFACE & MODELS ---

    public interface OpenAIService {
        @POST("v1/chat/completions")
        Call<ChatResponse> getChatCompletion(@Header("Authorization") String auth, @Body ChatRequest request);
    }

    public static class ChatRequest {
        public String model;
        public List<OpenAIMessage> messages;
        public ChatRequest(String model, List<OpenAIMessage> messages) {
            this.model = model;
            this.messages = messages;
        }
    }

    public static class OpenAIMessage {
        public String role;
        public String content;
        public OpenAIMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class ChatResponse {
        public List<Choice> choices;
        public static class Choice {
            public OpenAIMessage message;
        }
    }
}
