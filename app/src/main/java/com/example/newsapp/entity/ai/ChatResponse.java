package com.example.newsapp.entity.ai;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatResponse {
    public String id;
    public List<Choice> choices;
    public Usage usage;
    public String model;

    public static class Choice {
        public ResponseMessage message;
        @SerializedName("finish_reason")
        public String finishReason;
    }

    public static class ResponseMessage {
        public String role;
        public String content;
    }

    public static class Usage {
        @SerializedName("prompt_tokens")
        public int promptTokens;
        @SerializedName("completion_tokens")
        public int completionTokens;
        @SerializedName("total_tokens")
        public int totalTokens;
    }
} 