package com.example.newsapp.network;

import com.example.newsapp.ui.detail.NewsContentResponse;
import com.example.newsapp.ui.main.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApi {
    // 新闻API密钥
    String NEWS_API_KEY = 请填入密钥;

    @GET("toutiao/index")
    Call<NewsResponse> getNewsList(
            @Query("key") String key,
            @Query("type") String type,
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("is_filter") int isFilter
    );

    @GET("toutiao/content")
    Call<NewsContentResponse> getNewsContent(
            @Query("key") String key,
            @Query("uniquekey") String uniquekey
    );

}