package com.example.newsapp.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(tableName = "follows",
        primaryKeys = {"followerId", "followingId"},
        indices = {@Index("followerId"), @Index("followingId")})
public class Follow {
    @NonNull
    public String followerId; // 关注者的用户ID

    @NonNull
    public String followingId; // 被关注者的用户ID

    public Follow(@NonNull String followerId, @NonNull String followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }
} 