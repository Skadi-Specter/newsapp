package com.example.newsapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.ui.main.News;
import com.example.newsapp.ui.detail.NewsDetailActivity;
import java.util.List;

public class RecentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent);

        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("最近浏览");

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        RecyclerView rvRecentList = findViewById(R.id.rv_recent_list);
        rvRecentList.setLayoutManager(new LinearLayoutManager(this));

        List<News> recentList = ProfileFragment.recentNews;
        rvRecentList.setAdapter(new RecentAdapter(recentList));
    }

    static class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
        private final List<News> data;
        RecentAdapter(List<News> data) { this.data = data; }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recent_news, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            News news = data.get(position);
            holder.title.setText(news.title);
            holder.source.setText(news.author_name != null ? news.author_name : "新闻来源");
            holder.time.setText(news.date != null ? news.date : "新闻时间");
            Glide.with(holder.image.getContext())
                    .load(news.thumbnail_pic_s)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.image);

            // 点击跳转详情页
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), NewsDetailActivity.class);
                intent.putExtra("uniquekey", news.uniquekey);
                v.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return data.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView title, source, time;
            ViewHolder(android.view.View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.news_image);
                title = itemView.findViewById(R.id.news_title);
                source = itemView.findViewById(R.id.news_source);
                time = itemView.findViewById(R.id.news_time);
            }
        }
    }
}