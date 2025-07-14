package com.example.newsapp.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class AiChatFragment extends Fragment {

    private RecyclerView rvChatList;
    private EditText etChatInput;
    private ImageButton btnSendChat;
    private AiChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private AiApiService aiApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_chat, container, false);

        initView(view);
        initRecyclerView();
        initAiService();
        setupSendButton();
        
        if (messageList.isEmpty()) {
            addInitialMessage();
        }

        return view;
    }

    private void initView(View view) {
        rvChatList = view.findViewById(R.id.rv_chat_list);
        etChatInput = view.findViewById(R.id.et_chat_input);
        btnSendChat = view.findViewById(R.id.btn_send_chat);
    }

    private void initRecyclerView() {
        chatAdapter = new AiChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvChatList.setLayoutManager(layoutManager);
        rvChatList.setAdapter(chatAdapter);
    }
    
    private void initAiService() {
        aiApiService = AiRetrofitClient.getApiService();
    }
    
    private void addInitialMessage() {
        messageList.add(new ChatMessage("你好！我是你的AI助手，有什么可以帮你的吗？", false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
    }

    private void setupSendButton() {
        btnSendChat.setOnClickListener(v -> {
            String inputText = etChatInput.getText().toString().trim();
            if (!TextUtils.isEmpty(inputText)) {
                etChatInput.setText("");
                sendMessage(inputText);
            }
        });
    }

    private void sendMessage(String text) {
        // Add user message to UI first
        messageList.add(new ChatMessage(text, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatList.scrollToPosition(messageList.size() - 1);

        final String imageGenPrefix = "生成图片。";
        if (text.startsWith(imageGenPrefix)) {
            String prompt = text.substring(imageGenPrefix.length()).trim();
            if (prompt.isEmpty()) {
                addAiResponse("请输入对图片的描述。");
            } else {
                generateImage(prompt);
            }
        } else {
            callChatApi();
        }
    }
    
    private void addAiResponse(String text) {
        ChatMessage assistantMessage = new ChatMessage(text, false);
        messageList.add(assistantMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatList.scrollToPosition(messageList.size() - 1);
    }
    
    private void callChatApi() {
        ChatMessage thinkingMessage = new ChatMessage(); // "Thinking..."
        messageList.add(thinkingMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatList.scrollToPosition(messageList.size() - 1);

        List<ChatRequest.Message> messagesToSend = new ArrayList<>();
        for (int i = 0; i < messageList.size() - 1; i++) {
            ChatMessage msg = messageList.get(i);
            if (msg.getMessageType() == ChatMessage.MessageType.TEXT) {
                if (i == 0 && !msg.isUser()) {
                    continue;
                }
                String role = msg.isUser() ? "user" : "assistant";
                messagesToSend.add(new ChatRequest.Message(role, msg.getMessage()));
            }
        }

        ChatRequest request = new ChatRequest("deepseek-ai/DeepSeek-V3", messagesToSend);
        String authHeader = "Bearer " + AiApiService.API_KEY;

        aiApiService.getChatCompletion(authHeader, request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                messageList.remove(messageList.size() - 1); // Remove "thinking"
                chatAdapter.notifyItemRemoved(messageList.size());

                if (response.isSuccessful() && response.body() != null && !response.body().choices.isEmpty()) {
                    String aiResponseText = response.body().choices.get(0).message.content;
                    addAiResponse(aiResponseText);
                } else {
                    String errorBody = "无法获取回复，请稍后再试。";
                    try {
                        if (response.errorBody() != null) {
                           errorBody += " (code: " + response.code() + ")";
                        }
                    } catch (Exception e) { /* ignored */ }
                    addAiResponse(errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                messageList.remove(messageList.size() - 1); // Remove "thinking"
                chatAdapter.notifyItemRemoved(messageList.size());
                Log.e("legion", "AI Chat API call failed", t);
                addAiResponse("网络错误: " + t.getMessage());
            }
        });
    }

    private void generateImage(String prompt) {
        ChatMessage thinkingMessage = new ChatMessage(); // "Thinking..."
        messageList.add(thinkingMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChatList.scrollToPosition(messageList.size() - 1);

        ImageGenerationRequest request = new ImageGenerationRequest(prompt);
        String authHeader = "Bearer " + AiApiService.API_KEY;

        aiApiService.generateImage(authHeader, request).enqueue(new Callback<ImageGenerationResponse>() {
            @Override
            public void onResponse(Call<ImageGenerationResponse> call, Response<ImageGenerationResponse> response) {
                messageList.remove(messageList.size() - 1); // Remove "thinking"
                chatAdapter.notifyItemRemoved(messageList.size());

                // 添加详细的响应日志
                Log.d("legion", "Response code: " + response.code());
                Log.d("legion", "Response successful: " + response.isSuccessful());
                Log.d("legion", "Response body is null: " + (response.body() == null));
                Log.d("legion", "Response body images is null: " + (response.body().images == null));
                if (response.body().images != null) {
                    Log.d("legion", "Response body images size: " + response.body().images.size());
                    if (!response.body().images.isEmpty()) {
                        Log.d("legion", "First data item url is null: " + (response.body().images.get(0).url == null));
                        if (response.body().images.get(0).url != null) {
                            Log.d("legion", "Image url: " + response.body().images.get(0).url);
                        }
                    }
                }

                if (response.isSuccessful() && response.body() != null && response.body().images != null && !response.body().images.isEmpty()) {
                    String imageUrl = response.body().images.get(0).url;
                    Log.d("legion", "Image URL from API: " + imageUrl);
                    messageList.add(new ChatMessage(imageUrl));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    rvChatList.scrollToPosition(messageList.size() - 1);
                } else {
                    String errorMsg = "图片生成失败";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e("legion", "Error body: " + errorBodyString);
                            errorMsg += ": " + errorBodyString;
                        } catch (Exception e) { 
                            Log.e("legion", "Error reading error body", e);
                        }
                    }
                    addAiResponse(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ImageGenerationResponse> call, Throwable t) {
                messageList.remove(messageList.size() - 1); // Remove "thinking"
                chatAdapter.notifyItemRemoved(messageList.size());
                Log.e("legion", "Image Gen API call failed", t);
                addAiResponse("网络错误，无法生成图片。");
            }
        });
    }
} 