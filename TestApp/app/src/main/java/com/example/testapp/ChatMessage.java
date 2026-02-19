package com.example.testapp;

/**
 * Professional data model for chat messages.
 */
public class ChatMessage {
    private final String message;
    private final boolean isUser;
    private final boolean isThinking;
    private final boolean isError;
    private final boolean canRegenerate;

    public ChatMessage(String message, boolean isUser) {
        this(message, isUser, false, false, false);
    }

    public ChatMessage(String message, boolean isUser, boolean isThinking) {
        this(message, isUser, isThinking, false, false);
    }

    public ChatMessage(String message, boolean isUser, boolean isThinking, boolean isError, boolean canRegenerate) {
        this.message = message;
        this.isUser = isUser;
        this.isThinking = isThinking;
        this.isError = isError;
        this.canRegenerate = canRegenerate;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public boolean isThinking() {
        return isThinking;
    }

    public boolean isError() {
        return isError;
    }

    public boolean canRegenerate() {
        return canRegenerate;
    }
}
