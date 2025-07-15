package com.example.newsapp.ui.detail;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.entity.Comment;
import com.example.newsapp.ui.user.UserProfileActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.User;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import com.example.newsapp.login.UserSession;

import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private final Context context;
    private final List<Comment> replies;
    private final OnReplyListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AppDatabase db;

    public interface OnReplyListener {
        void onReplyClick(Comment reply);
        void onDeleteClick(Comment reply);
    }

    public ReplyAdapter(Context context, List<Comment> replies, OnReplyListener listener) {
        this.context = context;
        this.replies = replies;
        this.listener = listener;
        this.db = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reply, parent, false);
        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        Comment reply = replies.get(position);
        holder.bind(reply);
    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    class ReplyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivReplyAvatar;
        TextView tvReplyNickname;
        TextView tvReplyContent;
        TextView tvReplyTime;
        Button btnDeleteReply;

        ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReplyAvatar = itemView.findViewById(R.id.iv_reply_avatar);
            tvReplyNickname = itemView.findViewById(R.id.tv_reply_nickname);
            tvReplyContent = itemView.findViewById(R.id.tv_reply_content);
            tvReplyTime = itemView.findViewById(R.id.tv_reply_time);
            btnDeleteReply = itemView.findViewById(R.id.btn_delete_reply);
        }

        void bind(Comment reply) {
            new Thread(() -> {
                User user = db.userDao().getUserByPhone(reply.userId);
                mainHandler.post(() -> {
                    String currentUserId = UserSession.getInstance().getPhone();
                    if (user != null) {
                        tvReplyNickname.setText(user.nickname);
                        Glide.with(context)
                                .load(user.avatarPath)
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .into(ivReplyAvatar);
                        View.OnClickListener profileClickListener = v -> {
                            Intent intent = new Intent(context, com.example.newsapp.ui.user.UserProfileActivity.class);
                            intent.putExtra("USER_ID", user.phone);
                            context.startActivity(intent);
                        };
                        ivReplyAvatar.setOnClickListener(profileClickListener);
                        tvReplyNickname.setOnClickListener(profileClickListener);
                        if (currentUserId != null && currentUserId.equals(reply.userId)) {
                            btnDeleteReply.setVisibility(View.VISIBLE);
                            btnDeleteReply.setOnClickListener(v -> listener.onDeleteClick(reply));
                        } else {
                            btnDeleteReply.setVisibility(View.GONE);
                            btnDeleteReply.setOnClickListener(null);
                        }
                    } else {
                        tvReplyNickname.setText("未知用户");
                        ivReplyAvatar.setImageResource(R.drawable.ic_default_avatar);
                        ivReplyAvatar.setOnClickListener(null);
                        tvReplyNickname.setOnClickListener(null);
                        btnDeleteReply.setVisibility(View.GONE);
                        btnDeleteReply.setOnClickListener(null);
                    }
                });
            }).start();
            tvReplyContent.setText(reply.content);
            tvReplyTime.setText(com.example.newsapp.utils.TimeUtils.getFriendlyTimeSpanByNow(reply.timestamp));
            itemView.setOnClickListener(null);
        }
    }
} 
