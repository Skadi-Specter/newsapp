package com.example.newsapp.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.newsapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FollowTabActivity extends AppCompatActivity {
    public static final String EXTRA_TAB = "tab"; // "follow" or "fans"
    private static final String[] TAB_TITLES = {"关注", "粉丝"};
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_tab);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() { return 2; }
            @Override
            public Fragment createFragment(int position) {
                String type = position == 0 ? "follow" : "fans";
                return FollowListFragment.newInstance(type);
            }
        });
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(TAB_TITLES[position])).attach();
        // 默认选中
        String tab = getIntent().getStringExtra(EXTRA_TAB);
        if (tab != null && tab.equals("fans")) {
            viewPager.setCurrentItem(1, false);
        }
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
    public static void start(Context context, String tab) {
        Intent intent = new Intent(context, FollowTabActivity.class);
        intent.putExtra(EXTRA_TAB, tab);
        context.startActivity(intent);
    }
} 