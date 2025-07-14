package com.example.newsapp.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_posts")
public class UserPost {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String content;
    public String authorNickname;
    public int authorId;
    public String authorAvatarPath;
    public long timestamp;
    public int likeCount;
    public int commentCount;
    public String authorPhone;

    // Room需要一个无参构造函数
    public UserPost() {}

    @androidx.room.Ignore
    public UserPost(String title, String content, String authorNickname, int authorId, String authorAvatarPath, long timestamp) {
        this.title = title;
        this.content = content;
        this.authorNickname = authorNickname;
        this.authorId = authorId;
        this.authorAvatarPath = authorAvatarPath;
        this.timestamp = timestamp;
        this.likeCount = 0;
        this.commentCount = 0;
    }
} 