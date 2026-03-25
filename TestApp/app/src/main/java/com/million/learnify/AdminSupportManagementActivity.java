package com.million.learnify;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.card.MaterialCardView;

public class AdminSupportManagementActivity extends AppCompatActivity {

    private LinearLayout ticketListContainer;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_support_management);

        dbHelper = new DatabaseHelper(this);
        setupToolbar();
        ticketListContainer = findViewById(R.id.ticketListContainer);
        loadTickets();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.supportMgmtToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadTickets() {
        ticketListContainer.removeAllViews();
        Cursor cursor = dbHelper.getAllSupportTickets();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String user = cursor.getString(1);
                String title = cursor.getString(2);
                String category = cursor.getString(3);
                String desc = cursor.getString(4);
                String status = cursor.getString(5);
                String reply = cursor.getString(6);

                addTicketCard(id, user, title, category, desc, status, reply);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void addTicketCard(int id, String user, String title, String category, String desc, String status, String reply) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 24);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(Color.WHITE);
        card.setRadius(24f);
        card.setStrokeWidth(2);
        card.setStrokeColor(Color.parseColor("#E2E8F0"));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView tvUser = new TextView(this);
        tvUser.setText("From: " + user);
        tvUser.setTextColor(Color.parseColor("#64748B"));
        tvUser.setTextSize(12);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title + " [" + category + "]");
        tvTitle.setTextColor(Color.parseColor("#1E293B"));
        tvTitle.setTextSize(16);
        tvTitle.setPadding(0, 8, 0, 8);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextColor(Color.parseColor("#475569"));

        TextView tvStatus = new TextView(this);
        tvStatus.setText("Status: " + status);
        tvStatus.setTextColor(status.equals("Open") ? Color.RED : Color.parseColor("#059669"));
        tvStatus.setPadding(0, 16, 0, 0);
        tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);

        layout.addView(tvUser);
        layout.addView(tvTitle);
        layout.addView(tvDesc);
        if (reply != null && !reply.isEmpty()) {
            TextView tvReply = new TextView(this);
            tvReply.setText("Admin Reply: " + reply);
            tvReply.setTextColor(Color.BLUE);
            tvReply.setPadding(0, 8, 0, 0);
            layout.addView(tvReply);
        }
        layout.addView(tvStatus);

        card.addView(layout);
        card.setOnClickListener(v -> showReplyDialog(id, title, user));
        ticketListContainer.addView(card);
    }

    private void showReplyDialog(int id, String title, String user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reply to: " + user);
        final EditText input = new EditText(this);
        input.setHint("Type your response here...");
        builder.setView(input);

        builder.setPositiveButton("SEND REPLY", (dialog, which) -> {
            String replyText = input.getText().toString();
            if (!replyText.isEmpty()) {
                dbHelper.updateTicketReply(id, replyText);
                Toast.makeText(this, "Reply sent to " + user, Toast.LENGTH_SHORT).show();
                loadTickets();
            }
        });
        builder.setNegativeButton("CANCEL", null);
        builder.show();
    }
}
