package com.example.newsapp.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.ui.main.News;
import com.example.newsapp.ui.main.NewsResponse;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsDataManager {
    private static final String TAG = "NewsDataManager";
    private static NewsDataManager instance;
    private NewsCacheManager cacheManager;
    private NewsApi newsApi;
    private AppDatabase db;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());


    private NewsDataManager(Context context) {
        cacheManager = NewsCacheManager.getInstance(context);
        newsApi = RetrofitClient.getInstance().create(NewsApi.class);
        db = AppDatabase.getInstance(context);
    }
    
    public static synchronized NewsDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new NewsDataManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 获取新闻数据，优先从缓存获取，本地有缓存时不再访问API
     * 启动时批量调用即可，UI层只需从缓存读取
     */
    public void getNewsData(String category, int page, int pageSize, NewsDataCallback callback) {
        cacheManager.hasCachedNews(category, hasCache -> {
            if (hasCache) {
                Log.d(TAG, "缓存存在，从缓存获取新闻数据，分类: " + category);
                // 只从本地缓存获取数据，不再访问API
                cacheManager.getNewsFromCache(category, cachedNews -> {
                    if (callback != null) {
                        callback.onSuccess(cachedNews);
                    }
                });
            } else {
                // 缓存不存在，从网络获取
                Log.d(TAG, "缓存不存在，从网络获取新闻数据，分类: " + category);
                loadNewsFromNetwork(category, page, pageSize, callback);
            }
        });
    }
    
    /**
     * 强制从网络刷新新闻数据
     */
    public void refreshNewsData(String category, int page, int pageSize, NewsDataCallback callback) {
        Log.d(TAG, "强制从网络刷新新闻数据，分类: " + category);
        loadNewsFromNetwork(category, page, pageSize, callback);
    }
    
    /**
     * 静默从网络加载新闻数据（不通知UI）
     */
    private void loadNewsFromNetworkSilently(String category, int page, int pageSize) {
        newsApi.getNewsList(
                NewsApi.NEWS_API_KEY,
                category,
                page,
                pageSize,
                0
        ).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().error_code == 0) {
                    List<News> newsList = response.body().result.data;
                    
                    // 静默保存到缓存
                    cacheManager.saveNewsToCache(category, newsList);
                    
                    Log.d(TAG, "静默更新成功，缓存 " + newsList.size() + " 条新闻，分类: " + category);
                } else {
                    Log.e(TAG, "静默网络请求失败: " + (response.body() != null ? response.body().error_code : response.code()));
                }
            }
            
            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Log.e(TAG, "静默网络请求异常: " + t.getMessage());
            }
        });
    }
    
    /**
     * 从网络加载新闻数据
     */
    private void loadNewsFromNetwork(String category, int page, int pageSize, final NewsDataCallback callback) {
        newsApi.getNewsList(
                NewsApi.NEWS_API_KEY,
                category,
                page,
                pageSize,
                0
        ).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().error_code == 0) {
                    List<News> newsList = response.body().result.data;
                    
                    // 保存到缓存
                    cacheManager.saveNewsToCache(category, newsList);
                    
                    Log.d(TAG, "成功从网络获取并缓存 " + newsList.size() + " 条新闻，分类: " + category);
                    if (callback != null) callback.onSuccess(newsList);
                } else {
                    Log.e(TAG, "网络请求失败: " + (response.body() != null ? response.body().error_code : response.code()));
                    if (callback != null) callback.onError("获取新闻失败");
                }
            }
            
            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Log.e(TAG, "网络请求异常: " + t.getMessage());
                if (callback != null) callback.onError("网络连接失败");
            }
        });
    }
    
    /**
     * 清除指定分类的缓存
     */
    public void clearCacheByCategory(String category) {
        cacheManager.clearNewsCacheByCategory(category);
    }
    
    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        cacheManager.clearAllNewsCache();
    }
    
    /**
     * 检查是否有缓存数据
     */
    @Deprecated
    public boolean hasCachedData(String category) {
        // 这是一个同步方法，不应该再被使用
        return false;
    }
    
    /**
     * 公开方法：异步从本地缓存获取新闻数据
     */
    public void getNewsFromCache(String category, Consumer<List<News>> callback) {
        Log.d("legion", "NewsDataManager.getNewsFromCache 调用, category=" + category);
        cacheManager.getNewsFromCache(category, newsList -> {
            Log.d("legion", "NewsDataManager.getNewsFromCache 回调, category=" + category + ", newsList.size=" + (newsList == null ? -1 : newsList.size()));
            if (callback != null) callback.accept(newsList);
        });
    }
    
    /**
     * 新增：对外提供新闻标题搜索
     */
    public void searchNewsByTitle(String keyword, java.util.function.Consumer<List<com.example.newsapp.ui.main.News>> callback) {
        cacheManager.searchNewsByTitle(keyword, callback);
    }
    
    /**
     * 新闻数据回调接口
     */
    public interface NewsDataCallback {
        void onSuccess(List<News> newsList);
        void onError(String errorMessage);
    }
} 