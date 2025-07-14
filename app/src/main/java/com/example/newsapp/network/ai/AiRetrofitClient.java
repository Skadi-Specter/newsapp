package com.example.newsapp.network.ai;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AiRetrofitClient {
    private static Retrofit instance = null;
    private static AiApiService apiService = null;

    private static Retrofit getInstance() {
        if (instance == null) {
            // Create a logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configure a custom OkHttpClient with longer timeouts and the logger
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor) // Add the logger
                    .build();

            instance = new Retrofit.Builder()
                    .baseUrl(AiApiService.BASE_URL)
                    .client(okHttpClient) // Use the custom client
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return instance;
    }

    public static AiApiService getApiService() {
        if (apiService == null) {
            apiService = getInstance().create(AiApiService.class);
        }
        return apiService;
    }
} 