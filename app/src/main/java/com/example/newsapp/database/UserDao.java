package com.example.newsapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.newsapp.entity.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Query("SELECT * FROM User WHERE phone = :phone LIMIT 1")
    User getUserByPhone(String phone);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("DELETE FROM User WHERE phone = :phone")
    void deleteUserByPhone(String phone);

    @Query("UPDATE User SET postCount = postCount - 1 WHERE phone = :userId")
    void decrementPostCount(String userId);

    @Query("UPDATE User SET followCount = followCount + 1 WHERE phone = :userId")
    void incrementFollowCount(String userId);

    @Query("UPDATE User SET followCount = CASE WHEN followCount > 0 THEN followCount - 1 ELSE 0 END WHERE phone = :userId")
    void decrementFollowCount(String userId);

    @Query("UPDATE User SET fansCount = fansCount + 1 WHERE phone = :userId")
    void incrementFansCount(String userId);

    @Query("UPDATE User SET fansCount = CASE WHEN fansCount > 0 THEN fansCount - 1 ELSE 0 END WHERE phone = :userId")
    void decrementFansCount(String userId);
} 