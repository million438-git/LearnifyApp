package com.million.learnify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * AnnouncementsActivity - Displays system-wide broadcasts and alerts to students.
 * Professional implementation with specialized item layout.
 */
public class AnnouncementsActivity extends AppCompatActivity {

    private RecyclerView rvAnnouncements;
    private DatabaseHelper dbHelper;
    private View emptyState;

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
            getSupportActionBar().setTitle("Bulletin Board");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        rvAnnouncements = findViewById(R.id.rvAnnouncements);
        emptyState = findViewById(R.id.llEmptyState); // Assuming this exists in the layout or adding it
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAnnouncements() {
        List<AnnouncementModel> list = dbHelper.getAnnouncements();
        if (list.isEmpty()) {
            if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
            rvAnnouncements.setVisibility(View.GONE);
        } else {
            if (emptyState != null) emptyState.setVisibility(View.GONE);
            rvAnnouncements.setVisibility(View.VISIBLE);
            rvAnnouncements.setAdapter(new AnnouncementAdapter(list));
        }
    }

    class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {
        private List<AnnouncementModel> items;

        AnnouncementAdapter(List<AnnouncementModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announcement, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AnnouncementModel item = items.get(position);
            holder.tvTitle.setText(item.getTitle());
            holder.tvMsg.setText(item.getMessage());
            holder.tvDate.setText(item.getDate());
            holder.tvType.setText(item.getType());

            // Professional Visual Cues based on Type
            if ("COURSE_UPDATE".equals(item.getType())) {
                holder.ivIcon.setImageResource(android.R.drawable.ic_menu_agenda);
                holder.tvType.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            } else if ("SYSTEM_ALERT".equals(item.getType())) {
                holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_alert);
                holder.tvType.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_info);
                holder.tvType.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvMsg, tvDate, tvType;
            ImageView ivIcon;

            ViewHolder(View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvAnnouncementTitle);
                tvMsg = v.findViewById(R.id.tvAnnouncementMessage);
                tvDate = v.findViewById(R.id.tvAnnouncementDate);
                tvType = v.findViewById(R.id.tvAnnouncementType);
                ivIcon = v.findViewById(R.id.ivAnnouncementIcon);
            }
        }
    }
}
