package com.million.learnify;

import java.util.ArrayList;
import java.util.List;

public class MathQuestionsProvider {
    public static List<Question> getQuestions() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question(
            "What is the value of x in 2x + 5 = 15?",
            "x = 5", "x = 10", "x = 7", "x = 15",
            0, // Index 0 for "x = 5"
            "Subtract 5 from both sides: 2x = 10. Then divide by 2: x = 5."
        ));
        questions.add(new Question(
            "What is the square root of 144?",
            "10", "12", "14", "16",
            1, // Index 1 for "12"
            "12 * 12 = 144, so the square root is 12."
        ));
        return questions;
    }
    
    public static String getNotes() {
        return "Mathematics Notes:\n\n" +
               "1. Algebra: Focus on balancing equations. Whatever you do to one side, do to the other.\n" +
               "2. Geometry: Area of a circle = πr².\n" +
               "3. Calculus: Derivatives represent the rate of change.";
    }
}
