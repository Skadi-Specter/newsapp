package com.example.newsapp.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.newsapp.R;
import com.example.newsapp.network.NewsDataManager;

import java.util.ArrayList;
import java.util.List;

public class NewsListFragment extends Fragment {
    private static final String ARG_CATEGORY = "category";
    private String category;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<News> newsList = new ArrayList<>();
    private NewsDataManager dataManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean hasLoaded = false;

    public static NewsListFragment newInstance(String category) {
        NewsListFragment fragment = new NewsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY, "top");
        }
        dataManager = NewsDataManager.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("legion", "NewsListFragment onCreateView, category=" + category);
        View view = inflater.inflate(R.layout.fragment_news_list, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsAdapter = new NewsAdapter(newsList, news -> {
            // 添加到最近浏览
            com.example.newsapp.ui.profile.ProfileFragment.addRecentNews(news);
            // 跳转到详情页
            android.content.Intent intent = new android.content.Intent(getContext(), com.example.newsapp.ui.detail.NewsDetailActivity.class);
            intent.putExtra("uniquekey", news.uniquekey);
            startActivity(intent);
        });
        recyclerView.setAdapter(newsAdapter);

        // 下拉刷新监听
        swipeRefreshLayout.setOnRefreshListener(() -> refreshNewsData());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("legion", "NewsListFragment onViewCreated, category=" + category);
        if (!hasLoaded) {
            loadNewsData();
            hasLoaded = true;
        }
    }

    /**
     * 加载新闻数据
     */
    private void loadNewsData() {
        Log.d("legion", "NewsListFragment loadNewsData, category=" + category);
        Log.d("NewsListFragment", "开始加载新闻数据，分类: " + category);
        dataManager.getNewsFromCache(category, newsList -> {
            if (getActivity() != null) {
                Log.d("NewsListFragment", "本地缓存返回数据条数: " + (newsList == null ? -1 : newsList.size()));
                NewsListFragment.this.newsList.clear();
                if (newsList != null) {
                    NewsListFragment.this.newsList.addAll(newsList);
                }
                if (recyclerView != null) {
                    recyclerView.post(() -> {
                        newsAdapter.notifyDataSetChanged();
                        Log.d("NewsListFragment", "post刷新UI，Adapter数据条数: " + NewsListFragment.this.newsList.size());
                    });
                }
            } else {
                Log.d("NewsListFragment", "getActivity() == null，无法刷新UI");
            }
        });
    }

    /**
     * 下拉刷新，强制从网络拉取
     */
    private void refreshNewsData() {
        swipeRefreshLayout.setRefreshing(true);
        dataManager.refreshNewsData(category, 1, 20, new NewsDataManager.NewsDataCallback() {
            @Override
            public void onSuccess(List<News> newsList) {
                if (getActivity() != null) {
                    NewsListFragment.this.newsList.clear();
                    NewsListFragment.this.newsList.addAll(newsList);
                    newsAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "刷新成功", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(String errorMessage) {
                if (getActivity() != null) {
                    Toast.makeText(getContext(), "API访问未获取到内容", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("legion", "NewsListFragment onResume, category=" + category);
        if (newsAdapter != null) {
            newsAdapter.notifyDataSetChanged();
        }
    }
}
