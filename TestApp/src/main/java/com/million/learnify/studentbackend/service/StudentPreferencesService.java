package com.million.learnify.studentbackend.service;

import com.million.learnify.studentbackend.dto.StudentPreferencesDto;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StudentPreferencesService - Authoritative Cloud Persistence for Student Account Preferences.
 */
@Service
public class StudentPreferencesService {

    private final Map<String, StudentPreferencesDto> userPreferencesMap = new ConcurrentHashMap<>();

    public StudentPreferencesDto getPreferences(String username) {
        if (username == null || username.trim().isEmpty()) {
            username = "default_student";
        }
        return userPreferencesMap.computeIfAbsent(username, this::createDefaultPreferences);
    }

    public synchronized PreferenceUpdateResult updatePreferences(String username, StudentPreferencesDto incoming) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (incoming == null) {
            throw new IllegalArgumentException("Preferences payload cannot be null.");
        }

        // Validate bounds
        if (incoming.getDailyStudyMinutes() < 5 || incoming.getDailyStudyMinutes() > 480) {
            incoming.setDailyStudyMinutes(Math.max(5, Math.min(480, incoming.getDailyStudyMinutes())));
        }
        if (incoming.getWeeklyLessonGoal() < 1 || incoming.getWeeklyLessonGoal() > 100) {
            incoming.setWeeklyLessonGoal(Math.max(1, Math.min(100, incoming.getWeeklyLessonGoal())));
        }

        StudentPreferencesDto existing = userPreferencesMap.get(username);
        if (existing != null) {
            // Conflict Detection: If incoming version is behind existing version and incoming update is older
            if (incoming.getVersion() < existing.getVersion() && incoming.getUpdatedAt() < existing.getUpdatedAt()) {
                return new PreferenceUpdateResult(false, existing, "Conflict detected: Server has a newer preference version (v" + existing.getVersion() + ").");
            }
            incoming.setVersion(existing.getVersion() + 1);
        } else {
            incoming.setVersion(1);
        }

        incoming.setUsername(username);
        incoming.setUpdatedAt(System.currentTimeMillis());
        userPreferencesMap.put(username, incoming);

        return new PreferenceUpdateResult(true, incoming, null);
    }

    private StudentPreferencesDto createDefaultPreferences(String username) {
        StudentPreferencesDto dto = new StudentPreferencesDto();
        dto.setUsername(username);
        dto.setVersion(1);
        dto.setUpdatedAt(System.currentTimeMillis());
        return dto;
    }

    public record PreferenceUpdateResult(boolean success, StudentPreferencesDto preferences, String errorMessage) {
    }
}

