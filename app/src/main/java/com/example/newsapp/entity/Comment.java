package com.example.newsapp.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments",
        foreignKeys = @ForeignKey(entity = UserPost.class,
                parentColumns = "id",
                childColumns = "postId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("postId"), @Index("newsId"), @Index("parentCommentId")})
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public int id;

    // A comment is either for a post or for a news item
    @Nullable
    public Integer postId; // For UserPost
    @Nullable
    public String newsId;  // For News

    @Nullable
    public Integer parentCommentId; // For nested replies

    @NonNull
    public String userId; // 发表评论的用户ID (手机号)

    @NonNull
    public String userNickname; // 发表评论的用户名

    public String userAvatarPath; // 用户头像路径

    @NonNull
    public String content; // 评论内容

    public long timestamp; // 评论时间戳

    // 为了Room，需要一个无参构造函数
    public Comment() {}

    // Constructor for creating a NEW post comment
    @androidx.room.Ignore
    public Comment(int postId, @NonNull String userId, @NonNull String userNickname, String userAvatarPath, @NonNull String content) {
        this.postId = postId;
        this.userId = userId;
        this.userNickname = userNickname;
        this.userAvatarPath = userAvatarPath;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Constructor for creating a NEW news comment, to fix compilation errors
    @androidx.room.Ignore
    public Comment(@NonNull String newsId, @NonNull String userId, @Nullable Integer parentCommentId, @NonNull String content, String userNickname, String userAvatarPath) {
        this.newsId = newsId;
        this.userId = userId;
        this.parentCommentId = parentCommentId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.userNickname = userNickname;
        this.userAvatarPath = userAvatarPath;
    }
} 