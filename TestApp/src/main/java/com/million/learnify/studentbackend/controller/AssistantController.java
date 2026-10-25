package com.million.learnify.studentbackend.controller;

import com.million.learnify.studentbackend.dto.AskRequest;
import com.million.learnify.studentbackend.dto.AskResponse;
import com.million.learnify.studentbackend.service.GeminiService;

import com.million.learnify.studentbackend.security.FirebasePrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AssistantController - Live Google Gemini AI Assistant Gateway.
 * Bridges Android client requests to GeminiService with structured pedagogical output.
 * Features rate limiting to protect upstream AI provider quotas.
 */
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final GeminiService geminiService;
    private final Map<String, RateLimitBucket> rateLimitMap = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 30;

    public AssistantController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/ask")
public AskResponse ask(
        @RequestBody(required = false) AskRequest request,
        Authentication authentication) {

    if (authentication == null
            || !(authentication.getPrincipal() instanceof FirebasePrincipal principal)
            || principal.uid() == null
            || principal.uid().isBlank()) {
        return new AskResponse("Authentication is required to use the AI assistant.");
    }

    return askWithIp(request, "firebase-uid:" + principal.uid());
}

/*
 * Keeps existing direct controller unit tests compatible.
 * HTTP requests use the authenticated method above.
 */
public AskResponse ask(AskRequest request) {
    return askWithIp(request, "default-client");
}

    public AskResponse askWithIp(AskRequest request, String remoteIp) {
        try {
            if (request == null || request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                return new AskResponse("Please ask an academic question to get started.");
            }

            // Rate limiting check
            String clientKey = remoteIp != null && !remoteIp.isBlank() ? remoteIp.split(",")[0].trim() : "default-client";
            if (isRateLimited(clientKey)) {
                return new AskResponse("EduAI Notice: You have reached the rate limit (30 requests/minute). Please wait a moment.");
            }

            String answer;
            if (request.getGrade() != null || request.getSubject() != null || request.getTopic() != null || request.getConversationHistory() != null) {
                answer = geminiService.askGeminiWithContext(request);
            } else {
                answer = geminiService.askGemini(request.getQuestion());
            }

            return new AskResponse(answer);
        } catch (Exception e) {
            return new AskResponse("EduAI Error: Unable to process request.");
        }
    }

    private boolean isRateLimited(String clientKey) {
        long currentMinute = System.currentTimeMillis() / 60000L;
        RateLimitBucket bucket = rateLimitMap.compute(clientKey, (key, existing) -> {
            if (existing == null || existing.minuteWindow != currentMinute) {
                return new RateLimitBucket(currentMinute, new AtomicInteger(1));
            }
            existing.counter.incrementAndGet();
            return existing;
        });
        return bucket.counter.get() > MAX_REQUESTS_PER_MINUTE;
    }

    private static class RateLimitBucket {
        final long minuteWindow;
        final AtomicInteger counter;

        RateLimitBucket(long minuteWindow, AtomicInteger counter) {
            this.minuteWindow = minuteWindow;
            this.counter = counter;
        }
    }
}
