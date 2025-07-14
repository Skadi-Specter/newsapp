package com.example.newsapp.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.newsapp.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView ivSplash = findViewById(R.id.iv_splash);
        SharedPreferences sp = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        String splashUri = sp.getString("splash_uri", null);
        Log.d("SplashActivity", "读取到的splashUri=" + splashUri);
        if (splashUri != null) {
            try {
                Log.d("SplashActivity", "尝试用Glide加载Uri: " + splashUri);
                Glide.with(this).load(Uri.parse(splashUri)).into(ivSplash);
            } catch (Exception e) {
                Log.e("SplashActivity", "Glide加载自定义开屏失败: " + e.getMessage(), e);
                ivSplash.setImageResource(R.drawable.bg_default);
            }
        } else {
            Log.d("SplashActivity", "未设置自定义开屏，使用默认图片");
            ivSplash.setImageResource(R.drawable.bg_default);
        }
        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, com.example.newsapp.MainActivity.class));
            finish();
        }, 1500);
    }
} 