package com.example.newsapp.ui.detail;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.newsapp.R;

public class CommentDialogFragment extends DialogFragment {

    private static final String ARG_PARENT_COMMENT_ID = "parent_comment_id";
    private static final String ARG_REPLY_TO_NICKNAME = "reply_to_nickname";

    private EditText etCommentContent;
    private Button btnSendComment;
    private TextView tvCharCounter;
    private TextView tvReplyTo;

    private OnCommentPostListener listener;

    public interface OnCommentPostListener {
        void onCommentPosted(String content, @Nullable Integer parentCommentId);
    }

    public static CommentDialogFragment newInstance(@Nullable Integer parentCommentId, @Nullable String replyToNickname) {
        CommentDialogFragment fragment = new CommentDialogFragment();
        Bundle args = new Bundle();
        if (parentCommentId != null) {
            args.putInt(ARG_PARENT_COMMENT_ID, parentCommentId);
            args.putString(ARG_REPLY_TO_NICKNAME, replyToNickname);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listener = (OnCommentPostListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnCommentPostListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_comment, null);

        etCommentContent = view.findViewById(R.id.et_comment_content);
        btnSendComment = view.findViewById(R.id.btn_send_comment);
        tvCharCounter = view.findViewById(R.id.tv_char_counter);
        tvReplyTo = view.findViewById(R.id.tv_reply_to);

        Integer parentId = getArguments() != null && getArguments().containsKey(ARG_PARENT_COMMENT_ID)
                ? getArguments().getInt(ARG_PARENT_COMMENT_ID) : null;
        String replyToNickname = getArguments() != null ? getArguments().getString(ARG_REPLY_TO_NICKNAME) : null;

        if (replyToNickname != null) {
            tvReplyTo.setVisibility(View.VISIBLE);
            tvReplyTo.setText("回复 @" + replyToNickname);
        }

        etCommentContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCounter.setText(s.length() + "/200");
                btnSendComment.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSendComment.setOnClickListener(v -> {
            String content = etCommentContent.getText().toString().trim();
            if (!content.isEmpty()) {
                listener.onCommentPosted(content, parentId);
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();
    }
}