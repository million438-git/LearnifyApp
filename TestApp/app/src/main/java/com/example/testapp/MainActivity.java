package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

/**
 * MainActivity handles the portal selection for Students and Admins.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        MaterialCardView cardStudentPortal = findViewById(R.id.cardStudentPortal);
        MaterialCardView cardAdminPortal = findViewById(R.id.cardAdminPortal);

        // Navigate to Student Login (The proper entry for students)
        if (cardStudentPortal != null) {
            cardStudentPortal.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, StudentLoginActivity.class);
                startActivity(intent);
            });
        }

        // Navigate to Admin Login
        if (cardAdminPortal != null) {
            cardAdminPortal.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
                startActivity(intent);
            });
        }
    }
}
