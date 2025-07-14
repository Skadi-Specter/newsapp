package com.example.newsapp.ui.updates;

import android.content.Context;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.entity.Comment;
import com.example.newsapp.database.AppDatabase;

import java.io.File;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    public void updateData(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }
    
    public void addComment(Comment comment) {
        this.comments.add(0, comment);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(comments.get(position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView commenterAvatar;
        private final TextView commenterNickname;
        private final TextView commentContent;
        private final TextView commentTimestamp;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            commenterAvatar = itemView.findViewById(R.id.iv_commenter_avatar);
            commenterNickname = itemView.findViewById(R.id.tv_commenter_nickname);
            commentContent = itemView.findViewById(R.id.tv_comment_content);
            commentTimestamp = itemView.findViewById(R.id.tv_comment_timestamp);
        }

        public void bind(final Comment comment) {
            Context context = itemView.getContext();
            AppDatabase db = AppDatabase.getInstance(context);
            new Thread(() -> {
                com.example.newsapp.entity.User user = db.userDao().getUserByPhone(comment.userId);
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> {
                    if (user != null) {
                        commenterNickname.setText(user.nickname);
                        if (user.avatarPath != null && !user.avatarPath.isEmpty()) {
                            com.bumptech.glide.Glide.with(context)
                                .load(user.avatarPath)
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .into(commenterAvatar);
                        } else {
                            commenterAvatar.setImageResource(R.drawable.ic_default_avatar);
                        }
                    } else {
                        commenterNickname.setText(comment.userNickname);
                        commenterAvatar.setImageResource(R.drawable.ic_default_avatar);
                    }
                });
            }).start();
            commentContent.setText(comment.content);
            CharSequence timeAgo = android.text.format.DateUtils.getRelativeTimeSpanString(
                comment.timestamp, System.currentTimeMillis(), android.text.format.DateUtils.MINUTE_IN_MILLIS);
            commentTimestamp.setText(timeAgo);
        }
    }
} 