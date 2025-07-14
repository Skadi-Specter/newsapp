package com.example.newsapp.network;

import android.content.Context;
import android.util.Log;

import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.NewsCache;
import com.example.newsapp.database.NewsCacheDao;
import com.example.newsapp.ui.main.News;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NewsCacheManager {
    private static final String TAG = "NewsCacheManager";
    private static NewsCacheManager instance;
    private NewsCacheDao newsCacheDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private NewsCacheManager(Context context) {
        newsCacheDao = AppDatabase.getInstance(context).newsCacheDao();
    }
    
    public static synchronized NewsCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new NewsCacheManager(context);
        }
        return instance;
    }
    
    /**
     * 异步保存新闻数据到本地缓存
     */
    public void saveNewsToCache(String category, List<News> newsList) {
        executor.execute(() -> {
            try {
                // 先删除该分类的旧缓存
                newsCacheDao.deleteNewsByCategory(category);
                
                // 转换为缓存实体并保存
                List<NewsCache> cacheList = new ArrayList<>();
                for (News news : newsList) {
                    NewsCache cache = new NewsCache(
                        category,
                        news.uniquekey,
                        news.title,
                        news.date,
                        news.author_name,
                        news.url,
                        news.thumbnail_pic_s,
                        news.thumbnail_pic_s02,
                        news.thumbnail_pic_s03,
                        news.is_content,
                        news.isAI
                    );
                    cacheList.add(cache);
                }
                
                newsCacheDao.insertNews(cacheList);
                Log.d(TAG, "成功缓存 " + cacheList.size() + " 条新闻数据，分类: " + category);
            } catch (Exception e) {
                Log.e(TAG, "保存新闻缓存失败", e);
            }
        });
    }
    
    /**
     * 异步从本地缓存获取新闻数据
     */
    public void getNewsFromCache(String category, Consumer<List<News>> callback) {
        executor.execute(() -> {
            try {
                List<NewsCache> cacheList = newsCacheDao.getNewsByCategory(category);
                List<News> newsList = new ArrayList<>();
                
                for (NewsCache cache : cacheList) {
                    News news = new News();
                    news.uniquekey = cache.uniquekey;
                    news.title = cache.title;
                    news.date = cache.date;
                    news.category = cache.category;
                    news.author_name = cache.author_name;
                    news.url = cache.url;
                    news.thumbnail_pic_s = cache.thumbnail_pic_s;
                    news.thumbnail_pic_s02 = cache.thumbnail_pic_s02;
                    news.thumbnail_pic_s03 = cache.thumbnail_pic_s03;
                    news.is_content = cache.is_content;
                    news.isAI = cache.isAI;
                    newsList.add(news);
                }
                
                Log.d(TAG, "从缓存获取到 " + newsList.size() + " 条新闻数据，分类: " + category);
                // 在主线程回调结果
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.accept(newsList));
            } catch (Exception e) {
                Log.e(TAG, "获取新闻缓存失败", e);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.accept(new ArrayList<>()));
            }
        });
    }
    
    /**
     * 异步检查指定分类是否有缓存数据
     */
    public void hasCachedNews(String category, Consumer<Boolean> callback) {
        executor.execute(() -> {
            try {
                int count = newsCacheDao.getNewsCountByCategory(category);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.accept(count > 0));
            } catch (Exception e) {
                Log.e(TAG, "检查缓存失败", e);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.accept(false));
            }
        });
    }
    
    /**
     * 异步清除所有新闻缓存
     */
    public void clearAllNewsCache() {
        executor.execute(() -> {
            try {
                newsCacheDao.deleteAllNews();
                Log.d(TAG, "已清除所有新闻缓存");
            } catch (Exception e) {
                Log.e(TAG, "清除新闻缓存失败", e);
            }
        });
    }
    
    /**
     * 异步清除指定分类的新闻缓存
     */
    public void clearNewsCacheByCategory(String category) {
        executor.execute(() -> {
            try {
                newsCacheDao.deleteNewsByCategory(category);
                Log.d(TAG, "已清除分类 " + category + " 的新闻缓存");
            } catch (Exception e) {
                Log.e(TAG, "清除分类缓存失败", e);
            }
        });
    }
    
    // 新增：异步根据标题关键字搜索新闻
    public void searchNewsByTitle(String keyword, java.util.function.Consumer<List<News>> callback) {
        executor.execute(() -> {
            try {
                List<com.example.newsapp.entity.NewsCache> cacheList = newsCacheDao.searchNewsByTitle(keyword);
                List<com.example.newsapp.ui.main.News> newsList = new ArrayList<>();
                for (com.example.newsapp.entity.NewsCache cache : cacheList) {
                    com.example.newsapp.ui.main.News news = new com.example.newsapp.ui.main.News();
                    news.uniquekey = cache.uniquekey;
                    news.title = cache.title;
                    news.date = cache.date;
                    news.category = cache.category;
                    news.author_name = cache.author_name;
                    news.url = cache.url;
                    news.thumbnail_pic_s = cache.thumbnail_pic_s;
                    news.thumbnail_pic_s02 = cache.thumbnail_pic_s02;
                    news.thumbnail_pic_s03 = cache.thumbnail_pic_s03;
                    news.is_content = cache.is_content;
                    news.isAI = cache.isAI;
                    newsList.add(news);
                }
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.accept(newsList));
            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.accept(new ArrayList<>()));
            }
        });
    }
} 