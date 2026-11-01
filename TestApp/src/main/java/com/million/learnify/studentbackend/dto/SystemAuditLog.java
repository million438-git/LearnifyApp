package com.million.learnify.studentbackend.dto;

import java.io.Serializable;

/**
 * SystemAuditLog - Administrative Configuration & Security Audit Record.
 */
public class SystemAuditLog implements Serializable {

    private long id;
    private long timestamp;
    private String adminUser;
    private String action;
    private String details;
    private int configVersion;

    public SystemAuditLog() {
    }

    public SystemAuditLog(long id, long timestamp, String adminUser, String action, String details, int configVersion) {
        this.id = id;
        this.timestamp = timestamp;
        this.adminUser = adminUser;
        this.action = action;
        this.details = details;
        this.configVersion = configVersion;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(int configVersion) {
        this.configVersion = configVersion;
    }
}

