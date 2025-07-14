package com.example.newsapp.ui.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.newsapp.R;
import com.example.newsapp.network.NewsApi;
import com.example.newsapp.network.NewsListResponse;
import com.example.newsapp.network.RetrofitClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

import com.example.newsapp.network.NewsDataManager;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;
    private List<News> newsList = new ArrayList<>();
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private EditText etSearch;
    private Button btnSearch;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("legion", "HomeFragment onCreateView");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        etSearch = view.findViewById(R.id.et_search);
        btnSearch = view.findViewById(R.id.btn_search);

        // 设置ViewPager2适配器
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() {
                return NewsCategory.CATEGORY_KEYS.length;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return NewsListFragment.newInstance(NewsCategory.CATEGORY_KEYS[position]);
            }
        });

        // TabLayout与ViewPager2联动
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(NewsCategory.CATEGORY_NAMES[position]);
        }).attach();

        // attach之后再设置当前item，确保首次进入首页正常显示
        viewPager.post(() -> viewPager.setCurrentItem(0, false));

        // 搜索按钮点击事件
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            if (!keyword.isEmpty()) {
                SearchResultActivity.start(requireContext(), keyword);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("legion", "HomeFragment onViewCreated");
        // 移除viewPager.setCurrentItem(0, false)（已移到onCreateView）
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("legion", "HomeFragment onResume");
    }

    private void loadNewsFromNetwork() {
        NewsApi newsApi = RetrofitClient.getInstance().create(NewsApi.class);
        newsApi.getNewsList(
                NewsApi.NEWS_API_KEY, // 你的AppKey
                "top", // 新闻类型
                1,     // 页码
                20,    // 每页条数
                0      // is_filter
        ).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().error_code == 0) {
                    newsList.clear();
                    newsList.addAll(response.body().result.data);
                    newsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "获取新闻失败，code: " + (response.body() != null ? response.body().error_code : response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Toast.makeText(getContext(), "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}