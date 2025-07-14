package com.example.newsapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.newsapp.entity.NewsCache;
import java.util.List;

@Dao
public interface NewsCacheDao {

    @Query("SELECT * FROM news_cache WHERE category = :category ORDER BY cacheTime DESC")
    List<NewsCache> getNewsByCategory(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNews(List<NewsCache> newsList);

    @Query("DELETE FROM news_cache WHERE category = :category")
    void deleteNewsByCategory(String category);

    @Query("DELETE FROM news_cache")
    void deleteAllNews();

    @Query("SELECT COUNT(*) FROM news_cache WHERE category = :category")
    int getNewsCountByCategory(String category);

    @Query("SELECT * FROM news_cache WHERE category = :category ORDER BY cacheTime DESC LIMIT :limit")
    List<NewsCache> getNewsByCategoryWithLimit(String category, int limit);

    @Query("SELECT * FROM news_cache WHERE title LIKE '%' || :keyword || '%' ORDER BY cacheTime DESC")
    List<NewsCache> searchNewsByTitle(String keyword);
}