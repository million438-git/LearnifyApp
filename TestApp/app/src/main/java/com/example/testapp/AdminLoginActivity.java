package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText etAdminUser, etAdminPassword;
    private MaterialButton btnAdminLogin;
    private TextView tvAdminRegisterLink;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        dbHelper = new DatabaseHelper(this);
        
        etAdminUser = findViewById(R.id.etAdminUser);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        tvAdminRegisterLink = findViewById(R.id.tvAdminRegisterLink);

        btnAdminLogin.setOnClickListener(v -> {
            String user = etAdminUser.getText().toString().trim();
            String pass = etAdminPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter admin credentials", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify against 'ADMIN' user type in database
            if (dbHelper.checkUser(user, pass, "ADMIN")) {
                Toast.makeText(this, "Admin access granted. Welcome!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Unauthorized admin access attempt blocked.", Toast.LENGTH_SHORT).show();
            }
        });

        tvAdminRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminRegisterActivity.class));
        });
    }
}
