package com.million.learnify;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;

/**
 * AdminSystemConfig - Category C Configuration.
 * Authoritative on Spring Boot backend, cached read-only in SQLite on Android client.
 * Governs registration, examinations, institutional branding, AI services, and maintenance mode.
 */
public class AdminSystemConfig implements Serializable {

    private static final Gson GSON = new GsonBuilder().create();
    public static final String DEFAULT_CONFIG_ID = "GLOBAL_SYSTEM_CONFIG";

    private String configId = DEFAULT_CONFIG_ID;
    private int version = 1;
    private long fetchedAt = System.currentTimeMillis();
    private String etag = "";
    private boolean isCached = false;

    // Institution & Branding
    private String institutionName = "Learnify National Academy";
    private String portalTagline = "Empowering Students Through Adaptive Intelligence";
    private String contactEmail = "support@learnify.org";
    private String supportPhone = "+1 (800) 555-0199";
    private String defaultLanguage = "en";

    // Registration & User Policies
    private boolean registrationAllowed = true;
    private boolean requireEmailVerification = true;
    private boolean studentSelfEnrollment = true;
    private int maxAccountsPerIp = 5;

    // Academic & Examination Policies
    private int examDurationMinutes = 60;
    private int passingPercentage = 75;
    private int maxExamAttempts = 3;
    private boolean practiceModeEnabled = true;

    // AI & Science Studio Governance
    private boolean aiServiceEnabled = true;
    private boolean labSimulationsEnabled = true;
    private boolean cameraScannerEnabled = true;

    // Security Policies
    private boolean enforceAdmin2fa = true;
    private int sessionTimeoutMinutes = 120;
    private int maxFailedLogins = 5;

    // Feature Flags
    private boolean chatEnabled = true;
    private boolean leaderboardEnabled = true;
    private boolean materialsDownloadEnabled = true;

    // Maintenance & Lifecycle
    private boolean maintenanceMode = false;
    private String maintenanceMessage = "Learnify is currently undergoing scheduled maintenance. Please check back shortly.";
    private String minClientVersion = "3.0.0";

    public AdminSystemConfig() {
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static AdminSystemConfig fromJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new AdminSystemConfig();
        }
        try {
            AdminSystemConfig c = GSON.fromJson(jsonString, AdminSystemConfig.class);
            return c != null ? c : new AdminSystemConfig();
        } catch (Exception e) {
            return new AdminSystemConfig();
        }
    }

    // Getters & Setters
    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public long getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(long fetchedAt) { this.fetchedAt = fetchedAt; }

    public String getEtag() { return etag; }
    public void setEtag(String etag) { this.etag = etag; }

    public boolean isCached() { return isCached; }
    public void setCached(boolean cached) { isCached = cached; }

    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }

    public String getPortalTagline() { return portalTagline; }
    public void setPortalTagline(String portalTagline) { this.portalTagline = portalTagline; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getSupportPhone() { return supportPhone; }
    public void setSupportPhone(String supportPhone) { this.supportPhone = supportPhone; }

    public String getDefaultLanguage() { return defaultLanguage; }
    public void setDefaultLanguage(String defaultLanguage) { this.defaultLanguage = defaultLanguage; }

    public boolean isRegistrationAllowed() { return registrationAllowed; }
    public void setRegistrationAllowed(boolean registrationAllowed) { this.registrationAllowed = registrationAllowed; }

    public boolean isRequireEmailVerification() { return requireEmailVerification; }
    public void setRequireEmailVerification(boolean requireEmailVerification) { this.requireEmailVerification = requireEmailVerification; }

    public boolean isStudentSelfEnrollment() { return studentSelfEnrollment; }
    public void setStudentSelfEnrollment(boolean studentSelfEnrollment) { this.studentSelfEnrollment = studentSelfEnrollment; }

    public int getMaxAccountsPerIp() { return maxAccountsPerIp; }
    public void setMaxAccountsPerIp(int maxAccountsPerIp) { this.maxAccountsPerIp = maxAccountsPerIp; }

    public int getExamDurationMinutes() { return examDurationMinutes; }
    public void setExamDurationMinutes(int examDurationMinutes) { this.examDurationMinutes = examDurationMinutes; }

    public int getPassingPercentage() { return passingPercentage; }
    public void setPassingPercentage(int passingPercentage) { this.passingPercentage = passingPercentage; }

    public int getMaxExamAttempts() { return maxExamAttempts; }
    public void setMaxExamAttempts(int maxExamAttempts) { this.maxExamAttempts = maxExamAttempts; }

    public boolean isPracticeModeEnabled() { return practiceModeEnabled; }
    public void setPracticeModeEnabled(boolean practiceModeEnabled) { this.practiceModeEnabled = practiceModeEnabled; }

    public boolean isAiServiceEnabled() { return aiServiceEnabled; }
    public void setAiServiceEnabled(boolean aiServiceEnabled) { this.aiServiceEnabled = aiServiceEnabled; }

    public boolean isLabSimulationsEnabled() { return labSimulationsEnabled; }
    public void setLabSimulationsEnabled(boolean labSimulationsEnabled) { this.labSimulationsEnabled = labSimulationsEnabled; }

    public boolean isCameraScannerEnabled() { return cameraScannerEnabled; }
    public void setCameraScannerEnabled(boolean cameraScannerEnabled) { this.cameraScannerEnabled = cameraScannerEnabled; }

    public boolean isEnforceAdmin2fa() { return enforceAdmin2fa; }
    public void setEnforceAdmin2fa(boolean enforceAdmin2fa) { this.enforceAdmin2fa = enforceAdmin2fa; }

    public int getSessionTimeoutMinutes() { return sessionTimeoutMinutes; }
    public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) { this.sessionTimeoutMinutes = sessionTimeoutMinutes; }

    public int getMaxFailedLogins() { return maxFailedLogins; }
    public void setMaxFailedLogins(int maxFailedLogins) { this.maxFailedLogins = maxFailedLogins; }

    public boolean isChatEnabled() { return chatEnabled; }
    public void setChatEnabled(boolean chatEnabled) { this.chatEnabled = chatEnabled; }

    public boolean isLeaderboardEnabled() { return leaderboardEnabled; }
    public void setLeaderboardEnabled(boolean leaderboardEnabled) { this.leaderboardEnabled = leaderboardEnabled; }

    public boolean isMaterialsDownloadEnabled() { return materialsDownloadEnabled; }
    public void setMaterialsDownloadEnabled(boolean materialsDownloadEnabled) { this.materialsDownloadEnabled = materialsDownloadEnabled; }

    public boolean isMaintenanceMode() { return maintenanceMode; }
    public void setMaintenanceMode(boolean maintenanceMode) { this.maintenanceMode = maintenanceMode; }

    public String getMaintenanceMessage() { return maintenanceMessage; }
    public void setMaintenanceMessage(String maintenanceMessage) { this.maintenanceMessage = maintenanceMessage; }

    public String getMinClientVersion() { return minClientVersion; }
    public void setMinClientVersion(String minClientVersion) { this.minClientVersion = minClientVersion; }
}
