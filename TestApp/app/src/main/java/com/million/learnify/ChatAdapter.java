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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import io.noties.markwon.Markwon;
import java.util.List;

/**
 * ChatAdapter - Refined Academic Pedagogical Chat Adapter.
 * Features:
 * - Markwon formatting for STEM notation and Markdown
 * - 4-part academic block styling with Neon Cyan headers
 * - Read-aloud speech button for Inclusive Accessibility
 * - One-tap clipboard copy action
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    private final List<ChatMessage> chatMessages;
    private final OnSpeechListener speechListener;
    private Markwon markwon;

    public interface OnSpeechListener {
        void onSpeak(String text);
    }

    public ChatAdapter(List<ChatMessage> chatMessages, @Nullable OnSpeechListener speechListener) {
        this.chatMessages = chatMessages;
        this.speechListener = speechListener;
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
            return new AIViewHolder(view, markwon, speechListener);
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
        ImageView ivSpeak;
        TextView tvHelpful;
        TextView tvRegenerate;
        Markwon markwon;
        OnSpeechListener speechListener;

        AIViewHolder(@NonNull View itemView, Markwon markwon, @Nullable OnSpeechListener speechListener) {
            super(itemView);
            this.markwon = markwon;
            this.speechListener = speechListener;
            container = itemView.findViewById(R.id.llBlocksContainer);
            ivCopy = itemView.findViewById(R.id.ivCopy);
            ivSpeak = itemView.findViewById(R.id.ivSpeak);
            tvHelpful = itemView.findViewById(R.id.tvHelpful);
            tvRegenerate = itemView.findViewById(R.id.tvRegenerate);
        }

        void bind(ChatMessage message) {
            container.removeAllViews();
            Context context = itemView.getContext();
            String rawText = AIResponseNormalizer.normalize(message.getMessage());

            if (message.isThinking()) {
                addStatusView(context, rawText);
                ivCopy.setVisibility(View.GONE);
                if (ivSpeak != null) ivSpeak.setVisibility(View.GONE);
                tvHelpful.setVisibility(View.GONE);
                tvRegenerate.setVisibility(View.GONE);
            } else {
                ivCopy.setVisibility(View.VISIBLE);
                if (ivSpeak != null) ivSpeak.setVisibility(View.VISIBLE);
                tvHelpful.setVisibility(View.VISIBLE);
                tvRegenerate.setVisibility(View.VISIBLE);

                // Structural academic parsing (Topic Overview, Key Points, Simple Example, Quick Summary)
                String[] blocks = rawText.split("\n\n");
                for (String block : blocks) {
                    if (block.trim().isEmpty()) continue;

                    if (isAcademicHeader(block)) {
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
                Toast.makeText(context, "Explanation copied to clipboard", Toast.LENGTH_SHORT).show();
            });

            if (ivSpeak != null) {
                ivSpeak.setOnClickListener(v -> {
                    if (speechListener != null) {
                        speechListener.onSpeak(rawText);
                    }
                });
            }

            itemView.setAlpha(message.isThinking() ? 0.7f : 1.0f);
        }

        private boolean isAcademicHeader(String block) {
            String trimmed = block.trim();
            return trimmed.startsWith("##")
                    || trimmed.startsWith("Topic Overview")
                    || trimmed.startsWith("Key Points")
                    || trimmed.startsWith("Simple Example")
                    || trimmed.startsWith("Quick Summary")
                    || trimmed.startsWith("Concept Overview")
                    || trimmed.startsWith("Practice Problem");
        }

        private void addHeaderView(Context context, String text) {
            TextView tv = new TextView(context);
            String cleanText = text.replaceFirst("^##\\s*", "").trim();
            cleanText = AIResponseNormalizer.stripDecorativeEmojis(cleanText);
            tv.setText(cleanText);
            tv.setTextSize(16f);
            tv.setTextColor(Color.parseColor("#00D8FF")); // Neon Cyan
            tv.setTypeface(null, Typeface.BOLD);
            tv.setPadding(0, 14, 0, 6);
            container.addView(tv);
        }

        private void addStatusView(Context context, String text) {
            TextView tv = new TextView(context);
            tv.setText(text);
            tv.setTextSize(14f);
            tv.setTextColor(Color.parseColor("#63E7B7")); // Science Mint
            tv.setPadding(0, 8, 0, 8);
            container.addView(tv);
        }

        private void addSimpleText(Context context, String text) {
            TextView tv = new TextView(context);
            tv.setPadding(0, 4, 0, 10);
            tv.setTextColor(Color.parseColor("#E2E8F0")); // Ice Slate
            tv.setTextSize(15f);
            tv.setLineSpacing(5f, 1.15f);
            markwon.setMarkdown(tv, text);
            container.addView(tv);
        }
    }
}
