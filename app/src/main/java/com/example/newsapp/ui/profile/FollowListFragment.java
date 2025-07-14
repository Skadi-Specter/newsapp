package com.example.newsapp.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.Follow;
import com.example.newsapp.entity.User;
import java.util.ArrayList;
import java.util.List;

public class FollowListFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    private String type; // "follow" or "fans"
    private RecyclerView rvUserList;
    private TextView tvListCount;
    private FollowUserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private AppDatabase db;
    private String currentUserPhone;

    public static FollowListFragment newInstance(String type) {
        FollowListFragment fragment = new FollowListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow_list, container, false);
        rvUserList = view.findViewById(R.id.rv_user_list);
        tvListCount = view.findViewById(R.id.tv_list_count);
        db = AppDatabase.getInstance(requireContext());
        currentUserPhone = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).getString("current_user_phone", null);
        type = getArguments() != null ? getArguments().getString(ARG_TYPE, "follow") : "follow";
        rvUserList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FollowUserAdapter(userList, type, currentUserPhone, db);
        rvUserList.setAdapter(adapter);
        loadUserList();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserList();
    }

    private void loadUserList() {
        new Thread(() -> {
            List<User> users = new ArrayList<>();
            if (type.equals("fans")) {
                List<Follow> fans = db.followDao().getFollowerList(currentUserPhone);
                for (Follow f : fans) {
                    User u = db.userDao().getUserByPhone(f.followerId);
                    if (u != null) users.add(u);
                }
            } else {
                List<Follow> follows = db.followDao().getFollowingList(currentUserPhone);
                for (Follow f : follows) {
                    User u = db.userDao().getUserByPhone(f.followingId);
                    if (u != null) users.add(u);
                }
            }
            requireActivity().runOnUiThread(() -> {
                userList.clear();
                userList.addAll(users);
                adapter.notifyDataSetChanged();
                tvListCount.setText("共" + users.size() + (type.equals("fans") ? "个粉丝" : "个关注"));
            });
        }).start();
    }

    // 强制刷新列表，供适配器调用
    public void reloadListForce() {
        loadUserList();
    }
} 