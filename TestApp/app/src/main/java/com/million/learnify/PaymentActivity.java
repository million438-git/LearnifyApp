package com.million.learnify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * PaymentActivity - Professional Fintech-grade Student Payment & Premium Access System.
 * Features:
 * 1. 3-Day Free Trial live status & countdown.
 * 2. Plan A (50 ETB - Math & English) vs Plan B (100 ETB - Full Lifetime Access) selection.
 * 3. Commercial Bank of Ethiopia (CBE) & Telebirr recipient cards with one-tap copy.
 * 4. App-internal persistent receipt image storage (no broken content:// references).
 * 5. Debounced transaction submission with honest manual verification workflow.
 * 6. Dynamic in-app student payment history with live status chips (Pending, Approved, Rejected).
 */
public class PaymentActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private String currentUsername;

    // Trial / Status UI
    private MaterialCardView cardTrialStatus;
    private ImageView ivTrialIcon;
    private TextView tvTrialTitle, tvTrialCountdown, chipTrialBadge;

    // Plan Selection UI
    private MaterialCardView cardPlan50, cardPlan100;
    private RadioGroup rgPlans;
    private RadioButton rb50, rb100;

    // Account Details & Copy
    private TextView tvAdminAccounts;
    private MaterialButton btnCopyCbe, btnCopyTelebirr;
    private static final String CBE_ACCOUNT_NUMBER = "1000591245407";
    private static final String TELEBIRR_NUMBER = "0902939206";

    // Form & Receipt UI
    private TextInputEditText etTransactionId;
    private MaterialButton btnSelectScreenshot, btnRemoveScreenshot, btnSubmitPayment;
    private LinearLayout layoutReceiptPreview;
    private ImageView ivReceiptThumbnail;
    private TextView tvReceiptFileName, tvScreenshotStatus;

    // Payment History UI
    private LinearLayout paymentHistoryContainer;
    private TextView tvNoPaymentHistory, tvHistoryCount;

    // Internal state
    private String selectedImagePath = "";
    private long lastSubmitClickTime = 0;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handlePickedImage(uri);
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
        updateTrialAndMembershipStatus();
        loadStudentPaymentHistory();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.paymentToolbar);
        if (toolbar != null) {
            setupPortalToolbar(toolbar, "Enrollment & Premium Access");
        }
    }

    private void initViews() {
        // Trial Status
        cardTrialStatus = findViewById(R.id.cardTrialStatus);
        ivTrialIcon = findViewById(R.id.ivTrialIcon);
        tvTrialTitle = findViewById(R.id.tvTrialTitle);
        tvTrialCountdown = findViewById(R.id.tvTrialCountdown);
        chipTrialBadge = findViewById(R.id.chipTrialBadge);

        // Plans
        cardPlan50 = findViewById(R.id.cardPlan50);
        cardPlan100 = findViewById(R.id.cardPlan100);
        rgPlans = findViewById(R.id.rgPlans);
        rb50 = findViewById(R.id.rb50);
        rb100 = findViewById(R.id.rb100);

        // Account Details & Copy
        tvAdminAccounts = findViewById(R.id.tvAdminAccounts);
        btnCopyCbe = findViewById(R.id.btnCopyCbe);
        btnCopyTelebirr = findViewById(R.id.btnCopyTelebirr);

        // Form
        etTransactionId = findViewById(R.id.etTransactionId);
        btnSelectScreenshot = findViewById(R.id.btnSelectScreenshot);
        btnRemoveScreenshot = findViewById(R.id.btnRemoveScreenshot);
        btnSubmitPayment = findViewById(R.id.btnSubmitPayment);
        layoutReceiptPreview = findViewById(R.id.layoutReceiptPreview);
        ivReceiptThumbnail = findViewById(R.id.ivReceiptThumbnail);
        tvReceiptFileName = findViewById(R.id.tvReceiptFileName);
        tvScreenshotStatus = findViewById(R.id.tvScreenshotStatus);

        // History
        paymentHistoryContainer = findViewById(R.id.paymentHistoryContainer);
        tvNoPaymentHistory = findViewById(R.id.tvNoPaymentHistory);
        tvHistoryCount = findViewById(R.id.tvHistoryCount);

        // Plan Selection Listeners
        if (cardPlan50 != null) {
            cardPlan50.setOnClickListener(v -> selectPlan(false));
        }
        if (cardPlan100 != null) {
            cardPlan100.setOnClickListener(v -> selectPlan(true));
        }

        // Account Copy Listeners
        if (btnCopyCbe != null) {
            btnCopyCbe.setOnClickListener(v -> copyToClipboard("CBE Account Number", CBE_ACCOUNT_NUMBER));
        }
        if (btnCopyTelebirr != null) {
            btnCopyTelebirr.setOnClickListener(v -> copyToClipboard("Telebirr Number", TELEBIRR_NUMBER));
        }

        // Image Picker Listener
        if (btnSelectScreenshot != null) {
            btnSelectScreenshot.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                try {
                    imagePickerLauncher.launch(intent);
                } catch (Exception e) {
                    Intent fallback = new Intent(Intent.ACTION_GET_CONTENT);
                    fallback.setType("image/*");
                    imagePickerLauncher.launch(Intent.createChooser(fallback, "Select Receipt"));
                }
            });
        }

        // Remove Receipt Listener
        if (btnRemoveScreenshot != null) {
            btnRemoveScreenshot.setOnClickListener(v -> clearReceiptPreview());
        }

        // Submit Listener
        if (btnSubmitPayment != null) {
            btnSubmitPayment.setOnClickListener(v -> handlePaymentSubmission());
        }

        // Default plan selection: Plan B (100 ETB - Recommended)
        selectPlan(true);
    }

    private void selectPlan(boolean isPlanB) {
        if (isPlanB) {
            if (rb100 != null) rb100.setChecked(true);
            if (rb50 != null) rb50.setChecked(false);
            if (cardPlan100 != null) {
                cardPlan100.setStrokeColor(ContextCompat.getColor(this, R.color.admin_green));
                cardPlan100.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
                cardPlan100.setCardBackgroundColor(0xFFF0FDF4);
            }
            if (cardPlan50 != null) {
                cardPlan50.setStrokeColor(0xFFCBD5E1);
                cardPlan50.setStrokeWidth((int) (1.5f * getResources().getDisplayMetrics().density));
                cardPlan50.setCardBackgroundColor(0xFFFFFFFF);
            }
        } else {
            if (rb50 != null) rb50.setChecked(true);
            if (rb100 != null) rb100.setChecked(false);
            if (cardPlan50 != null) {
                cardPlan50.setStrokeColor(ContextCompat.getColor(this, R.color.colorPrimary));
                cardPlan50.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
                cardPlan50.setCardBackgroundColor(0xFFEEF2FF);
            }
            if (cardPlan100 != null) {
                cardPlan100.setStrokeColor(0xFFCBD5E1);
                cardPlan100.setStrokeWidth((int) (1.5f * getResources().getDisplayMetrics().density));
                cardPlan100.setCardBackgroundColor(0xFFFFFFFF);
            }
        }
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, label + " copied: " + text, Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePickedImage(Uri uri) {
        try {
            // Save receipt into private app-internal directory so the file persists
            File receiptsDir = new File(getFilesDir(), "receipts");
            if (!receiptsDir.exists()) {
                receiptsDir.mkdirs();
            }

            String fileName = "receipt_" + System.currentTimeMillis() + ".jpg";
            File targetFile = new File(receiptsDir, fileName);

            InputStream in = getContentResolver().openInputStream(uri);
            if (in != null) {
                FileOutputStream out = new FileOutputStream(targetFile);
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
                out.close();
                in.close();

                selectedImagePath = targetFile.getAbsolutePath();

                // Decode thumbnail safely
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 2;
                Bitmap bmp = BitmapFactory.decodeFile(selectedImagePath, opts);
                if (bmp != null && ivReceiptThumbnail != null) {
                    ivReceiptThumbnail.setImageBitmap(bmp);
                }

                if (layoutReceiptPreview != null) layoutReceiptPreview.setVisibility(View.VISIBLE);
                if (tvReceiptFileName != null) tvReceiptFileName.setText(fileName + " (" + (targetFile.length() / 1024) + " KB)");
                if (tvScreenshotStatus != null) {
                    tvScreenshotStatus.setText("Receipt attached. Payment has not been verified.");
                    tvScreenshotStatus.setTextColor(0xFFD97706);
                }
            } else {
                selectedImagePath = uri.toString();
                if (tvScreenshotStatus != null) tvScreenshotStatus.setText("Screenshot attached");
            }
        } catch (Exception e) {
            // Fallback to raw uri string if internal copy fails
            selectedImagePath = uri.toString();
            if (tvScreenshotStatus != null) tvScreenshotStatus.setText("Screenshot attached");
        }
    }

    private void clearReceiptPreview() {
        selectedImagePath = "";
        if (layoutReceiptPreview != null) layoutReceiptPreview.setVisibility(View.GONE);
        if (ivReceiptThumbnail != null) ivReceiptThumbnail.setImageDrawable(null);
        if (tvScreenshotStatus != null) {
            tvScreenshotStatus.setText("No file selected");
            tvScreenshotStatus.setTextColor(0xFF94A3B8);
        }
    }

    private void loadAdminAccountDetails() {
        if (tvAdminAccounts != null) {
            tvAdminAccounts.setText(dbHelper.getAdminAccountDetails());
        }
    }

    private void updateTrialAndMembershipStatus() {
        if (tvTrialTitle == null || tvTrialCountdown == null || chipTrialBadge == null) return;

        // 1. Check if user already has an approved payment
        boolean hasApproved100 = false;
        boolean hasApproved50 = false;
        boolean hasPending = false;

        Cursor c = null;
        try {
            c = dbHelper.getReadableDatabase().rawQuery("SELECT amount, status FROM payments WHERE username=? ORDER BY id DESC", new String[]{currentUsername});
            if (c != null && c.moveToFirst()) {
                do {
                    String amt = c.getString(0);
                    String st = c.getString(1);
                    if ("Approved".equalsIgnoreCase(st)) {
                        if (amt != null && amt.contains("100")) hasApproved100 = true;
                        else hasApproved50 = true;
                    } else if ("Pending".equalsIgnoreCase(st)) {
                        hasPending = true;
                    }
                } while (c.moveToNext());
            }
        } catch (Exception ignored) {
        } finally {
            if (c != null) c.close();
        }

        if (hasApproved100) {
            tvTrialTitle.setText("Full Lifetime Access Active");
            tvTrialCountdown.setText("All 5 subjects and advanced features are unlocked");
            chipTrialBadge.setText("ACTIVE");
            chipTrialBadge.setBackgroundResource(R.drawable.bg_badge_active);
            chipTrialBadge.setTextColor(0xFF059669);
            if (cardTrialStatus != null) {
                cardTrialStatus.setCardBackgroundColor(0xFFF0FDF4);
                cardTrialStatus.setStrokeColor(0xFF86EFAC);
            }
            return;
        }

        if (hasApproved50) {
            tvTrialTitle.setText("Standard Pack Active (Plan A)");
            tvTrialCountdown.setText("Math & English modules unlocked • Upgrade anytime");
            chipTrialBadge.setText("PLAN A");
            chipTrialBadge.setBackgroundResource(R.drawable.bg_badge_active);
            chipTrialBadge.setTextColor(0xFF059669);
            if (cardTrialStatus != null) {
                cardTrialStatus.setCardBackgroundColor(0xFFEEF2FF);
                cardTrialStatus.setStrokeColor(0xFFC7D2FE);
            }
            return;
        }

        if (hasPending) {
            tvTrialTitle.setText("Payment Under Admin Verification");
            tvTrialCountdown.setText("Verification usually completes within 1–4 hours");
            chipTrialBadge.setText("PENDING");
            chipTrialBadge.setBackgroundResource(R.drawable.bg_badge_pending);
            chipTrialBadge.setTextColor(0xFFD97706);
            if (cardTrialStatus != null) {
                cardTrialStatus.setCardBackgroundColor(0xFFFFFBEB);
                cardTrialStatus.setStrokeColor(0xFFFDE68A);
            }
            return;
        }

        // Check 3-day trial
        if (dbHelper.isTrialActive(currentUsername)) {
            long remainingMillis = dbHelper.getTrialRemainingMillis(currentUsername);
            long hours = remainingMillis / (3600 * 1000L);
            long mins = (remainingMillis % (3600 * 1000L)) / (60 * 1000L);

            tvTrialTitle.setText("3-Day Free Trial Active");
            tvTrialCountdown.setText(hours + "h " + mins + "m remaining • Full access to explore all subjects");
            chipTrialBadge.setText("TRIAL");
            chipTrialBadge.setBackgroundResource(R.drawable.bg_badge_active);
            chipTrialBadge.setTextColor(0xFF059669);
            if (cardTrialStatus != null) {
                cardTrialStatus.setCardBackgroundColor(0xFFEFF6FF);
                cardTrialStatus.setStrokeColor(0xFFBFDBFE);
            }
        } else {
            tvTrialTitle.setText("Free Trial Expired");
            tvTrialCountdown.setText("Select Plan A or Plan B below to unlock your subjects");
            chipTrialBadge.setText("EXPIRED");
            chipTrialBadge.setBackgroundResource(R.drawable.bg_badge_blocked);
            chipTrialBadge.setTextColor(0xFFEF4444);
            if (cardTrialStatus != null) {
                cardTrialStatus.setCardBackgroundColor(0xFFFFF1F2);
                cardTrialStatus.setStrokeColor(0xFFFECDD3);
            }
        }
    }

    private void handlePaymentSubmission() {
        // Debounce double-clicks
        if (SystemClock.elapsedRealtime() - lastSubmitClickTime < 2000) {
            return;
        }
        lastSubmitClickTime = SystemClock.elapsedRealtime();

        String txId = etTransactionId != null && etTransactionId.getText() != null
                ? etTransactionId.getText().toString().trim()
                : "";

        if (txId.isEmpty()) {
            if (etTransactionId != null) etTransactionId.setError("Enter bank transaction ID");
            Toast.makeText(this, "Please enter your bank Transaction ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (txId.length() < 4) {
            if (etTransactionId != null) etTransactionId.setError("Transaction ID is too short");
            Toast.makeText(this, "Invalid transaction ID format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImagePath == null || selectedImagePath.isEmpty()) {
            Toast.makeText(this, "Please attach a screenshot of your payment receipt", Toast.LENGTH_LONG).show();
            return;
        }

        String amount = (rb50 != null && rb50.isChecked()) ? "50 ETB" : "100 ETB";

        boolean success = dbHelper.submitPayment(currentUsername, txId, amount, selectedImagePath);
        if (success) {
            Toast.makeText(this, "Proof submitted. Administrator will verify your transaction shortly.", Toast.LENGTH_LONG).show();
            if (etTransactionId != null) etTransactionId.setText("");
            clearReceiptPreview();
            updateTrialAndMembershipStatus();
            loadStudentPaymentHistory();
        } else {
            Toast.makeText(this, "Submission failed. Please check your data and retry.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadStudentPaymentHistory() {
        if (paymentHistoryContainer == null) return;
        paymentHistoryContainer.removeAllViews();

        Cursor cursor = null;
        int count = 0;
        try {
            cursor = dbHelper.getStudentPayments(currentUsername);
            if (cursor != null && cursor.moveToFirst()) {
                LayoutInflater inflater = LayoutInflater.from(this);
                do {
                    count++;
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String txId = cursor.getString(cursor.getColumnIndexOrThrow("transaction_id"));
                    String amount = cursor.getString(cursor.getColumnIndexOrThrow("amount"));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    String timestamp = cursor.getColumnIndex("timestamp") != -1 ? cursor.getString(cursor.getColumnIndexOrThrow("timestamp")) : "";

                    View itemView = inflater.inflate(R.layout.item_student_payment, paymentHistoryContainer, false);
                    TextView tvAmount = itemView.findViewById(R.id.tvPaymentAmount);
                    TextView tvTx = itemView.findViewById(R.id.tvPaymentTxId);
                    TextView tvTime = itemView.findViewById(R.id.tvPaymentTimestamp);
                    TextView tvBadge = itemView.findViewById(R.id.tvPaymentStatusBadge);

                    if (tvAmount != null) tvAmount.setText(amount);
                    if (tvTx != null) tvTx.setText("Tx: " + txId);
                    if (tvTime != null) tvTime.setText(timestamp != null && !timestamp.isEmpty() ? timestamp : "Recently submitted");

                    if (tvBadge != null) {
                        if ("Approved".equalsIgnoreCase(status)) {
                            tvBadge.setText("Approved");
                            tvBadge.setBackgroundResource(R.drawable.bg_badge_active);
                            tvBadge.setTextColor(0xFF059669);
                        } else if ("Rejected".equalsIgnoreCase(status)) {
                            tvBadge.setText("Rejected");
                            tvBadge.setBackgroundResource(R.drawable.bg_badge_blocked);
                            tvBadge.setTextColor(0xFFEF4444);
                        } else {
                            tvBadge.setText("Pending");
                            tvBadge.setBackgroundResource(R.drawable.bg_badge_pending);
                            tvBadge.setTextColor(0xFFD97706);
                        }
                    }

                    paymentHistoryContainer.addView(itemView);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("PaymentActivity", "Failed to load payment history", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        if (tvHistoryCount != null) {
            tvHistoryCount.setText(count + (count == 1 ? " record" : " records"));
        }

        if (tvNoPaymentHistory != null) {
            tvNoPaymentHistory.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
        }
    }
}
