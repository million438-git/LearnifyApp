package com.million.learnify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * AIChatActivity - Refined EduAI Academic Tutor.
 * Features:
 * - Persistent student conversation history via SQLite
 * - STEM pedagogical structure with Markdown and code rendering
 * - Inclusive TextToSpeech read-aloud via InclusiveContentSpeaker
 * - Contextual curriculum routing (Grade, Subject, Topic)
 * - Transparent online / offline status indicators
 */
public class AIChatActivity extends BaseActivity implements ChatAdapter.OnSpeechListener {

    private RecyclerView rvChat;
    private ChatAdapter chatAdapter;
    private ChatViewModel viewModel;
    private EditText etChatInput;
    private View viewStatusDot, emptyStateView;
    private TextView tvStatusText, tvStudentSubtitle;
    private View btnSendMessage;
    private ImageButton btnClearChat;

    private String currentUsername = "Student";
    private String subjectContext;
    private String gradeContext;
    private String topicContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SECURITY: Only allow Student users to enter this portal.
        enforceRole("STUDENT");

        setContentView(R.layout.activity_ai_chat);

        // Load student session context
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");
        subjectContext = getIntent().getStringExtra("SUBJECT_NAME");
        gradeContext = getIntent().getStringExtra("GRADE_NAME");
        topicContext = getIntent().getStringExtra("TOPIC_NAME");

        // Initialize ViewModel (The Brain)
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        initViews();
        setupToolbar();
        observeViewModel();

        // Load persistent student conversation
        viewModel.loadHistory(currentUsername);
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etChatInput = findViewById(R.id.etChatInput);
        viewStatusDot = findViewById(R.id.viewStatusDot);
        tvStatusText = findViewById(R.id.tvStatusText);
        emptyStateView = findViewById(R.id.emptyStateView);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnClearChat = findViewById(R.id.btnClearChat);
        tvStudentSubtitle = findViewById(R.id.tvStudentSubtitle);

        if (subjectContext != null && !subjectContext.isBlank()) {
            tvStudentSubtitle.setText("Subject focus: " + subjectContext + (topicContext != null ? " • " + topicContext : ""));
        }

        chatAdapter = new ChatAdapter(new ArrayList<>(), this);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        btnSendMessage.setOnClickListener(v -> handleSendMessage());
        if (btnClearChat != null) {
            btnClearChat.setOnClickListener(v -> showClearConversationDialog());
        }

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSuggestions() {
        findViewById(R.id.chipExplain).setOnClickListener(v -> prefillAndSend("Explain Photosynthesis step-by-step"));
        findViewById(R.id.chipMath).setOnClickListener(v -> prefillAndSend("How to solve quadratic equations?"));
        findViewById(R.id.chipPhysics).setOnClickListener(v -> prefillAndSend("Explain Newton's Second Law with examples"));
        findViewById(R.id.chipChemistry).setOnClickListener(v -> prefillAndSend("What is acid-base titration?"));
    }

    private void prefillAndSend(String text) {
        etChatInput.setText(text);
        handleSendMessage();
    }

    private void handleSendMessage() {
        String query = etChatInput.getText().toString().trim();
        if (query.isEmpty()) return;

        if (viewModel.sendMessage(query, currentUsername, gradeContext, subjectContext, topicContext)) {
            etChatInput.setText("");
        }
    }

    private void showClearConversationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Conversation?")
                .setMessage("This will remove all saved messages for this study session.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    viewModel.clearHistory(currentUsername);
                    Toast.makeText(this, "Conversation cleared", Toast.LENGTH_SHORT).show();
                    AccessibilityUtils.announceForAccessibility(rvChat, "Conversation cleared");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.messages.observe(this, messages -> {
            if (messages != null && !messages.isEmpty()) {
                emptyStateView.setVisibility(View.GONE);
                rvChat.setVisibility(View.VISIBLE);

                chatAdapter = new ChatAdapter(messages, this);
                rvChat.setAdapter(chatAdapter);
                rvChat.scrollToPosition(messages.size() - 1);
            } else {
                emptyStateView.setVisibility(View.VISIBLE);
                rvChat.setVisibility(View.GONE);
            }
        });

        viewModel.isLoading.observe(this, loading -> {
            btnSendMessage.setEnabled(!loading);
            btnSendMessage.setAlpha(loading ? 0.5f : 1.0f);
        });

        viewModel.serviceStatus.observe(this, this::updateAIStatus);

        viewModel.error.observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateAIStatus(String text) {
        if (tvStatusText != null) {
            tvStatusText.setText(text);
        }
        if (viewStatusDot != null) {
            if ("EduAI is analyzing your question...".equals(text) || "EduAI is researching...".equals(text)) {
                viewStatusDot.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_dot_busy));
                tvStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_light));
            } else if (text != null && text.contains("Offline")) {
                viewStatusDot.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_dot_offline));
                tvStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            } else {
                viewStatusDot.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_status_dot_ready));
                tvStatusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
            }
        }
    }

    @Override
    public void onSpeak(String text) {
        if (text == null || text.isBlank()) return;
        getAccessibilitySpeaker().speakContent(text);
        AccessibilityUtils.announceForAccessibility(rvChat, "Reading explanation aloud");
    }

    @Override
    protected void onPause() {
        super.onPause();
        getAccessibilitySpeaker().stop();
    }

    public interface AssistantService {
        @POST("api/assistant/ask")
        Call<AssistantResponse> ask(@Body AssistantRequest request);
    }

    public static class AssistantRequest {
        public String question;
        public String grade;
        public String subject;
        public String topic;

        public AssistantRequest() {}

        public AssistantRequest(String question) {
            this.question = question;
        }

        public AssistantRequest(String question, String grade, String subject, String topic) {
            this.question = question;
            this.grade = grade;
            this.subject = subject;
            this.topic = topic;
        }
    }

    public static class AssistantResponse {
        public String answer;
        public AssistantResponse() {}
        public AssistantResponse(String answer) {
            this.answer = answer;
        }
    }
}
