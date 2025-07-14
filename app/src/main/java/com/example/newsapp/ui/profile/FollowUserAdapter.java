package com.example.newsapp.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.User;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.List;

public class FollowUserAdapter extends RecyclerView.Adapter<FollowUserAdapter.ViewHolder> {
    private final List<User> userList;
    private final String type; // "follow" or "fans"
    private final String currentUserPhone;
    private final AppDatabase db;

    public FollowUserAdapter(List<User> userList, String type, String currentUserPhone, AppDatabase db) {
        this.userList = userList;
        this.type = type;
        this.currentUserPhone = currentUserPhone;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvNickname, tvSignature, tvFansCount;
        Button btnAction;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvSignature = itemView.findViewById(R.id.tv_signature);
            tvFansCount = itemView.findViewById(R.id.tv_fans_count);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
        public void bind(User user) {
            Context context = itemView.getContext();
            new Thread(() -> {
                User freshUser = db.userDao().getUserByPhone(user.phone);
                itemView.post(() -> {
                    String avatarPath = (freshUser != null && freshUser.avatarPath != null && !freshUser.avatarPath.isEmpty()) ? freshUser.avatarPath : null;
                    Glide.with(context)
                        .load(avatarPath)
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .into(ivAvatar);
                    tvNickname.setText(freshUser != null ? freshUser.nickname : user.nickname);
                    tvSignature.setText(freshUser != null ? (freshUser.signature == null ? "" : freshUser.signature) : (user.signature == null ? "" : user.signature));
                    tvFansCount.setText(Math.max(0, freshUser != null ? freshUser.fansCount : user.fansCount) + "粉丝");
                });
            }).start();
            btnAction.setVisibility(user.phone.equals(currentUserPhone) ? View.GONE : View.VISIBLE);
            btnAction.setEnabled(true);
            if (user.phone.equals(currentUserPhone)) return;
            if (type.equals("fans")) {
                new Thread(() -> {
                    boolean isFollowed = db.followDao().isFollowing(currentUserPhone, user.phone);
                    itemView.post(() -> {
                        if (isFollowed) {
                            btnAction.setText("取消关注");
                            btnAction.setOnClickListener(v -> doUnfollow(context, user));
                        } else {
                            btnAction.setText("回关");
                            btnAction.setOnClickListener(v -> doFollow(context, user));
                        }
                    });
                }).start();
            } else {
                btnAction.setText("取消关注");
                btnAction.setOnClickListener(v -> doUnfollow(context, user));
            }
            ivAvatar.setOnClickListener(v -> {
                Intent intent = new Intent(context, com.example.newsapp.ui.user.UserProfileActivity.class);
                intent.putExtra("USER_ID", user.phone);
                context.startActivity(intent);
            });
        }
        private void doFollow(Context context, User user) {
            btnAction.setEnabled(false);
            new Thread(() -> {
                db.followDao().follow(new com.example.newsapp.entity.Follow(currentUserPhone, user.phone));
                db.userDao().incrementFansCount(user.phone);
                db.userDao().incrementFollowCount(currentUserPhone);
                itemView.post(() -> {
                    Toast.makeText(context, "已关注", Toast.LENGTH_SHORT).show();
                    refreshAllTabsAndProfile(context);
                    // 关注后重新查库刷新按钮状态
                    bind(user);
                });
            }).start();
        }
        private void doUnfollow(Context context, User user) {
            btnAction.setEnabled(false);
            new Thread(() -> {
                db.followDao().unfollow(new com.example.newsapp.entity.Follow(currentUserPhone, user.phone));
                db.userDao().decrementFansCount(user.phone);
                db.userDao().decrementFollowCount(currentUserPhone);
                itemView.post(() -> {
                    Toast.makeText(context, "已取消关注", Toast.LENGTH_SHORT).show();
                    refreshAllTabsAndProfile(context);
                    // 取关后重新查库刷新按钮状态
                    bind(user);
                });
            }).start();
        }
        // 联动刷新所有关注/粉丝Tab和个人主页
        private void refreshAllTabsAndProfile(Context context) {
            if (context instanceof androidx.fragment.app.FragmentActivity) {
                androidx.fragment.app.FragmentActivity act = (androidx.fragment.app.FragmentActivity) context;
                for (androidx.fragment.app.Fragment f : act.getSupportFragmentManager().getFragments()) {
                    if (f instanceof FollowListFragment) {
                        ((FollowListFragment) f).reloadListForce();
                    }
                    if (f instanceof com.example.newsapp.ui.profile.ProfileFragment) {
                        ((com.example.newsapp.ui.profile.ProfileFragment) f).reloadUserInfoForce();
                    }
                }
            }
            if (context instanceof com.example.newsapp.ui.user.UserProfileActivity) {
                ((com.example.newsapp.ui.user.UserProfileActivity) context).recreate();
            }
        }
    }
} 