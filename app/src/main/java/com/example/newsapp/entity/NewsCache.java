package com.example.newsapp.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "news_cache")
public class NewsCache {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String category;  // 新闻类别
    public String uniquekey; // 新闻唯一标识
    public String title;     // 新闻标题
    public String date;      // 发布日期
    public String author_name; // 作者
    public String url;       // 新闻链接
    public String thumbnail_pic_s; // 缩略图1
    public String thumbnail_pic_s02; // 缩略图2
    public String thumbnail_pic_s03; // 缩略图3
    public String is_content; // 是否有内容
    public boolean isAI;     // 是否为AI新闻
    public long cacheTime;   // 缓存时间戳
    
    @Ignore
    public NewsCache() {}
    
    public NewsCache(String category, String uniquekey, String title, String date, 
                    String author_name, String url, String thumbnail_pic_s, 
                    String thumbnail_pic_s02, String thumbnail_pic_s03, 
                    String is_content, boolean isAI) {
        this.category = category;
        this.uniquekey = uniquekey;
        this.title = title;
        this.date = date;
        this.author_name = author_name;
        this.url = url;
        this.thumbnail_pic_s = thumbnail_pic_s;
        this.thumbnail_pic_s02 = thumbnail_pic_s02;
        this.thumbnail_pic_s03 = thumbnail_pic_s03;
        this.is_content = is_content;
        this.isAI = isAI;
        this.cacheTime = System.currentTimeMillis();
    }
} 