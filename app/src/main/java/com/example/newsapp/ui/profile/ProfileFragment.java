package com.example.newsapp.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.User;
import com.example.newsapp.login.LoginActivity;
import com.example.newsapp.network.NewsDataManager;
import com.example.newsapp.ui.main.News;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment implements View.OnClickListener{
    private static final int REQUEST_PICK_AVATAR = 1;
    private static final int REQUEST_PICK_BG = 2;

    private ImageView ivAvatar, ivBg, ivEdit;
    private TextView tvNickname;
    private TextView tvPostCount, tvFollowCount, tvFansCount;
    private RecyclerView rvRecent;
    private User currentUser;
    private AppDatabase db;
    private SharedPreferences sp;
    private Activity activity;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // 最近浏览（内存保存，退出登录或应用时清空）
    public static List<News> recentNews = new ArrayList<>();

    private LinearLayout layoutUnlogin;
    private View layoutProfile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        // 1. 优先初始化核心对象
        activity = getActivity();
        db = AppDatabase.getInstance(activity);

        // 2. 初始化所有视图引用
        layoutUnlogin = view.findViewById(R.id.layout_unlogin);
        layoutProfile = view.findViewById(R.id.layout_profile);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        ivBg = view.findViewById(R.id.iv_bg);
        ivEdit = view.findViewById(R.id.iv_edit);
        tvNickname = view.findViewById(R.id.tv_nickname);
        tvPostCount = view.findViewById(R.id.tv_post_count);
        tvFollowCount = view.findViewById(R.id.tv_follow_count);
        tvFansCount = view.findViewById(R.id.tv_fans_count);
        Button btnLogout = layoutProfile.findViewById(R.id.btn_logout);
        Button btnLogoff = layoutProfile.findViewById(R.id.btn_logoff);

        // 3. 设置所有监听器
        layoutUnlogin.findViewById(R.id.btn_login).setOnClickListener(v -> 
                startActivity(new Intent(getActivity(), LoginActivity.class)));
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(this);
        view.findViewById(R.id.btn_recent).setOnClickListener(this);
        view.findViewById(R.id.btn_theme).setOnClickListener(this);
        view.findViewById(R.id.btn_theme).setOnLongClickListener(v -> {
            // 恢复默认壁纸
            SharedPreferences sp = requireContext().getSharedPreferences("theme_prefs", android.content.Context.MODE_PRIVATE);
            sp.edit().remove("splash_uri").apply();
            android.widget.Toast.makeText(getContext(), "已恢复默认壁纸，下次启动生效", android.widget.Toast.LENGTH_SHORT).show();
            return true;
        });
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnLogoff.setOnClickListener(v -> showLogoffConfirmDialog());
        ivAvatar.setOnClickListener(v -> pickImage(REQUEST_PICK_AVATAR));
        ivBg.setOnClickListener(v -> pickImage(REQUEST_PICK_BG));
        ivEdit.setOnClickListener(v -> {
            if (currentUser == null) return;
            showEditDialog("修改昵称", currentUser.nickname, value -> {
                currentUser.nickname = value;
                executor.execute(() -> {
                    db.userDao().updateUser(currentUser);
                    activity.runOnUiThread(() -> tvNickname.setText(value));
                });
            });
        });
        tvFollowCount.setOnClickListener(v -> FollowTabActivity.start(activity, "follow"));
        tvFansCount.setOnClickListener(v -> FollowTabActivity.start(activity, "fans"));

        // 4. onCreateView只负责创建和初始化，UI状态的更新完全交给onResume
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_edit_profile) {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        }
        else if(v.getId() == R.id.btn_recent) {
            startActivity(new Intent(getActivity(), RecentActivity.class));
        }
        // 动态权限申请，切换主题前先请求权限
        else if(v.getId() == R.id.btn_theme) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1001);
                    return;
                }
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
                    return;
                }
            }
            // 已有权限，直接打开相册
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1011);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，打开相册
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1011);
            } else {
                android.widget.Toast.makeText(getContext(), "请授予图片访问权限后再设置开屏图片", android.widget.Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1011 && resultCode == android.app.Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // 立即用Glide加载到ivBg，确认Uri有效
                Glide.with(this).load(uri).into(ivBg);
                // 强制加上读权限标志，兼容部分ROM和相册
                final int takeFlags = (data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION)) | Intent.FLAG_GRANT_READ_URI_PERMISSION;
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                SharedPreferences sp = requireContext().getSharedPreferences("theme_prefs", android.content.Context.MODE_PRIVATE);
                sp.edit().putString("splash_uri", uri.toString()).apply();
                android.widget.Toast.makeText(getContext(), "已设置为自定义开屏，下次启动生效", android.widget.Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                executor.execute(() -> {
                    if (requestCode == REQUEST_PICK_AVATAR) {
                        currentUser.avatarPath = uri.toString();
                        db.userDao().updateUser(currentUser);
                        activity.runOnUiThread(() -> Glide.with(this).load(uri).into(ivAvatar));
                    } else if (requestCode == REQUEST_PICK_BG) {
                        currentUser.bgPath = uri.toString();
                        db.userDao().updateUser(currentUser);
                        activity.runOnUiThread(() -> Glide.with(this).load(uri).into(ivBg));
                    }
                });
            }
        }
    }

    private void loadUserData(String phone) {
        executor.execute(() -> {
            currentUser = db.userDao().getUserByPhone(phone);
            if (activity != null && currentUser != null) {
                activity.runOnUiThread(this::showUserInfo);
            }
        });
    }

    private void showUserInfo() {
        // 头像
        if (currentUser.avatarPath != null && !currentUser.avatarPath.isEmpty()) {
            Glide.with(this).load(currentUser.avatarPath).into(ivAvatar);
        }
        // 背景
        if (currentUser.bgPath != null && !currentUser.bgPath.isEmpty()) {
            Glide.with(this).load(currentUser.bgPath).into(ivBg);
        }
        tvNickname.setText(currentUser.nickname);
        tvPostCount.setText(String.valueOf(currentUser.postCount));
        tvFollowCount.setText(String.valueOf(Math.max(0, currentUser.followCount)));
        tvFansCount.setText(String.valueOf(Math.max(0, currentUser.fansCount)));
    }

    // 选择图片
    private void pickImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    // 通用编辑对话框
    private void showEditDialog(String title, String oldValue, OnValueSetListener listener) {
        final EditText et = new EditText(activity);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setText(oldValue == null ? "" : oldValue);
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(et)
                .setPositiveButton("确定", (dialog, which) -> {
                    String value = et.getText().toString().trim();
                    if (!value.isEmpty()) listener.onSet(value);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 最近浏览适配器（这里只做演示，实际可自定义item布局）
//    class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
//        @NonNull
//        @Override
//        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            TextView tv = new TextView(parent.getContext());
//            tv.setPadding(16, 16, 16, 16);
//            tv.setBackgroundResource(R.drawable.shape_edit_normal);
//            return new ViewHolder(tv);
//        }
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//            holder.tv.setText((CharSequence) recentNews.get(position));
//        }
//        @Override
//        public int getItemCount() {
//            return recentNews.size();
//        }
//        class ViewHolder extends RecyclerView.ViewHolder {
//            TextView tv;
//            ViewHolder(View itemView) {
//                super(itemView);
//                tv = (TextView) itemView;
//            }
//        }
//    }

    interface OnValueSetListener {
        void onSet(String value);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次回到这个Fragment时都检查登录状态并刷新用户数据
        updateViewBasedOnLoginState();
    }

    private void updateViewBasedOnLoginState() {
        SharedPreferences sp = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String phone = sp.getString("current_user_phone", null);

        if (phone == null) {
            // 未登录
            layoutUnlogin.setVisibility(View.VISIBLE);
            layoutProfile.setVisibility(View.GONE);
            currentUser = null; // 清理过时的用户数据
        } else {
            // 已登录
            layoutUnlogin.setVisibility(View.GONE);
            layoutProfile.setVisibility(View.VISIBLE);
            // 每次都查库刷新，保证头像、封面、昵称等信息与编辑资料界面一致
            loadUserData(phone);
        }
    }

    public static void addRecentNews(News news) {
        // 去重
        for (int i = 0; i < recentNews.size(); i++) {
            if (recentNews.get(i).uniquekey.equals(news.uniquekey)) {
                recentNews.remove(i);
                break;
            }
        }
        // 添加到最前面
        recentNews.add(0, news);
        // 最多10条
        if (recentNews.size() > 10) {
            recentNews.remove(recentNews.size() - 1);
        }
    }

    /**
     * 显示退出登录对话框
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？\n\n退出后需要重新登录才能使用个人功能。")
                .setPositiveButton("确定", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示注销确认对话框
     */
    private void showLogoffConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("确认注销")
                .setMessage("注销后您的账号信息将被永久删除，无法恢复。\n\n确定要注销该账号吗？")
                .setPositiveButton("确认注销", (dialog, which) -> {
                    performLogoff();
                })
                .setNegativeButton("取消", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * 执行退出登录操作
     */
    private void performLogout() {
        try {
            // 1. 清除SharedPreferences中的登录状态（只清除current_user_phone）
            SharedPreferences sp = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            sp.edit().remove("current_user_phone").apply();
            // 2. 清空最近浏览记录
            ProfileFragment.recentNews.clear();
            // 3. 切换回未登录界面
            layoutUnlogin.setVisibility(View.VISIBLE);
            layoutProfile.setVisibility(View.GONE);
            // 4. 重置当前用户对象
            currentUser = null;
            //5. 还原头像和封面为默认图片
            if (ivAvatar != null) {
                ivAvatar.setImageResource(R.drawable.ic_launcher_background);
            }
            if (ivBg != null) {
                ivBg.setImageResource(R.drawable.bg_cat);
            }
            Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "退出登录失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 执行注销操作
     */
    private void performLogoff() {
        if (currentUser != null) {
            executor.execute(() -> {
                db.userDao().deleteUser(currentUser);
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "账号已注销", Toast.LENGTH_SHORT).show();
                    performLogout();
                });
            });
        }
    }

    // 强制刷新用户信息，供适配器调用
    public void reloadUserInfoForce() {
        SharedPreferences sp = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String phone = sp.getString("current_user_phone", null);
        if (phone != null) {
            loadUserData(phone);
        }
    }

}