package com.example.newsapp.entity.ai;

import java.util.List;

public class ChatRequest {
    public String model;
    public List<Message> messages;
    public boolean stream = false;

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public ChatRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }
} 