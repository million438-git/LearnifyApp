package com.million.learnify;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * AdminPaymentVerificationActivity - Professional tool to verify student ETB transactions.
 * Refactored to use DataRepository for Client-Server readiness.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 * 
 * Features:
 * - Real-time Financial KPI summary cards (Total, Pending, Approved, Rejected)
 * - Live search by student username or transaction reference
 * - Status filter chips (All, Pending, Approved, Rejected)
 * - Safe receipt preview with graceful fallback for remote/unshared content URIs
 * - Tap debouncing to prevent duplicate approval/rejection operations
 * - 100% preservation of database methods, RBAC, and approval workflows
 */
public class AdminPaymentVerificationActivity extends BaseActivity {

    // Core layout containers
    private LinearLayout paymentListContainer;
    private LinearLayout emptyPaymentState;
    private TextView tvPaymentCount;
    private TextView tvPaymentSectionTitle;
    private TextView tvEmptyPaymentTitle;
    private TextView tvEmptyPaymentSubtitle;
    private MaterialButton btnResetFilters;

    // Financial KPI Cards
    private TextView tvStatTotalCount;
    private TextView tvStatPendingCount;
    private TextView tvStatApprovedCount;
    private TextView tvStatRejectedCount;

    // Search and Status Filters
    private TextInputEditText etSearchPayment;
    private ChipGroup chipGroupPaymentStatus;
    private Chip chipPaymentAll;
    private Chip chipPaymentPending;
    private ChipPaymentHolder chipHolder;

    // State management
    private final List<PaymentRecord> allPayments = new ArrayList<>();
    private String currentSearchQuery = "";
    private String currentStatusFilter = "ALL";
    private boolean isDecisionInProgress = false;

    /**
     * Internal immutable representation of a payment row.
     */
    static class PaymentRecord {
        final int id;
        final String user;
        final String txId;
        final String amount;
        final String screenshotUri;
        final String status;
        final String timestamp;

        PaymentRecord(int id, String user, String txId, String amount, String screenshotUri, String status, String timestamp) {
            this.id = id;
            this.user = user != null && !user.trim().isEmpty() ? user.trim() : "Anonymous Student";
            this.txId = txId != null ? txId.trim() : "";
            this.amount = amount != null && !amount.trim().isEmpty() ? amount.trim() : "Free Tier";
            this.screenshotUri = screenshotUri != null ? screenshotUri.trim() : "";
            this.status = status != null && !status.trim().isEmpty() ? status.trim() : "Pending";
            this.timestamp = timestamp != null ? timestamp.trim() : "";
        }
    }

    private static class ChipPaymentHolder {
        Chip chipAll;
        Chip chipPending;
        Chip chipApproved;
        Chip chipRejected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_payment_verification);

        Toolbar toolbar = findViewById(R.id.paymentVerifyToolbar);
        setupPortalToolbar(toolbar, "Payment Verification");

