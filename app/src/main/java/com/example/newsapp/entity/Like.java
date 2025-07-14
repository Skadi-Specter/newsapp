package com.example.newsapp.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.annotation.NonNull;

@Entity(tableName = "likes",
        primaryKeys = {"postId", "userId"},
        foreignKeys = {
                @ForeignKey(entity = UserPost.class,
                        parentColumns = "id",
                        childColumns = "postId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "phone",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("postId"), @Index("userId")})
public class Like {
    public int postId;

    @NonNull
    public String userId;

    public Like(int postId, @NonNull String userId) {
        this.postId = postId;
        this.userId = userId;
    }
} 