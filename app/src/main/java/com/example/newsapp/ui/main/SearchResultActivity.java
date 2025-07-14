package com.example.newsapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsapp.R;
import com.example.newsapp.network.NewsDataManager;
import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {
    public static final String EXTRA_KEYWORD = "keyword";
    private RecyclerView rvResult;
    private TextView tvEmpty;
    private NewsAdapter adapter;
    private List<News> newsList = new ArrayList<>();

    public static void start(Context context, String keyword) {
        Intent intent = new Intent(context, SearchResultActivity.class);
        intent.putExtra(EXTRA_KEYWORD, keyword);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        rvResult = findViewById(R.id.rv_search_result);
        tvEmpty = findViewById(R.id.tv_empty);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        adapter = new NewsAdapter(newsList, news -> {
            // 跳转详情页
            Intent intent = new Intent(this, com.example.newsapp.ui.detail.NewsDetailActivity.class);
            intent.putExtra("uniquekey", news.uniquekey);
            startActivity(intent);
        });
        rvResult.setLayoutManager(new LinearLayoutManager(this));
        rvResult.setAdapter(adapter);
        String keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
        if (keyword != null && !keyword.isEmpty()) {
            search(keyword);
        }
    }
    private void search(String keyword) {
        NewsDataManager.getInstance(this).searchNewsByTitle(keyword, resultList -> {
            newsList.clear();
            if (resultList != null && !resultList.isEmpty()) {
                newsList.addAll(resultList);
                tvEmpty.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.VISIBLE);
            }
            adapter.notifyDataSetChanged();
        });
    }
} 