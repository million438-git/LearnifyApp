package com.million.learnify;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AdminQuestionActivity extends AppCompatActivity {

    private TextInputEditText etQuestion, etOptA, etOptB, etOptC, etOptD, etExplanation;
    private AutoCompleteTextView actvSubject, actvGrade, actvAnswer;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_question);

        dbHelper = new DatabaseHelper(this);
        setupToolbar();
        initViews();
        setupDropdowns();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.questionToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        etQuestion = findViewById(R.id.etQuestion);
        etOptA = findViewById(R.id.etOptA);
        etOptB = findViewById(R.id.etOptB);
        etOptC = findViewById(R.id.etOptC);
        etOptD = findViewById(R.id.etOptD);
        etExplanation = findViewById(R.id.etExplanation);
        actvSubject = findViewById(R.id.actvSubject);
        actvGrade = findViewById(R.id.actvGrade);
        actvAnswer = findViewById(R.id.actvAnswer);

        MaterialButton btnPublish = findViewById(R.id.btnPublishQuestion);
        btnPublish.setOnClickListener(v -> handlePublish());
    }

    private void setupDropdowns() {
        String[] subjects = {"Mathematics", "English", "Physics", "Biology", "Chemistry", "Aptitude", "General"};
        actvSubject.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects));

        String[] grades = {"9", "10", "11", "12", "General"};
        actvGrade.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, grades));

        String[] answers = {"A", "B", "C", "D"};
        actvAnswer.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, answers));
    }

    private void handlePublish() {
        String question = etQuestion.getText().toString().trim();
        String a = etOptA.getText().toString().trim();
        String b = etOptB.getText().toString().trim();
        String c = etOptC.getText().toString().trim();
        String d = etOptD.getText().toString().trim();
        String ans = actvAnswer.getText().toString().trim();
        String sub = actvSubject.getText().toString().trim();
        String grade = actvGrade.getText().toString().trim();
        String expl = etExplanation.getText().toString().trim();

        if (question.isEmpty() || a.isEmpty() || b.isEmpty() || ans.isEmpty() || sub.isEmpty() || grade.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Map A,B,C,D to the actual text for the DB helper
        String actualAns = "";
        if (ans.equals("A")) actualAns = a;
        else if (ans.equals("B")) actualAns = b;
        else if (ans.equals("C")) actualAns = c;
        else if (ans.equals("D")) actualAns = d;

        boolean success = dbHelper.insertDailyQuestion(question, a, b, c, d, actualAns, grade, sub, "Medium", expl);
        if (success) {
            Toast.makeText(this, "✅ Exam Question Published to Students!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Error publishing question.", Toast.LENGTH_SHORT).show();
        }
    }
}
