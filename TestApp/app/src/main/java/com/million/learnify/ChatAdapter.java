package com.million.learnify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.noties.markwon.Markwon;
import java.util.List;

/**
 * EDUAI REAL ASSISTANT ADAPTER
 * Perfectly handles structured student-friendly academic content.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    private final List<ChatMessage> chatMessages;
    private Markwon markwon;

    public ChatAdapter(List<ChatMessage> chatMessages, OnRegenerateListener listener) {
        this.chatMessages = chatMessages;
    }

    public interface OnRegenerateListener {
        void onRegenerate();
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).isUser() ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (markwon == null) {
            markwon = Markwon.create(parent.getContext());
        }
        
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new AIViewHolder(view, markwon);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof AIViewHolder) {
            ((AIViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
        }
    }

    static class AIViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        ImageView ivCopy;
        TextView tvHelpful, tvRegenerate;
        Markwon markwon;

        AIViewHolder(@NonNull View itemView, Markwon markwon) {
            super(itemView);
            this.markwon = markwon;
            container = itemView.findViewById(R.id.llBlocksContainer);
            ivCopy = itemView.findViewById(R.id.ivCopy);
            tvHelpful = itemView.findViewById(R.id.tvHelpful);
            tvRegenerate = itemView.findViewById(R.id.tvRegenerate);
        }

        void bind(ChatMessage message) {
            container.removeAllViews();
            Context context = itemView.getContext();
            String rawText = message.getMessage();

            if (message.isThinking()) {
                addStatusView(context, message.getMessage());
                ivCopy.setVisibility(View.GONE);
                tvHelpful.setVisibility(View.GONE);
                tvRegenerate.setVisibility(View.GONE);
            } else {
                ivCopy.setVisibility(View.VISIBLE);
                tvHelpful.setVisibility(View.VISIBLE);
                tvRegenerate.setVisibility(View.GONE);

                // ENHANCED STRUCTURE PARSING (Support for 📘, 🧠, 🧪, 📝)
                String[] blocks = rawText.split("\n\n");
                for (String block : blocks) {
                    if (block.trim().isEmpty()) continue;
                    
                    if (block.startsWith("📘") || block.startsWith("🧠") || 
                        block.startsWith("🧪") || block.startsWith("📝")) {
                        addHeaderView(context, block);
                    } else {
                        addSimpleText(context, block);
                    }
                }
            }

            ivCopy.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("EduAI Content", rawText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            });

            itemView.setAlpha(message.isThinking() ? 0.7f : 1.0f);
        }

        private void addHeaderView(Context context, String text) {
            TextView tv = new TextView(context);
            tv.setText(text);
            tv.setTextSize(17f);
            tv.setTextColor(Color.parseColor("#1E293B"));
            tv.setTypeface(null, Typeface.BOLD);
            tv.setPadding(0, 16, 0, 8);
            container.addView(tv);
        }

        private void addStatusView(Context context, String text) {
            TextView tv = new TextView(context);
            tv.setText(text);
            tv.setTextSize(14f);
            tv.setTextColor(Color.parseColor("#64748B"));
            tv.setPadding(0, 8, 0, 8);
            container.addView(tv);
        }

        private void addSimpleText(Context context, String text) {
            TextView tv = new TextView(context);
            tv.setPadding(0, 4, 0, 12);
            tv.setTextColor(Color.parseColor("#334155"));
            tv.setTextSize(16f);
            tv.setLineSpacing(6f, 1.1f);
            markwon.setMarkdown(tv, text);
            container.addView(tv);
        }
    }
}
