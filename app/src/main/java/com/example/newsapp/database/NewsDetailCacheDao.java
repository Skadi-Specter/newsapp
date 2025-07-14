package com.example.newsapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.newsapp.entity.NewsDetailCache;

@Dao
public interface NewsDetailCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(NewsDetailCache detailCache);

    @Query("SELECT * FROM news_detail_cache WHERE uniquekey = :uniquekey LIMIT 1")
    NewsDetailCache getDetailByUniquekey(String uniquekey);
} 