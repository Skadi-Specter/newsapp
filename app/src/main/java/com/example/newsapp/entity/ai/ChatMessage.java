package com.example.newsapp.entity.ai;

public class ChatMessage {

    public enum MessageType {
        TEXT, IMAGE, THINKING
    }

    private String message;
    private boolean isUser;
    private MessageType messageType;
    private String imageBase64; // To store base64 string for images

    // Constructor for text messages
    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.messageType = MessageType.TEXT;
    }

    // Constructor for image messages (from AI)
    public ChatMessage(String imageBase64) {
        this.message = "[图片]"; // Placeholder text
        this.isUser = false;
        this.messageType = MessageType.IMAGE;
        this.imageBase64 = imageBase64;
    }

    // Constructor for a thinking indicator
    public ChatMessage() {
        this.message = ""; // No text needed
        this.isUser = false;
        this.messageType = MessageType.THINKING;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getImageBase64() {
        return imageBase64;
    }
} 