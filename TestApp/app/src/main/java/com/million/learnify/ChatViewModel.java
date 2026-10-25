package com.million.learnify;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ChatViewModel - The Central Controller for EduAI.
 * Features:
 * - SQLite persistent conversation history
 * - Student & curriculum context propagation (Grade, Subject, Topic)
 * - Offline study engine fallback (OfflineAiEngine)
 * - Live service readiness observation
 */
public class ChatViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ChatMessage>> _messages = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<ChatMessage>> messages = _messages;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>(null);
    public final LiveData<String> error = _error;

    private final MutableLiveData<String> _serviceStatus = new MutableLiveData<>("Ready to help");
    public final LiveData<String> serviceStatus = _serviceStatus;

    private final AIChatActivity.AssistantService assistantService;
    private final OfflineAiEngine offlineAiEngine;
    private final DatabaseHelper dbHelper;
    private final String assistantConfigurationError;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        this.offlineAiEngine = new OfflineAiEngine(application);
        this.dbHelper = new DatabaseHelper(application);

        AIChatActivity.AssistantService configuredService = null;
        String configurationError = null;
        try {
            configuredService = AssistantApiClient.create(
                    BuildConfig.LEARNIFY_API_BASE_URL,
                    BuildConfig.DEBUG,
                    new FirebaseBackendTokenProvider(application));
        } catch (IllegalArgumentException | IllegalStateException e) {
            configurationError = e.getMessage();
        }
        assistantService = configuredService;
        assistantConfigurationError = configurationError;
    }

    public void loadHistory(String username) {
        if (username == null) return;
        List<ChatMessage> saved = dbHelper.getChatHistory(username);
        _messages.setValue(saved != null ? saved : new ArrayList<>());
    }

    public void clearHistory(String username) {
        if (username != null) {
            dbHelper.clearChatHistory(username);
        }
        _messages.setValue(new ArrayList<>());
    }

    public boolean sendMessage(String query) {
        return sendMessage(query, "Student", null, null, null);
    }

    public boolean sendMessage(String query, String username, String grade, String subject, String topic) {
        if (query == null || query.trim().isEmpty()) return false;

        String safeUser = (username != null && !username.isBlank()) ? username : "Student";

        List<ChatMessage> currentMessages = new ArrayList<>(_messages.getValue() != null ? _messages.getValue() : new ArrayList<>());
        ChatMessage userMsg = new ChatMessage(query, true);
        currentMessages.add(userMsg);
        _messages.setValue(currentMessages);

        // Persist student message in SQLite
        dbHelper.saveChatMessage(safeUser, query, true);

        _isLoading.setValue(true);
        _error.setValue(null);
        _serviceStatus.setValue("EduAI is researching...");

        // Add "Thinking..." placeholder
        currentMessages.add(new ChatMessage("EduAI is analyzing your question...", false, true));
        _messages.setValue(currentMessages);

        if (assistantService == null) {
            fallbackToOffline(query, safeUser, assistantConfigurationError != null ? assistantConfigurationError : "Cloud AI service unconfigured.");
            return true;
        }

        AIChatActivity.AssistantRequest request = new AIChatActivity.AssistantRequest(query.trim(), grade, subject, topic);

        assistantService.ask(request).enqueue(new Callback<AIChatActivity.AssistantResponse>() {
            @Override
            public void onResponse(Call<AIChatActivity.AssistantResponse> call, Response<AIChatActivity.AssistantResponse> response) {
                _isLoading.postValue(false);
                _serviceStatus.postValue("Ready to help");

                List<ChatMessage> updatedMessages = new ArrayList<>(_messages.getValue());
                if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).isThinking()) {
                    updatedMessages.remove(updatedMessages.size() - 1);
                }

                if (response.isSuccessful() && response.body() != null && response.body().answer != null && !response.body().answer.isBlank()) {
                    String answer = AIResponseNormalizer.normalize(response.body().answer);
                    if (isServiceStatusMessage(answer)) {
                        _error.postValue(answer);
                        // Also show helpful fallback content so student is not stranded
                        String offlineAnswer = AIResponseNormalizer.normalize(offlineAiEngine.searchLocalKnowledge(query));
                        ChatMessage aiMsg = new ChatMessage(offlineAnswer + "\n\n(Note: Cloud AI reported: " + answer + ". Showing offline lesson notes.)", false);
                        updatedMessages.add(aiMsg);
                        dbHelper.saveChatMessage(safeUser, aiMsg.getMessage(), false);
                    } else {
                        ChatMessage aiMsg = new ChatMessage(answer, false);
                        updatedMessages.add(aiMsg);
                        dbHelper.saveChatMessage(safeUser, answer, false);
                    }
                } else {
                    String reason;
                    if (response.code() == 401) {
                        reason = "Your secure session could not be verified. Sign in again for online AI.";
                        _error.postValue(reason);
                    } else if (response.code() == 403) {
                        reason = "Verify your email before using online AI.";
                        _error.postValue(reason);
                    } else {
                        reason = "Server response was empty or invalid.";
                    }
                    fallbackToOffline(query, safeUser, reason);
                    return;
                }
                _messages.postValue(updatedMessages);
            }

            @Override
            public void onFailure(Call<AIChatActivity.AssistantResponse> call, Throwable t) {
                Throwable cause = t;
                while (cause.getCause() != null && cause.getCause() != cause) {
                    cause = cause.getCause();
                }
                String reason = cause instanceof SessionAuthenticationException
                        ? cause.getMessage()
                        : "Network connection unavailable.";
                if (cause instanceof SessionAuthenticationException) {
                    _error.postValue(reason);
                }
                fallbackToOffline(query, safeUser, reason);
            }
        });
        return true;
    }

    private void fallbackToOffline(String query, String safeUser, String reason) {
        _isLoading.postValue(false);
        _serviceStatus.postValue("Offline Study Mode");

        List<ChatMessage> updatedMessages = new ArrayList<>(_messages.getValue() != null ? _messages.getValue() : new ArrayList<>());
        if (!updatedMessages.isEmpty() && updatedMessages.get(updatedMessages.size() - 1).isThinking()) {
            updatedMessages.remove(updatedMessages.size() - 1);
        }

        String offlineAnswer = AIResponseNormalizer.normalize(offlineAiEngine.searchLocalKnowledge(query));
        String fullMessage = offlineAnswer + "\n\n(Note: Offline Study Mode active — " + reason + ")";
        ChatMessage offlineMsg = new ChatMessage(fullMessage, false);
        updatedMessages.add(offlineMsg);
        dbHelper.saveChatMessage(safeUser, fullMessage, false);

        _messages.postValue(updatedMessages);
    }

    private boolean isServiceStatusMessage(String answer) {
        return answer.startsWith("EduAI Error:")
                || answer.startsWith("EduAI Notice:")
                || answer.startsWith("EduAI Configuration Notice:");
    }
}