        initViews();
        setupListeners();
        loadPendingPayments();
    }

    private void initViews() {
        paymentListContainer = findViewById(R.id.paymentListContainer);
        emptyPaymentState = findViewById(R.id.emptyPaymentState);
        tvPaymentCount = findViewById(R.id.tvPaymentCount);
        tvPaymentSectionTitle = findViewById(R.id.tvPaymentSectionTitle);
        tvEmptyPaymentTitle = findViewById(R.id.tvEmptyPaymentTitle);
        tvEmptyPaymentSubtitle = findViewById(R.id.tvEmptyPaymentSubtitle);
        btnResetFilters = findViewById(R.id.btnResetFilters);

        // KPI Stat Counters
        tvStatTotalCount = findViewById(R.id.tvStatTotalCount);
        tvStatPendingCount = findViewById(R.id.tvStatPendingCount);
        tvStatApprovedCount = findViewById(R.id.tvStatApprovedCount);
        tvStatRejectedCount = findViewById(R.id.tvStatRejectedCount);

        // Search & Filters
        etSearchPayment = findViewById(R.id.etSearchPayment);
        chipGroupPaymentStatus = findViewById(R.id.chipGroupPaymentStatus);

        chipHolder = new ChipPaymentHolder();
        chipHolder.chipAll = findViewById(R.id.chipPaymentAll);
        chipHolder.chipPending = findViewById(R.id.chipPaymentPending);
        chipHolder.chipApproved = findViewById(R.id.chipPaymentApproved);
        chipHolder.chipRejected = findViewById(R.id.chipPaymentRejected);
    }

    private void setupListeners() {
        if (etSearchPayment != null) {
            etSearchPayment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s != null ? s.toString().trim().toLowerCase(Locale.getDefault()) : "";
                    applyFilterAndRender();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (chipGroupPaymentStatus != null) {
            chipGroupPaymentStatus.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.chipPaymentPending) {
                    currentStatusFilter = "PENDING";
                } else if (checkedId == R.id.chipPaymentApproved) {
                    currentStatusFilter = "APPROVED";
                } else if (checkedId == R.id.chipPaymentRejected) {
                    currentStatusFilter = "REJECTED";
                } else {
                    currentStatusFilter = "ALL";
                }
                applyFilterAndRender();
            });
        }

        if (btnResetFilters != null) {
            btnResetFilters.setOnClickListener(v -> resetFilters());
        }
    }

    private void resetFilters() {
        if (etSearchPayment != null) {
            etSearchPayment.setText("");
        }
        currentSearchQuery = "";
        currentStatusFilter = "ALL";
        if (chipHolder != null && chipHolder.chipAll != null) {
            chipHolder.chipAll.setChecked(true);
        }
        applyFilterAndRender();
    }

    void loadPendingPayments() {
        allPayments.clear();
        Cursor cursor = repository.getAllPayments();

        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String user = cursor.getString(1);
                String txId = cursor.getString(2);
                String amount = cursor.getString(3);
                String screenshotUri = cursor.getString(4);
                String status = cursor.getString(5);
                String timestamp = cursor.getColumnCount() > 6 ? cursor.getString(6) : "";

                PaymentRecord record = new PaymentRecord(id, user, txId, amount, screenshotUri, status, timestamp);
                allPayments.add(record);

                if ("Approved".equalsIgnoreCase(record.status)) {
                    approvedCount++;
                } else if ("Rejected".equalsIgnoreCase(record.status)) {
                    rejectedCount++;
                } else {
                    pendingCount++;
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Update real financial summary KPI cards
        int totalCount = allPayments.size();
        if (tvStatTotalCount != null) tvStatTotalCount.setText(String.valueOf(totalCount));
        if (tvStatPendingCount != null) tvStatPendingCount.setText(String.valueOf(pendingCount));
        if (tvStatApprovedCount != null) tvStatApprovedCount.setText(String.valueOf(approvedCount));
        if (tvStatRejectedCount != null) tvStatRejectedCount.setText(String.valueOf(rejectedCount));

        applyFilterAndRender();
    }

    private void applyFilterAndRender() {
        if (paymentListContainer == null) return;
        paymentListContainer.removeAllViews();

        List<PaymentRecord> filteredList = new ArrayList<>();
        for (PaymentRecord record : allPayments) {
            // Apply status filter
            boolean matchesStatus;
            if ("PENDING".equalsIgnoreCase(currentStatusFilter)) {
                matchesStatus = "Pending".equalsIgnoreCase(record.status);
            } else if ("APPROVED".equalsIgnoreCase(currentStatusFilter)) {
                matchesStatus = "Approved".equalsIgnoreCase(record.status);
            } else if ("REJECTED".equalsIgnoreCase(currentStatusFilter)) {
                matchesStatus = "Rejected".equalsIgnoreCase(record.status);
            } else {
                matchesStatus = true;
            }

            if (!matchesStatus) continue;

            // Apply search query filter
            boolean matchesQuery = currentSearchQuery.isEmpty() ||
                    record.user.toLowerCase(Locale.getDefault()).contains(currentSearchQuery) ||
                    record.txId.toLowerCase(Locale.getDefault()).contains(currentSearchQuery) ||
                    record.amount.toLowerCase(Locale.getDefault()).contains(currentSearchQuery);

            if (matchesQuery) {
                filteredList.add(record);
            }
        }

        if (filteredList.isEmpty()) {
            if (emptyPaymentState != null) emptyPaymentState.setVisibility(View.VISIBLE);
            paymentListContainer.setVisibility(View.GONE);
            if (tvPaymentCount != null) tvPaymentCount.setText("0 Transactions");

            boolean isFiltered = !currentSearchQuery.isEmpty() || !"ALL".equalsIgnoreCase(currentStatusFilter);
            if (tvEmptyPaymentTitle != null) {
                tvEmptyPaymentTitle.setText(isFiltered ? "No Matching Transactions" : "No Pending Transactions");
            }
            if (tvEmptyPaymentSubtitle != null) {
                tvEmptyPaymentSubtitle.setText(isFiltered ?
                        "No payments match your current search query or status filter." :
                        "No payment records are available on this device.");
            }
            if (btnResetFilters != null) {
                btnResetFilters.setVisibility(isFiltered ? View.VISIBLE : View.GONE);
            }
        } else {
            if (emptyPaymentState != null) emptyPaymentState.setVisibility(View.GONE);
            paymentListContainer.setVisibility(View.VISIBLE);
            if (tvPaymentCount != null) {
                tvPaymentCount.setText(filteredList.size() + (filteredList.size() == 1 ? " Transaction" : " Transactions"));
            }

            for (PaymentRecord record : filteredList) {
                addPaymentRow(record.id, record.user, record.txId, record.amount, record.screenshotUri, record.status, record.timestamp);
            }
        }
    }

    void addPaymentRow(int id, String user, String txId, String amount, String screenshotUri, String status) {
        addPaymentRow(id, user, txId, amount, screenshotUri, status, "");
    }

    void addPaymentRow(int id, String user, String txId, String amount, String screenshotUri, String status, String timestamp) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_payment_admin, paymentListContainer, false);
        
        TextView tvUser = row.findViewById(R.id.tvPaymentUser);
        TextView tvTxId = row.findViewById(R.id.tvPaymentTxId);
        TextView tvAmount = row.findViewById(R.id.tvPaymentAmount);
        TextView tvStatus = row.findViewById(R.id.tvPaymentStatus);
        TextView tvProof = row.findViewById(R.id.tvProofBadge);
        TextView tvDate = row.findViewById(R.id.tvPaymentDate);

        tvUser.setText(user != null && !user.isEmpty() ? user : "Anonymous Student");
        tvTxId.setText("Tx Ref: " + (txId != null && !txId.isEmpty() ? txId : "N/A"));
        tvAmount.setText(amount != null && !amount.isEmpty() ? amount : "Free Tier");

        String normStatus = status != null ? status.trim() : "Pending";
        tvStatus.setText(normStatus.toUpperCase(Locale.getDefault()));

        if ("Approved".equalsIgnoreCase(normStatus)) {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_active);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.emerald_accent));
        } else if ("Rejected".equalsIgnoreCase(normStatus)) {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_blocked);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.rose_accent));
        } else {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_pending);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.amber_accent));
        }

        if (screenshotUri != null && !screenshotUri.isEmpty()) {
            tvProof.setVisibility(View.VISIBLE);
            tvProof.setText("Receipt Attached");
        } else {
            tvProof.setVisibility(View.GONE);
        }

        if (tvDate != null && timestamp != null && !timestamp.isEmpty()) {
            tvDate.setVisibility(View.VISIBLE);
            tvDate.setText(timestamp);
        }

        row.setOnClickListener(v -> showVerificationDialog(id, user, txId, amount, screenshotUri, normStatus, timestamp));

        paymentListContainer.addView(row);
    }

    void showVerificationDialog(int id, String user, String txId, String amount, String screenshotUri, String currentStatus) {
        showVerificationDialog(id, user, txId, amount, screenshotUri, currentStatus, "");
    }

    void showVerificationDialog(int id, String user, String txId, String amount, String screenshotUri, String currentStatus, String timestamp) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Verify Transaction #" + id);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, pad / 2);

        StringBuilder sb = new StringBuilder();
        sb.append("Student Account: ").append(user != null ? user : "N/A").append("\n");
        sb.append("Transaction ID: ").append(txId != null && !txId.isEmpty() ? txId : "N/A").append("\n");
        sb.append("Plan / Amount: ").append(amount != null && !amount.isEmpty() ? amount : "Free Tier").append("\n");
        sb.append("Current Status: ").append(currentStatus);
        if (timestamp != null && !timestamp.isEmpty()) {
            sb.append("\nSubmitted At: ").append(timestamp);
        }

        TextView info = new TextView(this);
        info.setText(sb.toString());
        info.setTextSize(14);
        info.setTextColor(ContextCompat.getColor(this, R.color.admin_text_primary));
        info.setLineSpacing(0, 1.25f);
        layout.addView(info);

        if (screenshotUri != null && !screenshotUri.isEmpty()) {
            TextView proofLabel = new TextView(this);
            proofLabel.setText("Receipt Image Proof:");
            proofLabel.setTextSize(13);
            proofLabel.setTypeface(null, android.graphics.Typeface.BOLD);
            proofLabel.setTextColor(ContextCompat.getColor(this, R.color.admin_text_secondary));
            proofLabel.setPadding(0, pad / 2, 0, pad / 4);
            layout.addView(proofLabel);

            // Attempt safe image decoding with honest unavailable state if local device cannot open it
            Bitmap receiptBitmap = tryDecodeReceiptBitmap(screenshotUri);

            if (receiptBitmap != null) {
                ImageView ivProof = new ImageView(this);
                int height = (int) (240 * getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
                ivProof.setLayoutParams(imgParams);
                ivProof.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivProof.setBackgroundColor(ContextCompat.getColor(this, R.color.admin_bg));
                ivProof.setImageBitmap(receiptBitmap);
                layout.addView(ivProof);
            } else {
                // Honest unavailable preview fallback without deleting or breaking the receipt reference
                LinearLayout unavailableCard = new LinearLayout(this);
                unavailableCard.setOrientation(LinearLayout.VERTICAL);
                unavailableCard.setPadding(pad / 2, pad / 2, pad / 2, pad / 2);
                unavailableCard.setBackgroundResource(R.drawable.bg_admin_stat_card);

                TextView tvUnavailableTitle = new TextView(this);
                tvUnavailableTitle.setText("Evidence Reference Attached");
                tvUnavailableTitle.setTextSize(13);
                tvUnavailableTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                tvUnavailableTitle.setTextColor(ContextCompat.getColor(this, R.color.admin_sapphire_primary));
                unavailableCard.addView(tvUnavailableTitle);

                TextView tvUnavailableDesc = new TextView(this);
                tvUnavailableDesc.setText("The receipt is stored in app-private storage on the submitting device. No shared receipt service is configured, so it cannot be securely reviewed here.");
                tvUnavailableDesc.setTextSize(12);
                tvUnavailableDesc.setTextColor(ContextCompat.getColor(this, R.color.admin_text_secondary));
                tvUnavailableDesc.setPadding(0, 4, 0, 0);
                unavailableCard.addView(tvUnavailableDesc);

                layout.addView(unavailableCard);
            }
        }

        builder.setView(layout);

        builder.setPositiveButton("APPROVE & UNLOCK", (dialog, which) -> {
            if (isDecisionInProgress) return;
            isDecisionInProgress = true;
            try {
                repository.adminUpdatePayment(id, "Approved");
                repository.logSystemAction("Admin", "PAYMENT_APPROVED", "Approved payment ID: " + id + " for user: " + user);
                Toast.makeText(this, "Student " + user + " unlocked.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to update payment: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                isDecisionInProgress = false;
                loadPendingPayments();
            }
        });

        builder.setNegativeButton("REJECT", (dialog, which) -> {
            if (isDecisionInProgress) return;
            isDecisionInProgress = true;
            try {
                repository.adminUpdatePayment(id, "Rejected");
                repository.logSystemAction("Admin", "PAYMENT_REJECTED", "Rejected payment ID: " + id + " for user: " + user);
                Toast.makeText(this, "Payment rejected for " + user, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Failed to update payment: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                isDecisionInProgress = false;
                loadPendingPayments();
            }
        });

        builder.setNeutralButton("CLOSE", null);
        builder.show();
    }

    /**
     * Attempts to safely decode an attached receipt image from local storage or content provider.
     * Returns null if the URI cannot be opened on this device.
     */
    private Bitmap tryDecodeReceiptBitmap(String uriStr) {
        if (uriStr == null || uriStr.trim().isEmpty()) return null;
        try {
            Uri uri = Uri.parse(uriStr);
            String scheme = uri.getScheme();
            if ("content".equalsIgnoreCase(scheme)) {
                try (InputStream is = getContentResolver().openInputStream(uri)) {
                    if (is != null) {
                        return BitmapFactory.decodeStream(is);
                    }
                }
            } else if ("file".equalsIgnoreCase(scheme) || uriStr.startsWith("/")) {
                String path = "file".equalsIgnoreCase(scheme) ? uri.getPath() : uriStr;
                if (path != null) {
                    File file = new File(path);
                    if (file.exists() && file.canRead()) {
                        return BitmapFactory.decodeFile(file.getAbsolutePath());
                    }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
