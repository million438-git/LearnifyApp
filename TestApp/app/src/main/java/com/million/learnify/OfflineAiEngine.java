package com.million.learnify;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * OfflineAiEngine - High-Performance On-Device Intelligence.
 * Perfected to scan deep academic structures and provide human-like responses.
 * Now includes Offline Quiz Generation.
 */
public class OfflineAiEngine {

    private static final String TAG = "OfflineAiEngine";
    private final Context context;
    private final Set<String> stopWords;
    private final Random random = new Random();

    public OfflineAiEngine(Context context) {
        this.context = context;
        this.stopWords = initStopWords();
    }

    private Set<String> initStopWords() {
        Set<String> set = new HashSet<>();
        String[] words = {"a", "an", "the", "and", "or", "but", "if", "is", "are", "was", "were", "be", "been", "being", "in", "on", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "can", "will", "just", "don", "should", "now", "means", "what", "define", "explain", "quiz", "test", "me"};
        Collections.addAll(set, words);
        return set;
    }

    public String summarize(String text) {
        if (text == null || text.trim().length() < 50) return text;
        try {
            String[] sentences = text.split("(?<=[.!?])\\s+");
            if (sentences.length <= 2) return text;
            
            Map<String, Integer> freqMap = new HashMap<>();
            String[] words = text.toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
            for (String word : words) {
                if (word.length() > 3 && !stopWords.contains(word)) {
                    freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
                }
            }
            
            List<SentenceScore> scores = new ArrayList<>();
            for (int i = 0; i < sentences.length; i++) {
                int score = 0;
                String[] sWords = sentences[i].toLowerCase().replaceAll("[^a-zA-Z ]", "").split("\\s+");
                for (String word : sWords) {
                    score += freqMap.getOrDefault(word, 0);
                }
                scores.add(new SentenceScore(sentences[i], score, i));
            }
            
            int count = Math.max(2, (int) (sentences.length * 0.4));
            Collections.sort(scores, (a, b) -> b.score - a.score);
            List<SentenceScore> top = new ArrayList<>(scores.subList(0, Math.min(count, scores.size())));
            Collections.sort(top, (a, b) -> a.index - b.index);
            
            StringBuilder sb = new StringBuilder("Summary (Local AI):\n\n");
            for (SentenceScore ss : top) sb.append("• ").append(ss.text.trim()).append("\n\n");
            return sb.toString().trim();
        } catch (Exception e) {
            return text;
        }
    }

    public String searchLocalKnowledge(String query) {
        if (query == null || query.trim().isEmpty()) return "How can I help you study?";
        
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("quiz") || lowerQuery.contains("test me") || lowerQuery.contains("ask me")) {
            return generateOfflineQuiz(query);
        }

        // 1. Check Python curriculum engine first
        try {
            if (Python.isStarted()) {
                Python py = Python.getInstance();
                PyObject engine = py.getModule("voice_assistant_engine");
                String pyAnswer = engine.callAttr("get_offline_answer", query).toString();
                if (pyAnswer != null && !pyAnswer.trim().isEmpty() && !pyAnswer.toLowerCase().contains("couldn't find")) {
                    return "Offline Academic Tutor:\n\n" + pyAnswer;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Offline Python study engine lookup failed: " + e.getMessage());
        }

        List<String> keywords = new ArrayList<>();
        for (String word : query.toLowerCase().replaceAll("[^a-z0-9 ]", "").split("\\s+")) {
            if (word.length() > 2 && !stopWords.contains(word)) {
                keywords.add(word);
            }
        }

        if (keywords.isEmpty()) {
            return "Offline Partner: Please ask a more specific academic question so I can search your lessons!";
        }

        String bestMatch = scanAllAssetsRecursively("subjects", keywords.toArray(new String[0]));
        
        if (bestMatch != null) {
            return "Offline Academic Tutor\n\n" + bestMatch;
        }

        try {
            if (Python.isStarted()) {
                Python py = Python.getInstance();
                PyObject engine = py.getModule("voice_assistant_engine");
                String pyAnswer = engine.callAttr("get_offline_answer", query).toString();
                if (pyAnswer != null && !pyAnswer.trim().isEmpty()) {
                    return "Offline Academic Tutor:\n\n" + pyAnswer;
                }
            }
        } catch (Exception ignored) {}

        return "Offline Study Partner:\nI couldn't find a direct explanation for '" + query + "' in your current offline packs. " +
               "Try downloading the relevant Subject Pack from the Library to unlock full offline intelligence.";
    }

    private String generateOfflineQuiz(String query) {
        // Try to find a topic mentioned in the quiz request (e.g., "quiz me on Dynamics")
        List<String> keywords = new ArrayList<>();
        for (String word : query.toLowerCase().replaceAll("[^a-z0-9 ]", "").split("\\s+")) {
            if (word.length() > 3 && !stopWords.contains(word)) {
                keywords.add(word);
            }
        }

        String content = null;
        if (keywords.isEmpty()) {
            // Pick a random topic if no keyword found
            content = getRandomLessonContent();
        } else {
            content = findContentForKeywords(keywords.toArray(new String[0]));
        }

        if (content == null) return "Offline Study Partner: I'm ready to quiz you, but I need to know which subject. For example, try 'Quiz me on Biology'.";

        return createQuestionsFromContent(content);
    }

    private String createQuestionsFromContent(String html) {
        String text = html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        
        StringBuilder quiz = new StringBuilder("Offline Practice Challenge\n\n");
        quiz.append("I've analyzed your lesson. Can you answer these?\n\n");
        
        int questionCount = 0;
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < sentences.length; i++) indices.add(i);
        Collections.shuffle(indices);

        for (int idx : indices) {
            String sentence = sentences[idx];
            if (sentence.length() < 40 || sentence.length() > 150) continue;

            // Find a good word to blank out
            String[] words = sentence.split("\\s+");
            String blankWord = "";
            for (String word : words) {
                String clean = word.replaceAll("[^a-zA-Z]", "");
                if (clean.length() > 5 && !stopWords.contains(clean.toLowerCase())) {
                    blankWord = clean;
                    break;
                }
            }

            if (!blankWord.isEmpty()) {
                questionCount++;
                String question = sentence.replaceFirst(blankWord, "__________");
                quiz.append(questionCount).append(". ").append(question).append("\n");
                quiz.append("   (Answer: ").append(blankWord).append(")\n\n");
            }

            if (questionCount >= 3) break;
        }

        if (questionCount == 0) return "Offline Partner: I found the material, but it's a bit short to generate a quiz. Try a longer chapter!";
        
        quiz.append("How did you do? Keep studying to master this topic!");
        return quiz.toString();
    }

