package com.example.newsapp.network.tts;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TtsRetrofitClient {
    private static Retrofit instance = null;

    public static Retrofit getInstance() {
        if (instance == null) {
            instance = new Retrofit.Builder()
                    .baseUrl(TtsApiService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }
}