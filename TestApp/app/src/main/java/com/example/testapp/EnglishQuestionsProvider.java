package com.example.testapp;

import java.util.ArrayList;
import java.util.List;

public class EnglishQuestionsProvider {
    public static List<Question> getQuestions() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question(
            "Identify the noun in this sentence: 'The cat sat on the mat.'",
            "Sat", "On", "Cat", "The",
            2, // Index 2 for "Cat"
            "A noun is a word that identifies a person, place, or thing. In this sentence, 'cat' and 'mat' are nouns."
        ));
        questions.add(new Question(
            "What is the past tense of 'run'?",
            "Running", "Ran", "Runs", "Runned",
            1, // Index 1 for "Ran"
            "The verb 'run' is irregular. Its past tense is 'ran'."
        ));
        return questions;
    }
    
    public static String getNotes() {
        return "English Language Notes:\n\n" +
               "1. Parts of Speech: Nouns, Verbs, Adjectives, and Adverbs.\n" +
               "2. Tenses: Present, Past, and Future are the three main time frames.\n" +
               "3. Punctuation: Always capitalize the first letter of a sentence.";
    }
}
