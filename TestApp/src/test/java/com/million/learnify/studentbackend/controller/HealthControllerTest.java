package com.million.learnify.studentbackend.controller;

import com.million.learnify.studentbackend.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private GeminiService mockGeminiService;

    private HealthController healthController;

    @BeforeEach
    void setUp() {
        healthController = new HealthController(mockGeminiService);
    }

    @Test
    @DisplayName("Should return 200 UP for health check")
    void testHealth_Success() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("Learnify Student Backend", response.getBody().get("service"));
    }

    @Test
    @DisplayName("Should return 200 with AI configuration status without leaking keys")
    void testStatus_Success() {
        when(mockGeminiService.isConfigured()).thenReturn(true);
        when(mockGeminiService.getModelName()).thenReturn("gemini-1.5-flash");

        ResponseEntity<Map<String, Object>> response = healthController.status();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals(true, response.getBody().get("aiConfigured"));
        assertEquals("gemini-1.5-flash", response.getBody().get("activeModel"));
        assertNull(response.getBody().get("apiKey"), "API keys must never be exposed");
    }
}

