package com.million.learnify.studentbackend.service;

import com.million.learnify.studentbackend.dto.SystemAuditLog;
import com.million.learnify.studentbackend.dto.SystemConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SystemConfigService - Authoritative Thread-Safe Backend Service for Global Learnify Configuration.
 */
@Service
public class SystemConfigService {

    private final AtomicReference<SystemConfiguration> currentConfig = new AtomicReference<>(new SystemConfiguration());
    private final List<SystemAuditLog> auditLogs = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong auditIdGenerator = new AtomicLong(1);

    public SystemConfigService() {
        // Initial audit entry
        auditLogs.add(new SystemAuditLog(
                auditIdGenerator.getAndIncrement(),
                System.currentTimeMillis(),
                "SYSTEM",
                "INIT",
                "System configuration initialized with default policies",
                1
        ));
    }

    public SystemConfiguration getConfiguration() {
        return currentConfig.get();
    }

    public synchronized SystemConfiguration updateConfiguration(SystemConfiguration updated, String adminUser) {
        if (updated == null) {
            throw new IllegalArgumentException("Configuration payload cannot be null.");
        }

        // Server-Side Strict Validations
        if (updated.getPassingPercentage() < 0 || updated.getPassingPercentage() > 100) {
            throw new IllegalArgumentException("Passing percentage must be between 0 and 100.");
        }
        if (updated.getExamDurationMinutes() < 5 || updated.getExamDurationMinutes() > 360) {
            throw new IllegalArgumentException("Exam duration must be between 5 and 360 minutes.");
        }
        if (updated.getMaxExamAttempts() < 1 || updated.getMaxExamAttempts() > 10) {
            throw new IllegalArgumentException("Maximum exam attempts must be between 1 and 10.");
        }
        if (updated.getSessionTimeoutMinutes() < 5 || updated.getSessionTimeoutMinutes() > 1440) {
            throw new IllegalArgumentException("Session timeout must be between 5 and 1440 minutes.");
        }
        if (updated.getInstitutionName() == null || updated.getInstitutionName().trim().isEmpty()) {
            throw new IllegalArgumentException("Institution name cannot be blank.");
        }
        if (updated.getContactEmail() == null || !updated.getContactEmail().contains("@")) {
            throw new IllegalArgumentException("A valid contact email address is required.");
        }

        SystemConfiguration existing = currentConfig.get();
        int newVersion = existing.getVersion() + 1;
        updated.setVersion(newVersion);
        updated.setLastModified(System.currentTimeMillis());
        updated.setModifiedBy(adminUser != null ? adminUser : "ADMIN");

        currentConfig.set(updated);

        // Record Audit Trail
        auditLogs.add(0, new SystemAuditLog(
                auditIdGenerator.getAndIncrement(),
                updated.getLastModified(),
                updated.getModifiedBy(),
                "UPDATE_SYSTEM_CONFIG",
                "Updated institution: '" + updated.getInstitutionName() + "', passing percentage: " + updated.getPassingPercentage() + "%",
                newVersion
        ));

        // Trim audit logs to 200 max entries
        while (auditLogs.size() > 200) {
            auditLogs.remove(auditLogs.size() - 1);
        }

        return updated;
    }

    public List<SystemAuditLog> getAuditLogs() {
        return new ArrayList<>(auditLogs);
    }
}

