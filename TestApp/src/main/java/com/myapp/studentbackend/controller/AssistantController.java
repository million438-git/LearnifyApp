package com.myapp.studentbackend.controller;

import com.myapp.studentbackend.dto.AskRequest;
import com.myapp.studentbackend.dto.AskResponse;
import com.myapp.studentbackend.service.OllamaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
@CrossOrigin(origins = "*") // Critical for Android connectivity
public class AssistantController {

    private final OllamaService ollamaService;

    public AssistantController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping("/ask")
    public AskResponse ask(@RequestBody AskRequest request) {
        String answer = ollamaService.askOllama(request.getQuestion());
        return new AskResponse(answer);
    }
}
