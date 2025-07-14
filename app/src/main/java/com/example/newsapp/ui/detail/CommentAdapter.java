package com.example.newsapp.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.Comment;
import com.example.newsapp.entity.User;
import com.example.newsapp.login.UserSession;
import com.example.newsapp.ui.user.UserProfileActivity;
import com.example.newsapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final Context context;
    private List<Comment> comments;
    private final OnCommentListener listener;
    private final Handler mainHandler;
    private final AppDatabase db;
    private final String currentUserId;

    public interface OnCommentListener extends ReplyAdapter.OnReplyListener {
        void onDeleteClick(Comment comment);
    }

    public CommentAdapter(Context context, List<Comment> comments, OnCommentListener listener) {
        this.context = context;
        this.comments = comments;
        this.listener = listener;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.db = AppDatabase.getInstance(context);
        this.currentUserId = UserSession.getInstance().getPhone();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivAvatar;
        TextView tvNickname, tvContent, tvTime;
        Button btnReply, btnDelete;
        RecyclerView rvReplies;
        ReplyAdapter replyAdapter;
        List<Comment> replyList = new ArrayList<>();
        Comment comment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_comment_avatar);
            tvNickname = itemView.findViewById(R.id.tv_comment_nickname);
            tvContent = itemView.findViewById(R.id.tv_comment_content);
            tvTime = itemView.findViewById(R.id.tv_comment_time);
            btnReply = itemView.findViewById(R.id.btn_reply);
            btnDelete = itemView.findViewById(R.id.btn_delete_comment);
            rvReplies = itemView.findViewById(R.id.rv_replies);
        }

        void bind(Comment comment) {
            this.comment = comment;
            new Thread(() -> {
                User user = db.userDao().getUserByPhone(comment.userId);
                mainHandler.post(() -> {
                    if (user != null) {
                        tvNickname.setText(user.nickname);
                        Glide.with(itemView.getContext())
                                .load(user.avatarPath)
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .into(ivAvatar);
                        View.OnClickListener profileClickListener = v -> {
                            Intent intent = new Intent(itemView.getContext(), com.example.newsapp.ui.user.UserProfileActivity.class);
                            intent.putExtra("USER_ID", user.phone);
                            itemView.getContext().startActivity(intent);
                        };
                        ivAvatar.setOnClickListener(profileClickListener);
                        tvNickname.setOnClickListener(profileClickListener);
                    } else {
                        tvNickname.setText("未知用户");
                        ivAvatar.setImageResource(R.drawable.ic_default_avatar);
                        ivAvatar.setOnClickListener(null);
                        tvNickname.setOnClickListener(null);
                    }
                });
            }).start();
            tvContent.setText(comment.content);
            tvTime.setText(TimeUtils.getFriendlyTimeSpanByNow(comment.timestamp));
            btnReply.setOnClickListener(v -> listener.onReplyClick(comment));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(comment));
            if (currentUserId != null && currentUserId.equals(comment.userId)) {
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                btnDelete.setVisibility(View.GONE);
            }
            setupReplyRecyclerView();
            loadReplies();
        }

        private void setupReplyRecyclerView() {
            rvReplies.setLayoutManager(new LinearLayoutManager(context));
            replyAdapter = new ReplyAdapter(context, replyList, new ReplyAdapter.OnReplyListener() {
                @Override
                public void onReplyClick(Comment reply) {
                    listener.onReplyClick(reply);
                }
                @Override
                public void onDeleteClick(Comment reply) {
                    listener.onDeleteClick(reply);
                }
            });
            rvReplies.setAdapter(replyAdapter);
        }

        private void loadReplies() {
            new Thread(() -> {
                List<Comment> replies = db.commentDao().getReplies(comment.id);
                mainHandler.post(() -> {
                    replyList.clear();
                    replyList.addAll(replies);
                    Collections.reverse(replyList); // Optional: show replies in chronological order
                    if (replyAdapter != null) {
                        replyAdapter.notifyDataSetChanged();
                    }
                });
            }).start();
        }
    }
} 