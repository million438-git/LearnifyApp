package com.example.testapp;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * StudyContentProvider - Pure HTML-Based Academic Engine.
 * Scans assets/subjects recursively to find all subjects, units, and HTML topics.
 * Zero-Complexity: Just add folders and .html files.
 */
public class StudyContentProvider {

    private static final String TAG = "StudyContentProvider";
    private static final String ASSETS_BASE = "subjects";

    /**
     * Lists all unit folders for a subject by scanning assets/subjects/<subjectName>.
     */
    public static List<String> getChapters(Context context, String subjectName) {
        List<String> chapters = new ArrayList<>();
        try {
            String subjectPath = findActualPath(context, ASSETS_BASE, subjectName);
            if (subjectPath == null) {
                Log.w(TAG, "Subject path not found: " + subjectName);
                return chapters;
            }

            String[] list = context.getAssets().list(subjectPath);
            if (list != null) {
                for (String name : list) {
                    // Filter for directories (units usually don't have dots in names)
                    if (!name.contains(".")) {
                        chapters.add(name);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error listing chapters", e);
        }
        // Natural sort: CHAPTER 1, CHAPTER 2...
        Collections.sort(chapters);
        return chapters;
    }

    /**
     * Finds all HTML files recursively inside a unit folder and returns them as topics.
     */
    public static List<String> getTopics(Context context, String subjectName, String chapterName) {
        List<String> topics = new ArrayList<>();
        try {
            String subjectPath = findActualPath(context, ASSETS_BASE, subjectName);
            if (subjectPath == null) return topics;

            String unitPath = findActualPath(context, subjectPath, chapterName);
            if (unitPath == null) return topics;

            addTopicsRecursively(context, unitPath, "", topics);
        } catch (IOException e) {
            Log.e(TAG, "Error listing topics", e);
        }
        Collections.sort(topics);
        return topics;
    }

    private static void addTopicsRecursively(Context context, String fullPath, String relativePath, List<String> topics) throws IOException {
        String[] list = context.getAssets().list(fullPath);
        if (list == null) return;

        for (String name : list) {
            String currentFullPath = fullPath + "/" + name;
            String currentRelativePath = relativePath.isEmpty() ? name : relativePath + "/" + name;

            if (name.toLowerCase().endsWith(".html")) {
                // Add the relative path without extension as the topic name
                topics.add(currentRelativePath.substring(0, currentRelativePath.lastIndexOf(".")));
            } else if (!name.contains(".")) {
                // It's a sub-directory, recurse into it
                addTopicsRecursively(context, currentFullPath, currentRelativePath, topics);
            }
        }
    }

    /**
     * Resolves the full asset URL for a given topic path.
     */
    public static String getHtmlUrl(Context context, String subject, String chapter, String topic) {
        try {
            String subjectPath = findActualPath(context, ASSETS_BASE, subject);
            if (subjectPath == null) return null;

            String unitPath = findActualPath(context, subjectPath, chapter);
            if (unitPath == null) return null;

            // The 'topic' string is the relative path found in getTopics (e.g., "GRAMMAR/tenses")
            // We need to resolve each part of this path case-insensitively
            String[] parts = topic.split("/");
            StringBuilder resolvedPath = new StringBuilder(unitPath);
            
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (i == parts.length - 1) {
                    // Try with .html extension
                    String actualName = findActualNameInFolder(context, resolvedPath.toString(), part + ".html");
                    if (actualName != null) {
                        resolvedPath.append("/").append(actualName);
                        continue;
                    }
                    // Fallback to exact part if it already has .html or is just a file without extension
                    actualName = findActualNameInFolder(context, resolvedPath.toString(), part);
                    if (actualName == null) return null;
                    resolvedPath.append("/").append(actualName);
                } else {
                    String actualName = findActualNameInFolder(context, resolvedPath.toString(), part);
                    if (actualName == null) return null;
                    resolvedPath.append("/").append(actualName);
                }
            }

            return "file:///android_asset/" + resolvedPath.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error resolving HTML URL", e);
            return null;
        }
    }

    private static String findActualPath(Context context, String parent, String target) throws IOException {
        String actualName = findActualNameInFolder(context, parent, target);
        return (actualName != null) ? parent + "/" + actualName : null;
    }

    private static String findActualNameInFolder(Context context, String folder, String target) throws IOException {
        String[] list = context.getAssets().list(folder);
        if (list != null) {
            for (String name : list) {
                if (name.equalsIgnoreCase(target)) return name;
            }
        }
        return null;
    }

    // --- Helper for Activities ---
    public static String getNoteForTopic(Context context, String subject, String chapter, String topic) {
        return getHtmlUrl(context, subject, chapter, topic);
    }
}
