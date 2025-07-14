package com.example.newsapp.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.R;
import com.example.newsapp.entity.ai.ChatMessage;
import com.example.newsapp.entity.ai.ChatRequest;
import com.example.newsapp.entity.ai.ChatResponse;
import com.example.newsapp.entity.ai.ImageGenerationRequest;
import com.example.newsapp.entity.ai.ImageGenerationResponse;
import com.example.newsapp.network.ai.AiApiService;
import com.example.newsapp.network.ai.AiRetrofitClient;
import com.example.newsapp.ui.chat.AiChatAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AiChatActivity extends AppCompatActivity {

    private List<ChatMessage> messages = new ArrayList<>();
    private AiChatAdapter adapter;
    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private AiApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_ai_chat);

        rvChat = findViewById(R.id.rv_chat_list);
        etMessage = findViewById(R.id.et_chat_input);
        btnSend = findViewById(R.id.btn_send_chat);

        apiService = AiRetrofitClient.getApiService();

        adapter = new AiChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Add user message to UI
        addToChat(messageText, true);
        etMessage.setText("");

        final String imageGenPrefix = "生成图片。";
        if (messageText.startsWith(imageGenPrefix)) {
            String prompt = messageText.substring(imageGenPrefix.length()).trim();
            if (prompt.isEmpty()) {
                addToChat("请输入对图片的描述。", false);
            } else {
                generateImage(prompt);
            }
        } else {
            // Existing text chat logic
            callChatApi();
        }
    }

    private void callChatApi() {
        // Add a thinking indicator
        addToChat("正在思考...", false);

        // Prepare messages for API - convert ChatMessage to Message
        List<ChatRequest.Message> apiMessages = new ArrayList<>();
        for (ChatMessage msg : messages) {
            if (msg.getMessageType() == ChatMessage.MessageType.TEXT) {
                String role = msg.isUser() ? "user" : "assistant";
                apiMessages.add(new ChatRequest.Message(role, msg.getMessage()));
            }
        }

        ChatRequest request = new ChatRequest("qwen-turbo", apiMessages);
        String authHeader = "Bearer " + AiApiService.API_KEY;

        apiService.getChatCompletion(authHeader, request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                // Remove thinking indicator
                messages.remove(messages.size() - 1);

                if (response.isSuccessful() && response.body() != null && 
                    response.body().choices != null && !response.body().choices.isEmpty()) {
                    String aiResponse = response.body().choices.get(0).message.content;
                    messages.add(new ChatMessage(aiResponse, false));
                } else {
                    String errorMsg = "聊天失败";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ": " + response.errorBody().string();
                        } catch (Exception e) { /* ignore */ }
                    }
                    messages.add(new ChatMessage(errorMsg, false));
                }
                adapter.notifyDataSetChanged();
                rvChat.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                // Remove thinking indicator and show error
                messages.remove(messages.size() - 1);
                Log.e("legion", "Chat API call failed", t);
                messages.add(new ChatMessage("网络错误，无法连接AI服务。", false));
                adapter.notifyDataSetChanged();
                rvChat.scrollToPosition(messages.size() - 1);
            }
        });
    }
    
    private void generateImage(String prompt) {
        // Add a thinking indicator
        addToChat("正在生成图片，请稍候...", false);

        ImageGenerationRequest request = new ImageGenerationRequest(prompt);
        String authHeader = "Bearer " + AiApiService.API_KEY;

        apiService.generateImage(authHeader, request).enqueue(new Callback<ImageGenerationResponse>() {
            @Override
            public void onResponse(Call<ImageGenerationResponse> call, Response<ImageGenerationResponse> response) {
                // Remove thinking indicator
                messages.remove(messages.size() - 1);

                if (response.isSuccessful() && response.body() != null && response.body().images != null && !response.body().images.isEmpty()) {
                    String imageUrl = response.body().images.get(0).url;
                    messages.add(new ChatMessage(imageUrl));
                } else {
                    String errorMsg = "图片生成失败";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += ": " + response.errorBody().string();
                        } catch (Exception e) { /* ignore */ }
                    }
                    messages.add(new ChatMessage(errorMsg, false));
                }
                adapter.notifyDataSetChanged();
                rvChat.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onFailure(Call<ImageGenerationResponse> call, Throwable t) {
                // Remove thinking indicator and show error
                messages.remove(messages.size() - 1);
                Log.e("legion", "Image Gen API call failed", t);
                messages.add(new ChatMessage("网络错误，无法生成图片。", false));
                adapter.notifyDataSetChanged();
                rvChat.scrollToPosition(messages.size() - 1);
            }
        });
    }

    private void addToChat(String message, boolean isUser) {
        messages.add(new ChatMessage(message, isUser));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }
} 