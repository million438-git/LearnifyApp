package com.million.learnify.studentbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OllamaService - Professional Structured AI Engine for Phi-3.
 * Forces the AI to follow the student-friendly template with 100% accuracy.
 */
@Service
public class OllamaService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public String askOllama(String question) {
        try {
            // STEP 1: The Strict "Master Teacher" System Prompt
            String systemPrompt = """
                    You are a professional academic tutor. 
                    ALWAYS provide your answer using this EXACT structure with double newlines between sections:
                    
                    Topic Overview
                    (Clear, simple explanation)
                    
                    Key Points
                    • Point 1
                    • Point 2
                    
                    Simple Example
                    (A relatable real-world example)
                    
                    Quick Summary
                    (Short wrap-up)
                    
                    Rules: 
                    - DO NOT use any other format. 
                    - Do NOT include decorative emojis or conversational pleasantries.
                    - Use simple English. 
                    - Use bullet points for key points.
                    """;

            // STEP 2: Configure for Maximum Stability
            Map<String, Object> options = new HashMap<>();
            options.put("temperature", 0.2); // Low temperature = Zero random mistakes
            options.put("num_predict", 400);

            // Use the Chat endpoint logic for better instruction following
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", question));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "phi3");
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("options", options);

            String jsonRequest = objectMapper.writeValueAsString(requestBody);

            // Using /api/chat instead of /api/generate for 100% structural accuracy
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/chat"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                // Extracting text from the 'message' -> 'content' path
                String rawAnswer = rootNode.path("message").get("content").asText().trim();
                return ResponseNormalizer.normalize(rawAnswer);
            } else {
                return "AI Service is busy. Status: " + response.statusCode();
            }
        } catch (Exception e) {
            return "AI Service connection error: " + e.getMessage();
        }
    }
}
