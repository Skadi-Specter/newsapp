package com.example.newsapp.network.ai;

import com.example.newsapp.entity.ai.ChatRequest;
import com.example.newsapp.entity.ai.ChatResponse;
import com.example.newsapp.entity.ai.ImageGenerationRequest;
import com.example.newsapp.entity.ai.ImageGenerationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AiApiService {
    String BASE_URL = "https://api.siliconflow.cn/v1/";
    String API_KEY = 请填入密钥;

    @POST("chat/completions")
    Call<ChatResponse> getChatCompletion(
            @Header("Authorization") String authorization,
            @Body ChatRequest chatRequest
    );

    @POST("images/generations")
    Call<ImageGenerationResponse> generateImage(
            @Header("Authorization") String authorization,
            @Body ImageGenerationRequest imageGenerationRequest
    );
} 