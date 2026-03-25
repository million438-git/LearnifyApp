package com.million.learnify;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ChatViewModel - THE BRAIN OF EduAI
 * Updated with Hybrid Offline AI support.
 */
public class ChatViewModel extends AndroidViewModel {

    private final MutableLiveData<List<ChatMessage>> _messages = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<ChatMessage>> messages = _messages;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>(null);
    public final LiveData<String> error = _error;

    private final AIChatActivity.OpenAIService openAIService;
    private final OfflineAiEngine offlineAiEngine;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        offlineAiEngine = new OfflineAiEngine(application);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        openAIService = retrofit.create(AIChatActivity.OpenAIService.class);
    }

    public void sendMessage(String query) {
        if (query == null || query.trim().isEmpty()) return;

        List<ChatMessage> currentMessages = new ArrayList<>(_messages.getValue());
        currentMessages.add(new ChatMessage(query, true));
        _messages.setValue(currentMessages);

        _isLoading.setValue(true);
        _error.setValue(null);

        // Add "Thinking..." placeholder
        currentMessages.add(new ChatMessage("EduAI is researching...", false, true));
        _messages.setValue(currentMessages);

        String rawKey = BuildConfig.OPENAI_API_KEY;
        String apiKey = "Bearer " + rawKey;

        List<AIChatActivity.OpenAIMessage> apiMessages = new ArrayList<>();
        apiMessages.add(new AIChatActivity.OpenAIMessage("system", "You are a professional academic assistant. Provide structured, helpful answers for students."));
        apiMessages.add(new AIChatActivity.OpenAIMessage("user", query));

        AIChatActivity.ChatRequest request = new AIChatActivity.ChatRequest("gpt-4o-mini", apiMessages);

        openAIService.getChatCompletion(apiKey, request).enqueue(new Callback<AIChatActivity.ChatResponse>() {
            @Override
            public void onResponse(Call<AIChatActivity.ChatResponse> call, Response<AIChatActivity.ChatResponse> response) {
                _isLoading.postValue(false);
                List<ChatMessage> updatedMessages = new ArrayList<>(_messages.getValue());
                if (!updatedMessages.isEmpty()) updatedMessages.remove(updatedMessages.size() - 1);

                if (response.isSuccessful() && response.body() != null) {
                    String answer = response.body().choices.get(0).message.content;
                    updatedMessages.add(new ChatMessage(answer, false));
                } else {
                    // FALLBACK TO OFFLINE AI ENGINE
                    String offlineAnswer = offlineAiEngine.searchLocalKnowledge(query);
                    updatedMessages.add(new ChatMessage(offlineAnswer + "\n\n(Note: Using Offline Tutor Lite as Cloud AI is currently unavailable)", false));
                }
                _messages.postValue(updatedMessages);
            }

            @Override
            public void onFailure(Call<AIChatActivity.ChatResponse> call, Throwable t) {
                _isLoading.postValue(false);
                List<ChatMessage> updatedMessages = new ArrayList<>(_messages.getValue());
                if (!updatedMessages.isEmpty()) updatedMessages.remove(updatedMessages.size() - 1);
                
                // FALLBACK TO OFFLINE AI ENGINE ON NETWORK FAILURE
                String offlineAnswer = offlineAiEngine.searchLocalKnowledge(query);
                updatedMessages.add(new ChatMessage(offlineAnswer + "\n\n(Note: You are currently offline. Using Local Study Partner)", false));

                _messages.postValue(updatedMessages);
            }
        });
    }
}
