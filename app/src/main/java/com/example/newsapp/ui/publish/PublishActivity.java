package com.example.newsapp.ui.publish;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.User;
import com.example.newsapp.entity.UserPost;
import com.example.newsapp.login.UserSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PublishActivity extends AppCompatActivity {

    private EditText contentEditText;
    private AppDatabase db;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        Toolbar toolbar = findViewById(R.id.toolbar_publish);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("发布动态");
        }

        contentEditText = findViewById(R.id.et_publish_content);
        db = AppDatabase.getInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_publish, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_publish) {
            showPublishDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPublishDialog() {
        String content = contentEditText.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("确认发布")
                .setMessage("确定要发布这篇动态吗？")
                .setPositiveButton("发布", (dialog, which) -> publishPost(content))
                .setNegativeButton("取消", null)
                .show();
    }

    private void publishPost(String content) {
        executorService.execute(() -> {
            SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String userPhone = prefs.getString("current_user_phone", null);

            if (userPhone == null) {
                runOnUiThread(() -> Toast.makeText(PublishActivity.this, "错误：用户未登录", Toast.LENGTH_SHORT).show());
                return;
            }
            
            User currentUser = db.userDao().getUserByPhone(userPhone);
            
            if (currentUser == null) {
                runOnUiThread(() -> Toast.makeText(PublishActivity.this, "错误：无法加载用户信息", Toast.LENGTH_SHORT).show());
                return;
            }
            
            String nickname = (currentUser.nickname != null && !currentUser.nickname.isEmpty()) ? currentUser.nickname : "火星用户";

            UserPost post = new UserPost("", content, nickname, userPhone.hashCode(), currentUser.avatarPath, System.currentTimeMillis());
            post.authorPhone = userPhone;
            post.authorNickname = nickname;
            post.authorAvatarPath = currentUser.avatarPath;
            post.likeCount = 0;
            db.userPostDao().insert(post);

            currentUser.postCount += 1;
            db.userDao().updateUser(currentUser);

            runOnUiThread(() -> {
                Toast.makeText(PublishActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 