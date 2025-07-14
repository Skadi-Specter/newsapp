package com.example.newsapp.ui.main;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.ui.detail.NewsDetailActivity;
import com.example.newsapp.ui.profile.ProfileFragment;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    public interface OnNewsClickListener {
        void onNewsClick(News news);
    }
    private List<News> newsList;
    private OnNewsClickListener clickListener;

    public NewsAdapter(List<News> newsList, OnNewsClickListener listener) {
        this.newsList = newsList;
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        Log.d("legion", "NewsAdapter onBindViewHolder position=" + position + ", title=" + news.title);
        holder.title.setText(news.title);
        holder.source.setText(news.author_name != null ? news.author_name : "新闻来源");
        holder.time.setText(news.date != null ? news.date : "新闻时间");
        // 加载图片
        Glide.with(holder.image.getContext())
                .load(news.thumbnail_pic_s)
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onNewsClick(news);
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, source, time;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.news_image);
            title = itemView.findViewById(R.id.news_title);
            source = itemView.findViewById(R.id.news_source);
            time = itemView.findViewById(R.id.news_time);
        }
    }
}