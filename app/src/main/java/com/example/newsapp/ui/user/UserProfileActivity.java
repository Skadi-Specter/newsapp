package com.example.newsapp.ui.user;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.Follow;
import com.example.newsapp.entity.User;
import com.example.newsapp.entity.UserPost;
import com.example.newsapp.login.UserSession;
import com.example.newsapp.ui.updates.UserPostAdapter;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private String targetUserId;
    private String currentUserId;
    private AppDatabase db;
    private boolean isFollowing = false;

    private CircleImageView ivUserAvatar;
    private TextView tvUserNickname, tvFollowingCount, tvFollowerCount;
    private Button btnFollow;
    private TextView tvNoPosts;
    private LinearLayout llLikeComment;
    private RecyclerView rvUserPosts;
    private UserPostAdapter postAdapter;
    private List<UserPost> userPosts = new ArrayList<>();
    private TextView tvUserSignature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("UserProfile", "UserProfileActivity onCreate, intent.USER_ID=" + getIntent().getStringExtra("USER_ID"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        db = AppDatabase.getInstance(this);

        ivUserAvatar = findViewById(R.id.iv_user_avatar);
        tvUserNickname = findViewById(R.id.tv_user_nickname);
        tvFollowingCount = findViewById(R.id.tv_following_count);
        tvFollowerCount = findViewById(R.id.tv_follower_count);
        btnFollow = findViewById(R.id.btn_follow);
        tvNoPosts = findViewById(R.id.tv_no_posts_placeholder);
        rvUserPosts = findViewById(R.id.rv_user_posts);
        tvUserSignature = findViewById(R.id.tv_user_signature);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        targetUserId = getIntent().getStringExtra("USER_ID");
        currentUserId = UserSession.getInstance().getPhone();

        if (targetUserId == null) {
            Toast.makeText(this, "无法加载用户信息", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 如果查看的是自己的主页，隐藏关注按钮
        if (targetUserId.equals(currentUserId)) {
            btnFollow.setVisibility(View.GONE);
        } else {
            btnFollow.setVisibility(View.VISIBLE);
            btnFollow.setOnClickListener(v -> onFollowClick());
        }

        rvUserPosts.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new UserPostAdapter(userPosts, new HashSet<>(), currentUserId, new UserPostAdapter.OnPostInteractionListener() {
            @Override
            public void onLikeClicked(UserPost post, int position) {}
            @Override
            public void onCommentClicked(UserPost post) {}
            @Override
            public void onDeleteClicked(UserPost post, int position) {}
        }, true);
        rvUserPosts.setAdapter(postAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次返回页面时都重新加载数据，以保证数据最新
        loadUserProfile();
    }

    private void loadUserProfile() {
        Log.d("UserProfile", "loadUserProfile called, targetUserId=" + targetUserId);
        new Thread(() -> {
            Log.d("UserProfile", "[Thread] 查user, targetUserId=" + targetUserId + ", 当前线程=" + Thread.currentThread().getName());
            User user = db.userDao().getUserByPhone(targetUserId);
            if (user == null) {
                Log.d("UserProfile", "[Thread] user == null");
            } else {
                Log.d("UserProfile", "[Thread] user != null, user.phone=" + user.phone + ", nickname=" + user.nickname + ", avatarPath=" + user.avatarPath);
            }
            List<UserPost> posts = null;
            if (user != null) {
                posts = db.userPostDao().getPostsByUserPhone(user.phone);
                Log.d("UserProfile", "[Thread] user.phone=" + user.phone + ", 动态数=" + (posts == null ? 0 : posts.size()));
            }
            List<UserPost> finalPosts = posts;
            runOnUiThread(() -> {
                Log.d("UserProfile", "[UI] runOnUiThread, user=" + (user == null ? "null" : user.phone));
                if (user != null) {
                    tvUserNickname.setText(user.nickname);
                    tvUserSignature.setText(user.signature == null || user.signature.isEmpty() ? "这个人很神秘，什么都没写~" : user.signature);
                    tvFollowingCount.setText(String.format("%d 关注", Math.max(0, user.followCount)));
                    tvFollowerCount.setText(String.format("%d 粉丝", Math.max(0, user.fansCount)));
                    Glide.with(this)
                            .load(user.avatarPath)
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(ivUserAvatar);
                    Glide.with(this)
                            .load(user.bgPath)
                            .placeholder(R.drawable.bg_cat)
                            .error(R.drawable.bg_cat)
                            .into((ImageView) findViewById(R.id.iv_user_bg));
                }
                userPosts.clear();
                if (finalPosts != null && !finalPosts.isEmpty()) {
                    userPosts.addAll(finalPosts);
                    rvUserPosts.setVisibility(View.VISIBLE);
                    tvNoPosts.setVisibility(View.GONE);
                } else {
                    rvUserPosts.setVisibility(View.GONE);
                    tvNoPosts.setVisibility(View.VISIBLE);
                }
                postAdapter.notifyDataSetChanged();

                // 更新关注状态
                if (currentUserId != null && user != null && !targetUserId.equals(currentUserId)) {
                    Log.d("UserProfile", "[UI] 查询关注状态，currentUserId=" + currentUserId + ", targetUserId=" + targetUserId);
                    new Thread(() -> {
                        boolean following = db.followDao().isFollowing(currentUserId, targetUserId);
                        Log.d("UserProfile", "[Thread] isFollowing查询结果：" + following);
                        runOnUiThread(() -> {
                            isFollowing = following;
                            if (!isFinishing()) {
                                updateFollowButton();
                            }
                        });
                    }).start();
                }
            });
        }).start();
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("取消关注");
        } else {
            btnFollow.setText("关注");
        }
    }

    private void notifyFollowTabRefresh() {
        // 通知所有FollowListFragment刷新
        if (getParent() instanceof androidx.fragment.app.FragmentActivity) {
            androidx.fragment.app.FragmentActivity act = (androidx.fragment.app.FragmentActivity) getParent();
            for (androidx.fragment.app.Fragment f : act.getSupportFragmentManager().getFragments()) {
                if (f instanceof com.example.newsapp.ui.profile.FollowListFragment) {
                    ((com.example.newsapp.ui.profile.FollowListFragment) f).reloadListForce();
                }
            }
        }
    }

    private void onFollowClick() {
        if (currentUserId == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }

        btnFollow.setEnabled(false); // 防止重复点击

        new Thread(() -> {
            if (isFollowing) {
                // 执行取消关注
                db.followDao().unfollow(new Follow(currentUserId, targetUserId));
                db.userDao().decrementFansCount(targetUserId);
                db.userDao().decrementFollowCount(currentUserId);
            } else {
                // 执行关注
                db.followDao().follow(new Follow(currentUserId, targetUserId));
                db.userDao().incrementFansCount(targetUserId);
                db.userDao().incrementFollowCount(currentUserId);
            }
            isFollowing = !isFollowing; // Toggle the state immediately

            // 操作完成后，重新加载整个页面的数据以保证同步
            if (!isFinishing()) {
                runOnUiThread(() -> {
                    loadUserProfile();
                    btnFollow.setEnabled(true);
                    notifyFollowTabRefresh();
                });
            }
        }).start();
    }
} 