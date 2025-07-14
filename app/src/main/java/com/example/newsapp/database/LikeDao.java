package com.example.newsapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.newsapp.entity.Like;
import java.util.List;

@Dao
public interface LikeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void like(Like like);

    @Delete
    void unlike(Like unlike);

    @Query("SELECT COUNT(*) FROM likes WHERE postId = :postId")
    int getLikeCount(int postId);

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE postId = :postId AND userId = :userId LIMIT 1)")
    boolean isLiked(int postId, String userId);

    @Query("SELECT postId FROM likes WHERE userId = :userId")
    List<Integer> getLikedPostIds(String userId);
} 