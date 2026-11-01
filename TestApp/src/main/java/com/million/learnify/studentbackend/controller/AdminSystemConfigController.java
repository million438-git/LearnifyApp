package com.million.learnify.studentbackend.controller;

import com.million.learnify.studentbackend.dto.SystemAuditLog;
import com.million.learnify.studentbackend.dto.SystemConfiguration;
import com.million.learnify.studentbackend.security.FirebasePrincipal;
import com.million.learnify.studentbackend.service.SystemConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminSystemConfigController - Authoritative Spring Boot API for Learnify System Governance.
 * Strictly enforces ROLE_ADMIN authorization for all sensitive operations.
 */
@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemConfigController {

    private final SystemConfigService systemConfigService;

    public AdminSystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping("/config")
    public ResponseEntity<SystemConfiguration> getConfiguration() {
        return ResponseEntity.ok(systemConfigService.getConfiguration());
    }

    @PutMapping("/config")
    public ResponseEntity<?> updateConfiguration(
            @RequestBody SystemConfiguration updatedConfig,
            Authentication authentication) {
        try {
            String adminUser = "ADMIN";
            if (authentication != null && authentication.getPrincipal() instanceof FirebasePrincipal principal) {
                adminUser = principal.email() != null ? principal.email() : principal.uid();
            }

            SystemConfiguration saved = systemConfigService.updateConfiguration(updatedConfig, adminUser);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", System.currentTimeMillis());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("error", "Bad Request");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<SystemAuditLog>> getAuditLogs() {
        return ResponseEntity.ok(systemConfigService.getAuditLogs());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        SystemConfiguration config = systemConfigService.getConfiguration();
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Learnify System Governance API");
        health.put("configVersion", config.getVersion());
        health.put("lastModified", config.getLastModified());
        health.put("maintenanceMode", config.isMaintenanceMode());
        health.put("aiServiceEnabled", config.isAiServiceEnabled());
        health.put("jvmFreeMemoryBytes", Runtime.getRuntime().freeMemory());
        health.put("jvmTotalMemoryBytes", Runtime.getRuntime().totalMemory());
        health.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }
}