    private String getRandomLessonContent() {
        // Implementation to pick a random file from assets
        return findContentForKeywords(new String[]{"biology", "physics", "dynamics", "chemistry", "mathematics"});
    }

    private String findContentForKeywords(String[] keywords) {
        AssetManager am = context.getAssets();
        try {
            return searchRecursive(am, "subjects", keywords);
        } catch (IOException e) {
            return null;
        }
    }

    private String searchRecursive(AssetManager am, String path, String[] keywords) throws IOException {
        String[] items = am.list(path);
        if (items == null) return null;
        for (String item : items) {
            String fullPath = path.isEmpty() ? item : path + "/" + item;
            if (item.toLowerCase().endsWith(".html")) {
                String content = readAssetFile(fullPath);
                if (content != null && isRelevant(content, keywords)) return content;
            } else if (!item.contains(".")) {
                String result = searchRecursive(am, fullPath, keywords);
                if (result != null) return result;
            }
        }
        return null;
    }

    private String scanAllAssetsRecursively(String path, String[] keywords) {
        AssetManager am = context.getAssets();
        try {
            String[] items = am.list(path);
            if (items == null) return null;

            for (String item : items) {
                String fullPath = path.isEmpty() ? item : path + "/" + item;
                if (item.toLowerCase().endsWith(".html")) {
                    String content = readAssetFile(fullPath);
                    if (content != null && isRelevant(content, keywords)) {
                        return extractSmartSnippet(content, keywords);
                    }
                } else if (!item.contains(".")) {
                    String result = scanAllAssetsRecursively(fullPath, keywords);
                    if (result != null) return result;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Recursive scan failed", e);
        }
        return null;
    }

    private boolean isRelevant(String content, String[] keywords) {
        String lower = content.toLowerCase();
        int score = 0;
        for (String kw : keywords) {
            if (lower.contains(kw)) score++;
        }
        return score >= Math.max(1, keywords.length / 2);
    }

    private String extractSmartSnippet(String html, String[] keywords) {
        String text = html.replaceAll("<style[^>]*>.*?</style>", "")
                          .replaceAll("<script[^>]*>.*?</script>", "")
                          .replaceAll("<[^>]*>", " ")
                          .replaceAll("&nbsp;", " ")
                          .replaceAll("\\s+", " ");
        
        String lowerText = text.toLowerCase();
        int bestIdx = 0;
        int maxScore = 0;

        for (int i = 0; i < text.length() - 400; i += 100) {
            String window = lowerText.substring(i, i + 400);
            int score = 0;
            for (String kw : keywords) {
                if (window.contains(kw)) score += 5;
            }
            if (score > maxScore) {
                maxScore = score;
                bestIdx = i;
            }
        }

        String snippet = text.substring(bestIdx, Math.min(bestIdx + 500, text.length())).trim();
        if (bestIdx > 0) snippet = "..." + snippet;
        return snippet + "... [From Local Lesson]";
    }

    private String readAssetFile(String path) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(path), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append(" ");
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    public String explain(String topic, String selectedText) {
        String summary = summarize(selectedText);
        return "Offline AI Partner Explanation:\n\n" + summary + "\n\nStudy this concept carefully from your " + topic + " materials.";
    }

    private static class SentenceScore {
        String text; int score; int index;
        SentenceScore(String t, int s, int i) { text = t; score = s; index = i; }
    }
}
