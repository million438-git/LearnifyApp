package com.example.testapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.textfield.TextInputEditText;

/**
 * PaymentActivity - Real-world ETB transaction submission with Screenshot proof.
 */
public class PaymentActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername;
    private TextView tvAdminAccounts, tvScreenshotStatus;
    private TextInputEditText etTransactionId;
    private RadioGroup rgPlans;
    private String selectedImageUri = "";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedImageUri = uri.toString();
                        tvScreenshotStatus.setText("📸 Screenshot attached!");
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        dbHelper = new DatabaseHelper(this);
        SharedPreferences pref = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUsername = pref.getString("username", "Student");

        setupToolbar();
        initViews();
        loadAdminAccountDetails();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.paymentToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        tvAdminAccounts = findViewById(R.id.tvAdminAccounts);
        tvScreenshotStatus = findViewById(R.id.tvScreenshotStatus);
        rgPlans = findViewById(R.id.rgPlans);
        etTransactionId = findViewById(R.id.etTransactionId);
        
        findViewById(R.id.btnSelectScreenshot).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        findViewById(R.id.btnSubmitPayment).setOnClickListener(v -> handlePaymentSubmission());
    }

    private void loadAdminAccountDetails() {
        tvAdminAccounts.setText(dbHelper.getAdminAccountDetails());
    }

    private void handlePaymentSubmission() {
        String txId = etTransactionId.getText().toString().trim();
        if (txId.isEmpty() || selectedImageUri.isEmpty()) {
            Toast.makeText(this, "Please enter ID and attach a Screenshot proof", Toast.LENGTH_SHORT).show();
            return;
        }

        String amount = rgPlans.getCheckedRadioButtonId() == R.id.rb50 ? "50 ETB" : "100 ETB";

        if (dbHelper.submitPayment(currentUsername, txId, amount, selectedImageUri)) {
            Toast.makeText(this, "🚀 Proof Sent! Admin will verify soon.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Submission failed.", Toast.LENGTH_SHORT).show();
        }
    }
}
