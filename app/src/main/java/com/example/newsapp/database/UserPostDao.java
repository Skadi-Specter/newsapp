package com.example.newsapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.newsapp.entity.UserPost;
import java.util.List;
import androidx.room.Update;
import androidx.room.Delete;

@Dao
public interface UserPostDao {
    @Insert
    void insert(UserPost post);

    @Query("SELECT * FROM user_posts ORDER BY timestamp DESC")
    List<UserPost> getAllPosts();

    @Query("SELECT * FROM user_posts WHERE authorId = :userId ORDER BY timestamp DESC")
    List<UserPost> getPostsByUserId(int userId);

    @Query("SELECT * FROM user_posts WHERE authorId IN (:userIds) ORDER BY timestamp DESC")
    List<UserPost> getPostsByUserIds(List<Integer> userIds);

    @Query("SELECT * FROM user_posts WHERE id = :postId LIMIT 1")
    UserPost getPostById(int postId);

    @Update
    void updatePost(UserPost post);

    @Delete
    void delete(UserPost post);

    @Query("UPDATE user_posts SET authorNickname = :newNickname, authorAvatarPath = :newAvatarPath WHERE authorId = :authorId")
    void updateAuthorInfo(int authorId, String newNickname, String newAvatarPath);

    @Query("SELECT * FROM user_posts WHERE authorPhone = :userPhone ORDER BY timestamp DESC")
    List<UserPost> getPostsByUserPhone(String userPhone);

    @Query("UPDATE user_posts SET authorNickname = :newNickname, authorAvatarPath = :newAvatarPath WHERE authorPhone = :authorPhone")
    void updateAuthorInfoByPhone(String authorPhone, String newNickname, String newAvatarPath);
} 