package com.million.learnify.studentbackend.controller;

import com.million.learnify.studentbackend.dto.StudentPreferencesDto;
import com.million.learnify.studentbackend.security.FirebasePrincipal;
import com.million.learnify.studentbackend.service.StudentPreferencesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * StudentPreferencesController - Cloud Synchronization for Student Account Preferences.
 */
@RestController
@RequestMapping("/api/student")
public class StudentPreferencesController {

    private final StudentPreferencesService preferencesService;

    public StudentPreferencesController(StudentPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping("/preferences")
    public ResponseEntity<StudentPreferencesDto> getPreferences(Authentication authentication) {
        String username = resolveUsername(authentication);
        return ResponseEntity.ok(preferencesService.getPreferences(username));
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> updatePreferences(
            @RequestBody StudentPreferencesDto incoming,
            Authentication authentication) {
        String username = resolveUsername(authentication);
        StudentPreferencesService.PreferenceUpdateResult result = preferencesService.updatePreferences(username, incoming);

        if (!result.success()) {
            Map<String, Object> conflictResponse = new HashMap<>();
            conflictResponse.put("status", HttpStatus.CONFLICT.value());
            conflictResponse.put("error", "Conflict");
            conflictResponse.put("message", result.errorMessage());
            conflictResponse.put("serverPreferences", result.preferences());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(conflictResponse);
        }

        return ResponseEntity.ok(result.preferences());
    }

    private String resolveUsername(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof FirebasePrincipal principal) {
            if (principal.email() != null && !principal.email().isBlank()) {
                return principal.email();
            }
            if (principal.uid() != null && !principal.uid().isBlank()) {
                return principal.uid();
            }
        }
        return "student";
    }
}

