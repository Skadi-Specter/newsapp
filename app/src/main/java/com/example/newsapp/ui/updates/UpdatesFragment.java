package com.example.newsapp.ui.updates;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.Follow;
import com.example.newsapp.entity.Like;
import com.example.newsapp.entity.UserPost;
import com.example.newsapp.login.LoginActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class UpdatesFragment extends Fragment implements UserPostAdapter.OnPostInteractionListener {

    private View layoutUnlogin;
    private RecyclerView rvUpdates;
    private TextView tvEmptyFeed;
    private View layoutUpdatesContent;
    private UserPostAdapter adapter;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String currentUserPhone;
    private ActivityResultLauncher<Intent> commentLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_updates, container, false);

        db = AppDatabase.getInstance(getContext());
        layoutUnlogin = view.findViewById(R.id.layout_unlogin);
        layoutUpdatesContent = view.findViewById(R.id.layout_updates_content);
        rvUpdates = view.findViewById(R.id.rv_updates);
        tvEmptyFeed = view.findViewById(R.id.tv_empty_feed);

        setupRecyclerView();

        layoutUnlogin.findViewById(R.id.btn_login).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), LoginActivity.class)));
        
        commentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // No need to check result code, just refresh the feed
                    // in case comment count changed.
                    if (currentUserPhone != null) {
                        loadFeed();
                    }
                });

        return view;
    }

    private void setupRecyclerView() {
        rvUpdates.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserPostAdapter(new ArrayList<>(), new HashSet<>(), currentUserPhone, this, false);
        rvUpdates.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStatusAndUpdateView();
    }

    private void checkLoginStatusAndUpdateView() {
        SharedPreferences sp = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserPhone = sp.getString("current_user_phone", null);

        if (currentUserPhone == null) {
            layoutUnlogin.setVisibility(View.VISIBLE);
            layoutUpdatesContent.setVisibility(View.GONE);
        } else {
            if (adapter == null || !currentUserPhone.equals(adapter.getCurrentUserId())) {
                setupRecyclerView();
            }
            layoutUnlogin.setVisibility(View.GONE);
            layoutUpdatesContent.setVisibility(View.VISIBLE);
            loadFeed();
        }
    }

    private void loadFeed() {
        executorService.execute(() -> {
            // 1. Get IDs of users being followed
            List<Follow> following = db.followDao().getFollowingList(currentUserPhone);
            List<Integer> followedUserIds = following.stream()
                    .map(follow -> follow.followingId.hashCode())
                    .collect(Collectors.toList());

            // 2. Add current user's ID to see their own posts
            followedUserIds.add(currentUserPhone.hashCode());

            // 3. Fetch all posts from these users
            List<UserPost> posts = db.userPostDao().getPostsByUserIds(followedUserIds);

            // 4. Get liked post IDs for the current user
            List<Integer> likedIds = db.likeDao().getLikedPostIds(currentUserPhone);
            Set<Integer> likedPostIds = new HashSet<>(likedIds);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (posts.isEmpty()) {
                        rvUpdates.setVisibility(View.GONE);
                        tvEmptyFeed.setVisibility(View.VISIBLE);
                    } else {
                        rvUpdates.setVisibility(View.VISIBLE);
                        tvEmptyFeed.setVisibility(View.GONE);
                        adapter.updateData(posts, likedPostIds);
                    }
                });
            }
        });
    }

    @Override
    public void onLikeClicked(UserPost post, int position) {
        executorService.execute(() -> {
            boolean isLiked = db.likeDao().isLiked(post.id, currentUserPhone);
            if (isLiked) {
                db.likeDao().unlike(new Like(post.id, currentUserPhone));
                post.likeCount--;
            } else {
                db.likeDao().like(new Like(post.id, currentUserPhone));
                post.likeCount++;
            }
            db.userPostDao().updatePost(post);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.updatePost(post, position, !isLiked));
            }
        });
    }

    @Override
    public void onCommentClicked(UserPost post) {
        Intent intent = new Intent(getActivity(), CommentActivity.class);
        intent.putExtra(CommentActivity.EXTRA_POST_ID, post.id);
        commentLauncher.launch(intent);
    }

    @Override
    public void onDeleteClicked(UserPost post, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除动态")
                .setMessage("确定要删除这条动态吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    executorService.execute(() -> {
                        // Use the new transactional method
                        db.deletePostAndUpdateUserCount(post);
                        
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                adapter.removePost(position);
                                Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }
} 