package com.million.learnify.studentbackend.controller;

import com.million.learnify.studentbackend.dto.AskRequest;
import com.million.learnify.studentbackend.dto.AskResponse;
import com.million.learnify.studentbackend.service.OllamaService;

import org.springframework.web.bind.annotation.*;

/**
 * AssistantController - The 100% Real AI Assistant Bridge.
 * This file is now perfectly synchronized with your Android App package.
 */
@RestController
@RequestMapping("/api/assistant")
@CrossOrigin(origins = "*")
public class AssistantController {

    private final OllamaService ollamaService;

    public AssistantController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping("/ask")
    public AskResponse ask(@RequestBody AskRequest request) {
        try {
            // Send the student's question to the Phi-3 engine
            String answer = ollamaService.askOllama(request.getQuestion());
            return new AskResponse(answer);
        } catch (Exception e) {
            return new AskResponse("🤖 AI Engine Error: " + e.getMessage());
        }
    }
}
