package com.example.newsapp.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.room.Ignore;

@Entity(tableName = "news_detail_cache")
public class NewsDetailCache {
    @PrimaryKey
    @NonNull
    public String uniquekey; // 新闻唯一标识
    public String title;
    public String author_name;
    public String date;
    public String category;
    public String content; // HTML内容
    public String thumbnail_pic_s;
    public String thumbnail_pic_s02;
    public String thumbnail_pic_s03;

    public NewsDetailCache() {}

    @Ignore
    public NewsDetailCache(@NonNull String uniquekey, String title, String author_name, String date, String category, String content, String thumbnail_pic_s, String thumbnail_pic_s02, String thumbnail_pic_s03) {
        this.uniquekey = uniquekey;
        this.title = title;
        this.author_name = author_name;
        this.date = date;
        this.category = category;
        this.content = content;
        this.thumbnail_pic_s = thumbnail_pic_s;
        this.thumbnail_pic_s02 = thumbnail_pic_s02;
        this.thumbnail_pic_s03 = thumbnail_pic_s03;
    }
} 