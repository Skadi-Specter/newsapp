package com.example.newsapp.network.tts;

import com.example.newsapp.entity.TtsRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface TtsApiService {
    String BASE_URL = "https://api.siliconflow.cn/";
    String API_KEY = 请填入密钥;

    @POST("v1/audio/speech")
    @Streaming
    Call<ResponseBody> getSpeech(@Header("Authorization") String authorization, @Body TtsRequest request);
}