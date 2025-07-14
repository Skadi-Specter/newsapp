package com.example.newsapp.ui.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.entity.ai.ChatMessage;
import com.github.ybq.android.spinkit.SpinKitView;

import java.util.List;

public class AiChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;
    private static final int VIEW_TYPE_IMAGE_AI = 3;

    private List<ChatMessage> messages;

    public AiChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.isUser()) {
            return VIEW_TYPE_USER;
        } else {
            if (message.getMessageType() == ChatMessage.MessageType.IMAGE) {
                return VIEW_TYPE_IMAGE_AI;
            } else {
                return VIEW_TYPE_AI;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserMessageViewHolder(view);
        } else if (viewType == VIEW_TYPE_AI) {
            View view = inflater.inflate(R.layout.item_chat_assistant, parent, false);
            return new AiMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_image_ai, parent, false);
            return new AiImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER:
                ((UserMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_AI:
                ((AiMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_AI:
                ((AiImageViewHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder for User messages
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvChatMessage;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatMessage = itemView.findViewById(R.id.tv_chat_message);
        }

        void bind(ChatMessage message) {
            tvChatMessage.setText(message.getMessage());
        }
    }

    // ViewHolder for AI messages
    static class AiMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        SpinKitView loadingIndicator;

        AiMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_chat_message);
            loadingIndicator = itemView.findViewById(R.id.loading_indicator);
        }

        void bind(ChatMessage message) {
            if (message.getMessageType() == ChatMessage.MessageType.THINKING) {
                tvMessage.setVisibility(View.GONE);
                loadingIndicator.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);
                tvMessage.setText(message.getMessage());
            }
        }
    }

    // New ViewHolder for AI-generated images
    static class AiImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGeneratedImage;

        AiImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGeneratedImage = itemView.findViewById(R.id.iv_generated_image);
        }

        void bind(ChatMessage message) {
            if (message.getImageBase64() != null && !message.getImageBase64().isEmpty()) {
                String imageUrl = message.getImageBase64(); // This now contains the URL
                Log.d("legion", "Loading image from URL: " + imageUrl);
                
                // Use Glide to load image from URL
                Glide.with(ivGeneratedImage.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.ic_error)
                    .into(ivGeneratedImage);
            }
        }
    }
} 