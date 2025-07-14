package com.example.newsapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.newsapp.entity.Follow;

import java.util.List;

@Dao
public interface FollowDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void follow(Follow follow);

    @Delete
    void unfollow(Follow follow);

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerId = :currentUserId AND followingId = :targetUserId LIMIT 1)")
    boolean isFollowing(String currentUserId, String targetUserId);

    @Query("SELECT * FROM follows WHERE followerId = :userId")
    List<Follow> getFollowingList(String userId);

    @Query("SELECT * FROM follows WHERE followingId = :userId")
    List<Follow> getFollowerList(String userId);
} 