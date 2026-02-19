package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

/**
 * WelcomeActivity - A professional and descriptive entry page for AceGrade.
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Fixed: Matching the ID from activity_welcome.xml
        MaterialButton btnGetStarted = findViewById(R.id.btnGetStarted);
        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> {
                // Smoothly transition to the main portal selection page
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                // Finish this so the user doesn't go back to the welcome page
                finish();
            });
        }
    }
}
