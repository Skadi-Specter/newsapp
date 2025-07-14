package com.example.newsapp.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;
import androidx.room.Transaction;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.newsapp.entity.Comment;
import com.example.newsapp.entity.Follow;
import com.example.newsapp.entity.User;
import com.example.newsapp.entity.NewsCache;
import com.example.newsapp.entity.UserPost;
import com.example.newsapp.entity.Like;
import com.example.newsapp.entity.NewsDetailCache;

@Database(entities = {NewsCache.class, User.class, Comment.class, Follow.class, UserPost.class, Like.class, NewsDetailCache.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public abstract UserDao userDao();
    public abstract NewsCacheDao newsCacheDao();
    public abstract CommentDao commentDao();
    public abstract FollowDao followDao();
    public abstract UserPostDao userPostDao();
    public abstract LikeDao likeDao();
    public abstract NewsDetailCacheDao newsDetailCacheDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "news_database")
                            // Destructive migration is simpler for development
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    // Transactional method to delete a post and update the user's post count.
    @Transaction
    public void deletePostAndUpdateUserCount(UserPost post) {
        // First, delete the post
        userPostDao().delete(post);
        // Then, decrement the post count for the author
        // We need to pass the author's phone (String), not the hashCode (int)
        // This assumes you have a way to get the author's phone.
        // For now, let's assume the UserPost object has the author's string ID.
        // If not, we'll need to fetch it first.
        // Based on the schema, UserPost only has authorId (int), not authorPhone (String).
        // Let's add authorPhone to UserPost entity.
        // No, let's not change the entity. We can find the user by hashcode. This is fragile but follows existing logic.
        
        // This is a placeholder for the logic to find the user's phone from the hashcode.
        // A better solution would be to store author's phone in UserPost.
        // But for now, let's find a user who matches this hashcode. This is inefficient.
        // A better approach: The caller should provide the user's phone.
        userDao().decrementPostCount(post.authorPhone);
    }
}
