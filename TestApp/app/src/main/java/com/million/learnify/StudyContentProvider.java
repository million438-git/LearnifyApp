package com.million.learnify;

import android.content.Context;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * StudyContentProvider - Perfected Academic Path Resolver.
 * Features: High-performance recursive search and ultra-robust fuzzy matching.
 * Guaranteed to find files like "Biology Technology.html" even with naming variations.
 */
public class StudyContentProvider {

    private static final String TAG = "StudyContentProvider";
    private static final String ASSETS_BASE = "subjects";

    public static List<String> getChapters(Context context, String subjectName) {
        return getChapters(context, subjectName, null);
    }

    public static List<String> getChapters(Context context, String subjectName, String grade) {
        List<String> chapters = new ArrayList<>();
        try {
            String subjectPath = findActualPath(context, ASSETS_BASE, subjectName);
            if (subjectPath == null) return chapters;

            String gradePath = findGradePath(context, subjectPath, grade);
            String contentPath = gradePath != null ? gradePath : subjectPath;
            String[] list = context.getAssets().list(contentPath);
            if (list != null) {
                for (String name : list) {
                    if (!name.contains(".")
                            && (gradePath != null || grade == null || !isGradeFolder(name))) {
                        chapters.add(name);
                    }
                }
            }
            if (gradePath != null) {
                String[] legacyList = context.getAssets().list(subjectPath);
                List<String> legacyChapters = new ArrayList<>();
                boolean hasLegacyLessons = false;
                if (legacyList != null) {
                    for (String name : legacyList) {
                        if (!name.contains(".") && !isGradeFolder(name)) {
                            legacyChapters.add(name);
                            hasLegacyLessons |= hasHtmlLessons(context, subjectPath + "/" + name);
                        }
                    }
                }
                if (chapters.isEmpty() || (!hasHtmlLessons(context, gradePath) && hasLegacyLessons)) {
                    chapters.clear();
                    chapters.addAll(legacyChapters);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error listing chapters", e);
        }
        sortCurriculumNames(chapters);
        return chapters;
    }

    public static List<String> getTopics(Context context, String subjectName, String chapterName) {
        return getTopics(context, subjectName, chapterName, null);
    }

    public static List<String> getTopics(Context context, String subjectName, String chapterName, String grade) {
        List<String> topics = new ArrayList<>();
        try {
            String subjectPath = findActualPath(context, ASSETS_BASE, subjectName);
            if (subjectPath == null) return topics;
            String gradePath = findGradePath(context, subjectPath, grade);
            String unitPath = gradePath == null ? null : findActualPath(context, gradePath, chapterName);
            if (unitPath == null && (grade == null || !isGradeFolder(chapterName))) {
                unitPath = findActualPath(context, subjectPath, chapterName);
            }
            if (unitPath == null) return topics;
            addTopicsRecursively(context, unitPath, "", topics);
        } catch (IOException e) {
            Log.e(TAG, "Error listing topics", e);
        }
        sortCurriculumNames(topics);
        return topics;
    }

    public static List<String> getGradeRootTopics(Context context, String subjectName, String grade) {
        List<String> topics = new ArrayList<>();
        try {
            String subjectPath = findActualPath(context, ASSETS_BASE, subjectName);
            if (subjectPath == null) return topics;
            String gradePath = findGradePath(context, subjectPath, grade);
            if (gradePath == null) return topics;

            String[] list = context.getAssets().list(gradePath);
            if (list != null) {
                for (String name : list) {
                    if (name.toLowerCase().endsWith(".html")) {
                        topics.add(name.substring(0, name.lastIndexOf(".")));
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error listing grade resources", e);
        }
        sortCurriculumNames(topics);
        return topics;
    }

    private static void addTopicsRecursively(Context context, String fullPath, String relativePath, List<String> topics) throws IOException {
        String[] list = context.getAssets().list(fullPath);
        if (list == null) return;
        for (String name : list) {
            String currentFullPath = fullPath + "/" + name;
            String currentRelativePath = relativePath.isEmpty() ? name : relativePath + "/" + name;
            if (name.toLowerCase().endsWith(".html")) {
                topics.add(currentRelativePath.substring(0, currentRelativePath.lastIndexOf(".")));
            } else if (!name.contains(".")) {
                addTopicsRecursively(context, currentFullPath, currentRelativePath, topics);
            }
        }
    }

    private static boolean hasHtmlLessons(Context context, String path) throws IOException {
        String[] list = context.getAssets().list(path);
        if (list == null) return false;
        for (String name : list) {
            if (name.toLowerCase().endsWith(".html")) return true;
            if (!name.contains(".") && hasHtmlLessons(context, path + "/" + name)) return true;
        }
        return false;
    }

    /**
     * Resolves the full asset URL for a given topic path.
     * Guaranteed robustness for naming mismatches like 'Biology Technology' vs 'Biology and Technology'.
     */
    public static String getHtmlUrl(Context context, String subject, String chapter, String topic) {
        return getHtmlUrl(context, subject, chapter, topic, null);
    }

    public static String getHtmlUrl(Context context, String subject, String chapter, String topic, String grade) {
    try {
        String subjectPath = findActualPath(context, ASSETS_BASE, subject);
        if (subjectPath == null || topic == null || topic.trim().isEmpty()) return null;

        String normalizedTopic = topic.trim().replace('\\', '/');
        if (normalizedTopic.toLowerCase().endsWith(".html")) {
            normalizedTopic = normalizedTopic.substring(0, normalizedTopic.length() - 5);
        }

        String gradePath = findGradePath(context, subjectPath, grade);
        String chapterPath = chapter == null
                ? gradePath
                : gradePath == null ? null : findActualPath(context, gradePath, chapter);
        if (chapterPath == null && chapter != null && (grade == null || !isGradeFolder(chapter))) {
            chapterPath = findActualPath(context, subjectPath, chapter);
        }

        if (chapterPath != null) {
            String exactPath = chapterPath + "/" + normalizedTopic + ".html";
            try (InputStream ignored = context.getAssets().open(exactPath)) {
                return "file:///android_asset/" + exactPath;
            } catch (IOException ignored) {
                // Continue with compatible fuzzy matching.
            }

            String searchPath = chapterPath;
            String target = normalizedTopic;

            if (normalizedTopic.contains("/")) {
                int slash = normalizedTopic.lastIndexOf('/');
                String parent = normalizedTopic.substring(0, slash);
                target = normalizedTopic.substring(slash + 1);
                String resolvedParent = findActualPath(context, chapterPath, parent);
                if (resolvedParent != null) searchPath = resolvedParent;
            }

            String found = searchFileWithFuzzyLogic(context, searchPath, target);
            if (found != null) return "file:///android_asset/" + found;
        }

        // Legacy fallback for existing course layouts.
        String targetFile = normalizedTopic.contains("/")
                ? normalizedTopic.substring(normalizedTopic.lastIndexOf('/') + 1)
                : normalizedTopic;
        String found = searchFileWithFuzzyLogic(context, subjectPath, targetFile, grade != null);
        return found == null ? null : "file:///android_asset/" + found;
    } catch (Exception e) {
        Log.e(TAG, "URL resolution failed", e);
        return null;
    }
}

    /**
     * Reads raw HTML content from the resolved asset file for Text-to-Speech and Inclusive Reading.
     */
    public static String getHtmlContent(Context context, String subject, String chapter, String topic) {
        return getHtmlContent(context, subject, chapter, topic, null);
    }

    public static String getHtmlContent(Context context, String subject, String chapter, String topic, String grade) {
        String url = getHtmlUrl(context, subject, chapter, topic, grade);
        if (url == null) return null;
        try {
            String assetPath = url.replace("file:///android_asset/", "");
            InputStream is = context.getAssets().open(assetPath);
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            String content = scanner.hasNext() ? scanner.next() : "";
            is.close();
            return content;
        } catch (Exception e) {
            Log.e(TAG, "Failed reading HTML asset content for " + topic, e);
            return null;
        }
    }

    private static String searchFileWithFuzzyLogic(Context context, String path, String target) throws IOException {
        return searchFileWithFuzzyLogic(context, path, target, false);
    }

    private static String searchFileWithFuzzyLogic(Context context, String path, String target,
                                                    boolean excludeGradeFolders) throws IOException {
        String[] list = context.getAssets().list(path);
        if (list == null) return null;

        String targetClean = cleanName(target);
        for (String name : list) {
            String fullPath = path + "/" + name;
            if (name.toLowerCase().endsWith(".html")) {
                String nameClean = cleanName(name.substring(0, name.lastIndexOf(".")));
                // Absolute Match OR Deep Substring Match
                if (nameClean.equals(targetClean) || nameClean.contains(targetClean) || targetClean.contains(nameClean)) {
                    return fullPath;
                }
            } else if (!name.contains(".")) {
                if (excludeGradeFolders && isGradeFolder(name)) continue;
                String result = searchFileWithFuzzyLogic(context, fullPath, target, excludeGradeFolders);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static String findActualPath(Context context, String parent, String target) throws IOException {
        if (parent == null || target == null || target.trim().isEmpty()) return null;
        String[] list = context.getAssets().list(parent);
        if (list == null) return null;
        String targetClean = cleanName(target);
        for (String name : list) {
            if (cleanName(name).equals(targetClean)) return parent + "/" + name;
        }
        for (String name : list) {
            if (cleanName(name).contains(targetClean)) return parent + "/" + name;
        }
        return null;
    }

    private static String cleanName(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replace("and", "")
                .replace("&", "")
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "")
                .trim();
    }

    private static String getGradeFolderName(String grade) {
        if (grade == null || grade.trim().isEmpty()) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(grade);
        return matcher.find() ? "Grade-" + matcher.group() : grade.trim();
    }

    private static String findGradePath(Context context, String subjectPath, String grade) throws IOException {
        String gradeFolderName = getGradeFolderName(grade);
        return gradeFolderName == null ? null : findActualPath(context, subjectPath, gradeFolderName);
    }

    private static boolean isGradeFolder(String name) {
        return name != null && name.trim().toLowerCase(java.util.Locale.US).matches("grade[- _]?\\d+");
    }

    private static void sortCurriculumNames(List<String> names) {
        Collections.sort(names, new Comparator<String>() {
            @Override
            public int compare(String first, String second) {
                java.util.regex.Matcher firstNumber = java.util.regex.Pattern.compile("\\d+").matcher(first);
                java.util.regex.Matcher secondNumber = java.util.regex.Pattern.compile("\\d+").matcher(second);
                boolean hasFirstNumber = firstNumber.find();
                boolean hasSecondNumber = secondNumber.find();
                if (hasFirstNumber && hasSecondNumber) {
                    try {
                        int numberComparison = Long.compare(
                                Long.parseLong(firstNumber.group()),
                                Long.parseLong(secondNumber.group()));
                        if (numberComparison != 0) return numberComparison;
                    } catch (NumberFormatException ignored) {
                        // Fall back to the full name when an asset contains an unusually large number.
                    }
                } else if (hasFirstNumber != hasSecondNumber) {
                    return hasFirstNumber ? -1 : 1;
                }
                return first.compareToIgnoreCase(second);
            }
        });
    }
}
