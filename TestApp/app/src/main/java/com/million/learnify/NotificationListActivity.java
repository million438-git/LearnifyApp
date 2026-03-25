package com.million.learnify;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class NotificationListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private RecyclerView rvNotifications;
    private TextView tvEmptyState;
    private String currentUsername;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        dbHelper = new DatabaseHelper(this);
        currentUsername = getSharedPreferences("UserSession", MODE_PRIVATE).getString("username", "");

        setupToolbar();
        initViews();
        loadNotifications();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notifications");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));

        MaterialButton btnMarkRead = findViewById(R.id.btnMarkRead);
        MaterialButton btnClearAll = findViewById(R.id.btnClearAll);

        btnMarkRead.setOnClickListener(v -> markAllAsRead());
        btnClearAll.setOnClickListener(v -> showClearAllConfirmation());
    }

    private void loadNotifications() {
        notificationList.clear();
        Cursor cursor = dbHelper.getUserNotifications(currentUsername);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                NotificationModel n = new NotificationModel();
                n.id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                n.title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                n.message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                n.timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
                n.isRead = cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1;
                notificationList.add(n);
            }
            cursor.close();
        }

        updateUI();
    }

    private void updateUI() {
        if (notificationList.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            findViewById(R.id.actionBarActions).setVisibility(View.GONE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            findViewById(R.id.actionBarActions).setVisibility(View.VISIBLE);
            
            if (adapter == null) {
                adapter = new NotificationAdapter(notificationList);
                rvNotifications.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void markAllAsRead() {
        dbHelper.markNotificationsAsRead(currentUsername);
        for (NotificationModel n : notificationList) {
            n.isRead = true;
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        Toast.makeText(this, "All marked as read", Toast.LENGTH_SHORT).show();
    }

    private void showClearAllConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear all notifications?")
                .setMessage("This action cannot be undone. Are you sure?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    dbHelper.clearAllNotifications(currentUsername);
                    notificationList.clear();
                    updateUI();
                    Toast.makeText(this, "Notifications cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSingleNotification(int position) {
        NotificationModel item = notificationList.get(position);
        dbHelper.deleteNotification(item.id);
        notificationList.remove(position);
        adapter.notifyItemRemoved(position);
        
        if (notificationList.isEmpty()) {
            updateUI();
        }

        Snackbar.make(rvNotifications, "Notification deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", v -> {
                    // In a real app, you'd re-insert into DB. For now, just reload to be safe or re-add to list.
                    dbHelper.addNotification(currentUsername, item.title, item.message, "General");
                    loadNotifications();
                }).show();
    }

    private static class NotificationModel {
        int id;
        String title, message, timestamp;
        boolean isRead;
    }

    private class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private List<NotificationModel> list;

        NotificationAdapter(List<NotificationModel> list) { this.list = list; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotificationModel item = list.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvMessage.setText(item.message);
            holder.tvTimestamp.setText(item.timestamp);
            
            // Visual state for read/unread
            if (item.isRead) {
                holder.tvTitle.setTextColor(0xFF64748B);
                holder.itemView.setAlpha(0.7f);
            } else {
                holder.tvTitle.setTextColor(0xFF1E293B);
                holder.itemView.setAlpha(1.0f);
            }

            holder.btnDelete.setOnClickListener(v -> deleteSingleNotification(position));
            
            holder.itemView.setOnClickListener(v -> {
                if (!item.isRead) {
                    // Logic to mark this one as read if clicked
                    // dbHelper.markNotificationAsRead(item.id); // Would need this method in DB
                    item.isRead = true;
                    notifyItemChanged(position);
                }
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMessage, tvTimestamp;
            ImageButton btnDelete;

            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvTitle);
                tvMessage = v.findViewById(R.id.tvMessage);
                tvTimestamp = v.findViewById(R.id.tvTimestamp);
                btnDelete = v.findViewById(R.id.btnDelete);
            }
        }
    }
}
