package com.example.newsapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.newsapp.entity.Comment;
import java.util.List;

@Dao
public interface CommentDao {

    @Insert
    void insert(Comment comment);

    @Delete
    void delete(Comment comment);

    // For User Posts
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp DESC")
    List<Comment> getCommentsByPostId(int postId);

    // For News Details
    @Query("SELECT * FROM comments WHERE newsId = :newsId AND parentCommentId IS NULL ORDER BY timestamp DESC")
    List<Comment> getTopLevelComments(String newsId);

    @Query("SELECT * FROM comments WHERE parentCommentId = :parentCommentId ORDER BY timestamp ASC")
    List<Comment> getReplies(int parentCommentId);
    
    @Query("SELECT * FROM comments WHERE id = :commentId")
    Comment getCommentById(Integer commentId);

    @Query("UPDATE comments SET userNickname = :newNickname, userAvatarPath = :newAvatarPath WHERE userId = :userId")
    void updateCommenterInfo(String userId, String newNickname, String newAvatarPath);
} 