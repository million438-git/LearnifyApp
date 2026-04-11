package com.million.learnify;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

/**
 * AdminPaymentVerificationActivity - Professional tool to verify student ETB transactions.
 * Refactored to use DataRepository for Client-Server readiness.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class AdminPaymentVerificationActivity extends BaseActivity {

    private LinearLayout paymentListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_payment_verification);

        setupToolbar();
        paymentListContainer = findViewById(R.id.paymentListContainer);
        loadPendingPayments();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.paymentVerifyToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment Verification Hub");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadPendingPayments() {
        paymentListContainer.removeAllViews();
        // Fetch via Repository
        Cursor cursor = repository.getAllPayments();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String user = cursor.getString(1);
                String txId = cursor.getString(2);
                String amount = cursor.getString(3);
                String screenshotUri = cursor.getString(4);
                String status = cursor.getString(5);

                addPaymentRow(id, user, txId, amount, screenshotUri, status);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            showEmptyState();
        }
    }

    private void addPaymentRow(int id, String user, String txId, String amount, String screenshotUri, String status) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_admin_user, paymentListContainer, false);
        
        TextView tvName = row.findViewById(R.id.tvUserName);
        TextView tvDetails = row.findViewById(R.id.tvUserRole);
        TextView tvStatus = row.findViewById(R.id.tvUserStatus);

        tvName.setText("Student: " + user);
        tvDetails.setText("ID: " + txId + " | " + amount);
        tvStatus.setText(status.toUpperCase());

        row.setOnClickListener(v -> showVerificationDialog(id, user, txId, amount, screenshotUri));

        paymentListContainer.addView(row);
    }

    private void showVerificationDialog(int id, String user, String txId, String amount, String screenshotUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        TextView title = new TextView(this);
        title.setText("Verify Payment Proof");
        title.setPadding(40, 40, 40, 20);
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        builder.setCustomTitle(title);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        TextView info = new TextView(this);
        info.setText("Student: " + user + "\nTransaction ID: " + txId + "\nAmount: " + amount);
        info.setTextSize(16);
        layout.addView(info);

        if (screenshotUri != null && !screenshotUri.isEmpty()) {
            ImageView ivProof = new ImageView(this);
            ivProof.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 600));
            ivProof.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivProof.setPadding(0, 20, 0, 20);
            try {
                ivProof.setImageURI(Uri.parse(screenshotUri));
            } catch (Exception e) {
                ivProof.setImageResource(android.R.drawable.ic_menu_gallery);
            }
            layout.addView(ivProof);
        }

        builder.setView(layout);

        builder.setPositiveButton("APPROVE ✅", (dialog, which) -> {
            // Update via Repository
            repository.adminUpdatePayment(id, "Approved");
            repository.logSystemAction("Admin", "PAYMENT_APPROVED", "Approved payment ID: " + id + " for user: " + user);
            Toast.makeText(this, "Student " + user + " unlocked!", Toast.LENGTH_SHORT).show();
            loadPendingPayments();
        });

        builder.setNegativeButton("REJECT ❌", (dialog, which) -> {
            repository.adminUpdatePayment(id, "Rejected");
            repository.logSystemAction("Admin", "PAYMENT_REJECTED", "Rejected payment ID: " + id + " for user: " + user);
            loadPendingPayments();
        });

        builder.setNeutralButton("CLOSE", null);
        builder.show();
    }

    private void showEmptyState() {
        TextView tv = new TextView(this);
        tv.setText("📭 No student transactions waiting for verification.");
        tv.setPadding(0, 150, 0, 0);
        tv.setGravity(android.view.Gravity.CENTER);
        paymentListContainer.addView(tv);
    }
}
