package com.example.testapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for Dashboard Data.
 * Stores a snapshot of the student's current status and progress.
 */
public class DashboardData {
    private String studentId;
    private int todayClasses;
    private int pendingHomework;
    private String progressSummary;
    private List<SubjectProgress> subjectProgressList;
    private List<String> notifications;
    private List<String> aiSuggestions;
    private long lastSync;

    public DashboardData() {
        this.subjectProgressList = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.aiSuggestions = new ArrayList<>();
        this.lastSync = System.currentTimeMillis();
    }

    // Inner class for Subject Progress
    public static class SubjectProgress {
        private String subjectName;
        private int progressPercentage;

        public SubjectProgress(String subjectName, int progressPercentage) {
            this.subjectName = subjectName;
            this.progressPercentage = progressPercentage;
        }

        public String getSubjectName() { return subjectName; }
        public int getProgressPercentage() { return progressPercentage; }
    }

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public int getTodayClasses() { return todayClasses; }
    public void setTodayClasses(int todayClasses) { this.todayClasses = todayClasses; }

    public int getPendingHomework() { return pendingHomework; }
    public void setPendingHomework(int pendingHomework) { this.pendingHomework = pendingHomework; }

    public String getProgressSummary() { return progressSummary; }
    public void setProgressSummary(String progressSummary) { this.progressSummary = progressSummary; }

    public List<SubjectProgress> getSubjectProgressList() { return subjectProgressList; }
    public void setSubjectProgressList(List<SubjectProgress> subjectProgressList) { this.subjectProgressList = subjectProgressList; }

    public List<String> getNotifications() { return notifications; }
    public void setNotifications(List<String> notifications) { this.notifications = notifications; }

    public List<String> getAiSuggestions() { return aiSuggestions; }
    public void setAiSuggestions(List<String> aiSuggestions) { this.aiSuggestions = aiSuggestions; }

    public long getLastSync() { return lastSync; }
    public void setLastSync(long lastSync) { this.lastSync = lastSync; }
}
