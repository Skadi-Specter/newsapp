package com.example.newsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.newsapp.login.LoginActivity;
import com.example.newsapp.ui.chat.AiChatFragment;
import com.example.newsapp.ui.main.HomeFragment;
import com.example.newsapp.ui.profile.ProfileFragment;
import com.example.newsapp.ui.publish.PublishActivity;
import com.example.newsapp.ui.updates.UpdatesFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import android.view.Menu;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private Fragment homeFragment;
    private Fragment updatesFragment;
    private Fragment aiChatFragment;
    private Fragment profileFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("legion", "onCreate: MainActivity");

        homeFragment = new HomeFragment();
        updatesFragment = new UpdatesFragment();
        aiChatFragment = new AiChatFragment();
        profileFragment = new ProfileFragment();
        Log.d("legion", "Fragment实例化完成");

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);

        // 只add一次，后续show/hide
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "home")
                .add(R.id.fragment_container, updatesFragment, "updates").hide(updatesFragment)
                .add(R.id.fragment_container, aiChatFragment, "aiChat").hide(aiChatFragment)
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .commit();
            currentFragment = homeFragment;
        } else {
            homeFragment = getSupportFragmentManager().findFragmentByTag("home");
            updatesFragment = getSupportFragmentManager().findFragmentByTag("updates");
            aiChatFragment = getSupportFragmentManager().findFragmentByTag("aiChat");
            profileFragment = getSupportFragmentManager().findFragmentByTag("profile");
            // 恢复currentFragment
            if (homeFragment != null && !homeFragment.isHidden()) currentFragment = homeFragment;
            else if (updatesFragment != null && !updatesFragment.isHidden()) currentFragment = updatesFragment;
            else if (aiChatFragment != null && !aiChatFragment.isHidden()) currentFragment = aiChatFragment;
            else if (profileFragment != null && !profileFragment.isHidden()) currentFragment = profileFragment;
        }

        navView.setOnItemSelectedListener(item -> {
            Log.d("legion", "onItemSelected: itemId=" + item.getItemId());
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.navigation_updates) {
                selectedFragment = updatesFragment;
            } else if (itemId == R.id.navigation_publish) {
                Log.d("legion", "点击了发布动态按钮");
                SharedPreferences sp = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                Log.d("legion", "current_user_phone=" + sp.getString("current_user_phone", null));
                boolean isLogin = sp.getString("current_user_phone", null) != null;
                Log.d("legion", "isLogin=" + isLogin);
                if (isLogin) {
                    Log.d("legion", "已登录，跳转发布动态");
                    startActivity(new Intent(this, PublishActivity.class));
                } else {
                    Log.d("legion", "未登录，弹窗提示");
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("请先登录")
                        .setPositiveButton("确定", null)
                        .show();
                }
                return false; // 阻止选中和跳转
            } else if (itemId == R.id.navigation_ai_chat) {
                selectedFragment = aiChatFragment;
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = profileFragment;
            }

            if (selectedFragment != null && selectedFragment != currentFragment) {
                getSupportFragmentManager().beginTransaction()
                    .hide(currentFragment)
                    .show(selectedFragment)
                    .commit();
                Log.d("legion", "show Fragment: " + selectedFragment.getClass().getSimpleName());
                currentFragment = selectedFragment;
            } else if (selectedFragment == currentFragment) {
                Log.d("legion", "重复点击当前Fragment: " + selectedFragment.getClass().getSimpleName());
            } else {
                Log.d("legion", "selectedFragment == null");
            }
            return true;
        });

        // 拦截重复点击"发布动态"按钮
        navView.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.navigation_publish) {
                SharedPreferences sp = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                if (sp.getString("current_user_phone", null) == null) {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("请先登录")
                        .setPositiveButton("确定", null)
                        .show();
                }
            }
        });

        // Set default fragment
        if (savedInstanceState == null) {
            Log.d("legion", "savedInstanceState == null，设置默认选中首页");
            navView.setSelectedItemId(R.id.navigation_home);
        } else {
            Log.d("legion", "savedInstanceState != null，不设置默认首页");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 不再隐藏或移除发布动态按钮，保留原样
    }
}