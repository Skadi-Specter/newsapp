package com.example.newsapp;

import android.app.Application;
import android.util.Log;

import com.example.newsapp.login.UserSession;
import com.example.newsapp.network.NewsDataManager;
import com.example.newsapp.ui.main.NewsCategory;

public class NewsApplication extends Application {
    private static final String TAG = "NewsApplication";
    private static NewsApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        UserSession.getInstance(this);
        Log.d(TAG, "应用启动");
        // 启动时批量拉取所有类型新闻
        fetchAllNewsCategories();
    }

    private void fetchAllNewsCategories() {
        NewsDataManager dataManager = NewsDataManager.getInstance(this);
        for (String category : NewsCategory.CATEGORY_KEYS) {
            dataManager.getNewsData(category, 1, 20, null); // 不需要回调，静默拉取
        }
    }

    public static NewsApplication getInstance() {
        return instance;
    }
} 