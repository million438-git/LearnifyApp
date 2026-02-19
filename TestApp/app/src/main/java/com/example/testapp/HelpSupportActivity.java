package com.example.testapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HelpSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        setupToolbar();
        initSupportFeatures();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.helpToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initSupportFeatures() {
        TextView btnHowToUse = findViewById(R.id.btnHowToUse);
        TextView btnFAQs = findViewById(R.id.btnFAQs);
        TextView btnReportProblem = findViewById(R.id.btnReportProblem);
        TextView btnContactSupport = findViewById(R.id.btnContactSupport);

        btnHowToUse.setOnClickListener(v -> 
            Toast.makeText(this, "Opening User Guide...", Toast.LENGTH_SHORT).show());

        btnFAQs.setOnClickListener(v -> 
            Toast.makeText(this, "Opening Frequently Asked Questions...", Toast.LENGTH_SHORT).show());

        btnReportProblem.setOnClickListener(v -> 
            Toast.makeText(this, "Launching Problem Reporter...", Toast.LENGTH_SHORT).show());

        btnContactSupport.setOnClickListener(v -> 
            Toast.makeText(this, "Connecting to Chat Support...", Toast.LENGTH_SHORT).show());
    }
}
