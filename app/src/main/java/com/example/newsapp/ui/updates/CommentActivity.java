package com.example.newsapp.ui.updates;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.Comment;
import com.example.newsapp.entity.User;
import com.example.newsapp.entity.UserPost;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "extra_post_id";

    private RecyclerView rvComments;
    private EditText etCommentInput;
    private Button btnSendComment;
    private CommentAdapter adapter;
    private AppDatabase db;
    private ExecutorService executorService;
    private int postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        postId = getIntent().getIntExtra(EXTRA_POST_ID, -1);
        if (postId == -1) {
            Toast.makeText(this, "无效的帖子ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        Toolbar toolbar = findViewById(R.id.toolbar_comment);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rvComments = findViewById(R.id.rv_comments);
        etCommentInput = findViewById(R.id.et_comment_input);
        btnSendComment = findViewById(R.id.btn_send_comment);

        setupRecyclerView();
        loadComments();

        btnSendComment.setOnClickListener(v -> postComment());
    }

    private void setupRecyclerView() {
        adapter = new CommentAdapter(new ArrayList<>());
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(adapter);
    }

    private void loadComments() {
        executorService.execute(() -> {
            List<Comment> comments = db.commentDao().getCommentsByPostId(postId);
            runOnUiThread(() -> adapter.updateData(comments));
        });
    }

    private void postComment() {
        String content = etCommentInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userPhone = prefs.getString("current_user_phone", null);

        if (userPhone == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            User currentUser = db.userDao().getUserByPhone(userPhone);
            if (currentUser == null) {
                runOnUiThread(() -> Toast.makeText(this, "无法获取用户信息", Toast.LENGTH_SHORT).show());
                return;
            }
            
            String nickname = (currentUser.nickname != null && !currentUser.nickname.isEmpty()) ? currentUser.nickname : "火星用户";

            Comment newComment = new Comment(postId, userPhone, nickname, currentUser.avatarPath, content);
            db.commentDao().insert(newComment);
            
            // Update post's comment count
            UserPost post = db.userPostDao().getPostById(postId); // Need to add this DAO method
            if (post != null) {
                post.commentCount++;
                db.userPostDao().updatePost(post);
            }

            runOnUiThread(() -> {
                adapter.addComment(newComment);
                rvComments.scrollToPosition(0);
                etCommentInput.setText("");
                Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 