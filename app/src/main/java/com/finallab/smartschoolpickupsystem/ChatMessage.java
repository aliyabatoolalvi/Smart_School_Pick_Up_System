package com.finallab.smartschoolpickupsystem;

public class ChatMessage {
    private String message;
    private boolean isBot; // true = bot message, false = user message

    public ChatMessage(String message, boolean isBot) {
        this.message = message;
        this.isBot = isBot;
    }

    public String getMessage() {
        return message;
    }

    public boolean isBot() {
        return isBot;
    }
}
