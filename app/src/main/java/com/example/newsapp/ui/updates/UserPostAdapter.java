package com.example.newsapp.ui.updates;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.entity.UserPost;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.User;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.List;
import java.util.Set;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserPostAdapter extends RecyclerView.Adapter<UserPostAdapter.UserPostViewHolder> {

    private List<UserPost> posts;
    private Set<Integer> likedPostIds;
    private final OnPostInteractionListener listener;
    private final String currentUserId;
    private boolean hideLikeAndComment = false;

    public String getCurrentUserId() {
        return currentUserId;
    }

    public interface OnPostInteractionListener {
        void onLikeClicked(UserPost post, int position);
        void onCommentClicked(UserPost post);
        void onDeleteClicked(UserPost post, int position);
    }

    public UserPostAdapter(List<UserPost> posts, Set<Integer> likedPostIds, String currentUserId, OnPostInteractionListener listener, boolean hideLikeAndComment) {
        this.posts = posts;
        this.likedPostIds = likedPostIds;
        this.currentUserId = currentUserId;
        this.listener = listener;
        this.hideLikeAndComment = hideLikeAndComment;
    }

    public void updateData(List<UserPost> newPosts, Set<Integer> newLikedPostIds) {
        this.posts = newPosts;
        this.likedPostIds = newLikedPostIds;
        notifyDataSetChanged();
    }
    
    public void removePost(int position) {
        if (position >= 0 && position < posts.size()) {
            posts.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updatePost(UserPost post, int position, boolean isLiked) {
        posts.set(position, post);
        if (isLiked) {
            likedPostIds.add(post.id);
        } else {
            likedPostIds.remove(post.id);
        }
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public UserPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.update_item, parent, false);
        return new UserPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserPostViewHolder holder, int position) {
        UserPost post = posts.get(position);
        boolean isLiked = likedPostIds.contains(post.id);
        holder.bind(post, currentUserId, isLiked, listener, hideLikeAndComment);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class UserPostViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView authorAvatar;
        private final TextView authorNickname;
        private final TextView postTimestamp;
        private final TextView postContent;
        private final TextView likeButton;
        private final TextView commentButton;
        private final ImageButton moreOptionsButton;

        public UserPostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorAvatar = itemView.findViewById(R.id.iv_author_avatar);
            authorNickname = itemView.findViewById(R.id.tv_author_nickname);
            postTimestamp = itemView.findViewById(R.id.tv_post_timestamp);
            postContent = itemView.findViewById(R.id.tv_post_content);
            likeButton = itemView.findViewById(R.id.btn_like);
            commentButton = itemView.findViewById(R.id.btn_comment);
            moreOptionsButton = itemView.findViewById(R.id.btn_more_options);
        }

        public void bind(final UserPost post, final String currentUserId, final boolean isLiked, final OnPostInteractionListener listener, boolean hideLikeAndComment) {
            Context context = itemView.getContext();
            
            // 实时查User表，显示最新昵称和头像
            com.example.newsapp.database.AppDatabase db = com.example.newsapp.database.AppDatabase.getInstance(context);
            android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            new Thread(() -> {
                com.example.newsapp.entity.User user = db.userDao().getUserByPhone(post.authorPhone);
                mainHandler.post(() -> {
                    if (user != null) {
                        authorNickname.setText(user.nickname);
                        if (user.avatarPath != null && !user.avatarPath.isEmpty()) {
                            com.bumptech.glide.Glide.with(context)
                                .load(user.avatarPath)
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .into(authorAvatar);
                        } else {
                            authorAvatar.setImageResource(R.drawable.ic_default_avatar);
                        }
                    } else {
                        authorNickname.setText(post.authorNickname);
                        authorAvatar.setImageResource(R.drawable.ic_default_avatar);
                    }
                });
            }).start();
            
            postContent.setText(post.content);
            CharSequence timeAgo = android.text.format.DateUtils.getRelativeTimeSpanString(
                post.timestamp, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS);
            postTimestamp.setText(timeAgo);

            // 设置点赞数和图标
            if (post.likeCount > 0) {
                likeButton.setText(String.valueOf(post.likeCount));
            } else {
                likeButton.setText("赞");
            }
            likeButton.setCompoundDrawablesWithIntrinsicBounds(
                isLiked ? R.drawable.ic_like_filled : R.drawable.ic_like_border, 0, 0, 0);

            // 设置评论数
            if (post.commentCount > 0) {
                commentButton.setText(String.valueOf(post.commentCount));
            } else {
                commentButton.setText("评论");
            }

            // Like/Comment显示控制
            if (hideLikeAndComment) {
                likeButton.setVisibility(View.GONE);
                commentButton.setVisibility(View.GONE);
            } else {
                likeButton.setVisibility(View.VISIBLE);
                commentButton.setVisibility(View.VISIBLE);
            }

            // Show delete button only if the current user is the author
            if (currentUserId != null && currentUserId.hashCode() == post.authorId) {
                moreOptionsButton.setVisibility(View.VISIBLE);
                moreOptionsButton.setOnClickListener(v -> listener.onDeleteClicked(post, getAdapterPosition()));
            } else {
                moreOptionsButton.setVisibility(View.GONE);
            }

            // 新增：头像点击跳转到个人主页
            authorAvatar.setOnClickListener(v -> {
                Intent intent = new Intent(context, com.example.newsapp.ui.user.UserProfileActivity.class);
                intent.putExtra("USER_ID", post.authorPhone);
                context.startActivity(intent);
            });

            likeButton.setOnClickListener(v -> listener.onLikeClicked(post, getAdapterPosition()));
            commentButton.setOnClickListener(v -> listener.onCommentClicked(post));
        }
    }
} 