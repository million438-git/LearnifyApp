package com.million.learnify;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * AdminSupportManagementActivity - Student support and ticket resolution center.
 * Redesigned with Learnify Executive Academic Sapphire design system.
 * Inherits from BaseActivity for Role-Based Access Control (RBAC).
 */
public class AdminSupportManagementActivity extends BaseActivity {

    private LinearLayout ticketListContainer;
    private LinearLayout emptyTicketState;
    private TextView tvTicketCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // SECURITY: Only allow Admin users to enter this portal.
        enforceRole("ADMIN");
        
        setContentView(R.layout.activity_admin_support_management);

        Toolbar toolbar = findViewById(R.id.supportMgmtToolbar);
        setupPortalToolbar(toolbar, "Support Management");

        ticketListContainer = findViewById(R.id.ticketListContainer);
        emptyTicketState = findViewById(R.id.emptyTicketState);
        tvTicketCount = findViewById(R.id.tvTicketCount);

        loadTickets();
    }

    private void loadTickets() {
        ticketListContainer.removeAllViews();
        Cursor cursor = repository.getLocalDb().getAllSupportTickets();

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                count++;
                int id = cursor.getInt(0);
                String user = cursor.getString(1);
                String title = cursor.getString(2);
                String category = cursor.getString(3);
                String desc = cursor.getString(4);
                String status = cursor.getString(5);
                String reply = cursor.getString(6);

                addTicketCard(id, user, title, category, desc, status, reply);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (count > 0) {
            emptyTicketState.setVisibility(View.GONE);
            ticketListContainer.setVisibility(View.VISIBLE);
            tvTicketCount.setText(count + (count == 1 ? " Ticket" : " Tickets"));
        } else {
            emptyTicketState.setVisibility(View.VISIBLE);
            ticketListContainer.setVisibility(View.GONE);
            tvTicketCount.setText("0 Tickets");
        }
    }

    private void addTicketCard(int id, String user, String title, String category, String desc, String status, String reply) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_admin_support_ticket, ticketListContainer, false);

        TextView tvUser = card.findViewById(R.id.tvTicketUser);
        TextView tvCategory = card.findViewById(R.id.tvTicketCategory);
        TextView tvTitle = card.findViewById(R.id.tvTicketTitle);
        TextView tvDesc = card.findViewById(R.id.tvTicketDesc);
        TextView tvStatus = card.findViewById(R.id.tvTicketStatus);
        View layoutReply = card.findViewById(R.id.layoutAdminReply);
        TextView tvReply = card.findViewById(R.id.tvAdminReply);
        MaterialButton btnReply = card.findViewById(R.id.btnReplyTicket);

        tvUser.setText("From: " + (user != null ? user : "Student"));
        tvCategory.setText("Category: " + (category != null && !category.isEmpty() ? category : "General"));
        tvTitle.setText(title != null ? title : "Support Ticket");
        tvDesc.setText(desc != null ? desc : "No additional details provided.");

        String normStatus = status != null ? status.trim() : "Open";
        tvStatus.setText(normStatus.toUpperCase());

        if ("Resolved".equalsIgnoreCase(normStatus) || "Closed".equalsIgnoreCase(normStatus)) {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_active);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.emerald_accent));
        } else {
            tvStatus.setBackgroundResource(R.drawable.bg_badge_pending);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.amber_accent));
        }

        if (reply != null && !reply.trim().isEmpty()) {
            layoutReply.setVisibility(View.VISIBLE);
            tvReply.setText(reply.trim());
            btnReply.setText("Update Reply");
        } else {
            layoutReply.setVisibility(View.GONE);
            btnReply.setText("Reply / Resolve");
        }

        View.OnClickListener clickListener = v -> showReplyDialog(id, title, user, reply);
        card.setOnClickListener(clickListener);
        btnReply.setOnClickListener(clickListener);

        ticketListContainer.addView(card);
    }

    private void showReplyDialog(int id, String title, String user, String existingReply) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Resolve Ticket: " + (user != null ? user : "Student"));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, pad / 2);

        TextView tvSubject = new TextView(this);
        tvSubject.setText("Issue: " + (title != null ? title : "Inquiry"));
        tvSubject.setTextSize(13);
        tvSubject.setTextColor(ContextCompat.getColor(this, R.color.admin_text_secondary));
        tvSubject.setPadding(0, 0, 0, pad / 2);
        layout.addView(tvSubject);

        TextInputLayout til = new TextInputLayout(this, null, com.google.android.material.R.attr.textInputOutlinedStyle);
        til.setHint("Official Admin Response");
        til.setBoxCornerRadii(
                12 * getResources().getDisplayMetrics().density,
                12 * getResources().getDisplayMetrics().density,
                12 * getResources().getDisplayMetrics().density,
                12 * getResources().getDisplayMetrics().density
        );

        TextInputEditText input = new TextInputEditText(til.getContext());
        input.setLines(4);
        input.setGravity(android.view.Gravity.TOP);
        input.setTextColor(ContextCompat.getColor(this, R.color.admin_text_primary));
        if (existingReply != null && !existingReply.isEmpty()) {
            input.setText(existingReply);
            input.setSelection(existingReply.length());
        }
        til.addView(input);
        layout.addView(til);

        builder.setView(layout);

        builder.setPositiveButton("SEND RESPONSE", (dialog, which) -> {
            String replyText = input.getText() != null ? input.getText().toString().trim() : "";
            if (!replyText.isEmpty()) {
                repository.getLocalDb().updateTicketReply(id, replyText);
                repository.logSystemAction("Admin", "SUPPORT_REPLY", "Resolved ticket #" + id + " for student: " + user);
                Toast.makeText(this, "Reply dispatched to " + user, Toast.LENGTH_SHORT).show();
                loadTickets();
            } else {
                Toast.makeText(this, "Response cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }
}
