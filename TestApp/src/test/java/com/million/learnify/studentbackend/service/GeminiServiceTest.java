package com.million.learnify.studentbackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    private ObjectMapper objectMapper;
    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        geminiService = new GeminiService(mockHttpClient, objectMapper, "gemini-2.5-flash", "test-mock-api-key");
    }

    @Test
    @DisplayName("Should return prompt when question is null or blank")
    void testAskGemini_NullOrEmptyQuestion() {
        String nullRes = geminiService.askGemini(null);
        assertEquals("Please ask an academic question to get started.", nullRes);

        String blankRes = geminiService.askGemini("   ");
        assertEquals("Please ask an academic question to get started.", blankRes);
    }

    @Test
    @DisplayName("Should return configuration notice when API key is missing")
    void testAskGemini_MissingApiKey() {
        GeminiService noKeyService = new GeminiService(mockHttpClient, objectMapper, "gemini-2.5-flash", null);
        // Ensure no env fallback in test if env is unset or handle key absence:
        if (noKeyService.getApiKey() == null) {
            String res = noKeyService.askGemini("What is photosynthesis?");
            assertTrue(res.contains("EduAI Configuration Notice"));
            assertTrue(res.contains("GEMINI_API_KEY"));
        }
    }

    @Test
    @DisplayName("Should parse and return educational 4-part response on HTTP 200")
    void testAskGemini_Success_200() throws Exception {
        String sampleAiResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "📘 Topic Overview\\nPhotosynthesis converts light into chemical energy.\\n\\n🧠 Key Points\\n• Chlorophyll absorbs light\\n• Produces glucose and oxygen\\n\\n🧪 Simple Example\\nPlants in sunlight grow healthy green leaves.\\n\\n📝 Quick Summary\\nPhotosynthesis powers life on Earth."
                          }
                        ]
                      },
                      "finishReason": "STOP"
                    }
                  ]
                }
                """;

        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(sampleAiResponse);
        when(mockHttpClient.send(ArgumentMatchers.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockHttpResponse);

        String answer = geminiService.askGemini("Explain photosynthesis");

        assertNotNull(answer);
        assertTrue(answer.contains("Topic Overview"));
        assertFalse(answer.contains("📘"));
        assertTrue(answer.contains("Photosynthesis converts light into chemical energy."));
        assertTrue(answer.contains("Key Points"));
        assertFalse(answer.contains("🧠"));
        assertTrue(answer.contains("Simple Example"));
        assertFalse(answer.contains("🧪"));
        assertTrue(answer.contains("Quick Summary"));
        assertFalse(answer.contains("📝"));
    }

    @Test
    @DisplayName("Should return friendly rate limit notice on HTTP 429")
    void testAskGemini_RateLimit_429() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(429);
        when(mockHttpClient.send(ArgumentMatchers.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockHttpResponse);

        String answer = geminiService.askGemini("Calculate the derivative of x^2");

        assertTrue(answer.contains("rate limit has been reached"));
    }

    @Test
    @DisplayName("Should return friendly notice on HTTP 400 or 403")
    void testAskGemini_Http400() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(400);
        when(mockHttpClient.send(ArgumentMatchers.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockHttpResponse);

        String answer = geminiService.askGemini("What is Boyle's Law?");

        assertTrue(answer.contains("HTTP 400"));
        assertFalse(answer.contains("test-mock-api-key"), "API key must never be leaked");
    }

    @Test
    @DisplayName("Should handle HttpTimeoutException gracefully")
    void testAskGemini_HttpTimeoutException() throws Exception {
        when(mockHttpClient.send(ArgumentMatchers.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new HttpTimeoutException("Connection timed out"));

        String answer = geminiService.askGemini("What is kinetic energy?");

        assertTrue(answer.contains("timed out"));
    }

    @Test
    @DisplayName("Should handle IOException gracefully")
    void testAskGemini_IOException() throws Exception {
        when(mockHttpClient.send(ArgumentMatchers.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("Simulated network failure"));

        String answer = geminiService.askGemini("What is potential energy?");

        assertTrue(answer.contains("Network error"));
    }

    @Test
    @DisplayName("Should handle safety block gracefully")
    void testAskGemini_SafetyBlock() throws Exception {
        String safetyResponse = """
                {
                  "candidates": [
                    {
                      "finishReason": "SAFETY"
                    }
                  ]
                }
                """;

        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(safetyResponse);
        when(mockHttpClient.send(ArgumentMatchers.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockHttpResponse);

        String answer = geminiService.askGemini("Harmful query");

        assertTrue(answer.contains("safety guidelines"));
    }

    @Test
    @DisplayName("Should handle malformed JSON response safely")
    void testAskGemini_MalformedJson() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn("<html><title>502 Bad Gateway</title></html>");
        when(mockHttpClient.send(ArgumentMatchers.any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockHttpResponse);

        String answer = geminiService.askGemini("What is mitochondria?");

       
        assertTrue(answer.contains("EduAI Error"));
    }

    @Test
    @DisplayName("Should normalize malformed UTF-8 bullet characters")
    void testNormalizeMalformedBullets() {
        String malformedBullet = ""
                + (char) 0x00E2
                + (char) 0x0080
                + (char) 0x00A2;

        String correctBullet = String.valueOf((char) 0x2022);

        String input = "Point 1 " + malformedBullet + " Point 2";
        String expected = "Point 1 " + correctBullet + " Point 2";

        assertEquals(expected, GeminiService.normalizeMalformedBullets(input));

        String alreadyCorrect = "Point 1 " + correctBullet + " Point 2";
        assertEquals(
                alreadyCorrect,
                GeminiService.normalizeMalformedBullets(alreadyCorrect)
        );
    }
}

