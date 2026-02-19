package com.example.testapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AnnouncementsActivity extends AppCompatActivity {

    private RecyclerView rvAnnouncements;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        dbHelper = new DatabaseHelper(this);
        setupToolbar();
        initViews();
        loadAnnouncements();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.announcementsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("System Announcements");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        rvAnnouncements = findViewById(R.id.rvAnnouncements);
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAnnouncements() {
        List<AnnouncementModel> list = dbHelper.getAnnouncements();
        rvAnnouncements.setAdapter(new AnnouncementAdapter(list));
    }

    class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {
        private List<AnnouncementModel> items;
        AnnouncementAdapter(List<AnnouncementModel> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AnnouncementModel item = items.get(position);
            holder.tvTitle.setText("📢 " + item.getTitle());
            holder.tvMsg.setText(item.getMessage());
            holder.tvDate.setText(item.getDate() + " • " + item.getType());
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMsg, tvDate;
            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvUserName);
                tvMsg = v.findViewById(R.id.tvUserRole);
                tvDate = v.findViewById(R.id.tvUserStatus);
            }
        }
    }
}
