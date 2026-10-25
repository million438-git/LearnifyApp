package com.million.learnify.studentbackend.controller;

import com.million.learnify.studentbackend.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * HealthController - Liveness, Readiness, and Diagnostic Endpoints.
 * Safely reports backend operational status without exposing secrets or credentials.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final GeminiService geminiService;

    public HealthController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Learnify Student Backend");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Learnify Student Backend");
        response.put("aiConfigured", geminiService.isConfigured());
        response.put("activeModel", geminiService.getModelName());
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}

