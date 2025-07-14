package com.example.newsapp.ui.detail;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.Comment;
import com.example.newsapp.entity.TtsRequest;
import com.example.newsapp.entity.User;
import com.example.newsapp.login.LoginActivity;
import com.example.newsapp.login.UserSession;
import com.example.newsapp.network.NewsApi;
import com.example.newsapp.network.RetrofitClient;
import com.example.newsapp.network.tts.TtsApiService;
import com.example.newsapp.network.tts.TtsRetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.newsapp.entity.NewsDetailCache;
import com.example.newsapp.database.NewsDetailCacheDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsDetailActivity extends AppCompatActivity implements CommentAdapter.OnCommentListener, CommentDialogFragment.OnCommentPostListener {
    private TextView tvTitle, tvInfo, tvContent, tvFakeCommentInput;
    private RecyclerView rvCommentList;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private AppDatabase db;
    private String newsId;

    // For TTS feature
    private FloatingActionButton fabReadAloud;
    private MediaPlayer mediaPlayer;
    private TtsApiService ttsApiService;
    private boolean isPlaying = false;
    private File tempAudioFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());  // 返回按钮

        db = AppDatabase.getInstance(this);

        initViews();
        initTtsService();
        
        newsId = getIntent().getStringExtra("uniquekey");
        if (newsId != null) {
            loadNewsDetailWithCache(newsId);
            loadComments(newsId);
        } else {
            Toast.makeText(this, "无效的新闻ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.news_title);
        tvInfo = findViewById(R.id.tv_info);
        tvContent = findViewById(R.id.tv_content);
        tvFakeCommentInput = findViewById(R.id.tv_fake_comment_input);
        rvCommentList = findViewById(R.id.rv_comment_list);

        fabReadAloud = findViewById(R.id.fab_read_aloud);
        fabReadAloud.setOnClickListener(v -> toggleReadAloud());

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        tvFakeCommentInput.setOnClickListener(v -> onCommentInputClicked());
        
        // Setup RecyclerView
        rvCommentList.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this, commentList, this);
        rvCommentList.setAdapter(commentAdapter);
    }
    
    private void onCommentInputClicked() {
        if (!UserSession.getInstance().isLoggedIn()) {
            showLoginDialog();
        } else {
            CommentDialogFragment dialogFragment = CommentDialogFragment.newInstance(null, null);
            dialogFragment.show(getSupportFragmentManager(), "comment_dialog");
        }
    }
    
    private void showLoginDialog() {
        new AlertDialog.Builder(this)
            .setTitle("需要登录")
            .setMessage("请先登录再进行操作。")
            .setPositiveButton("去登录", (dialog, which) -> {
                startActivity(new Intent(this, LoginActivity.class));
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void loadComments(String newsId) {
        new Thread(() -> {
            // 在后台线程中执行数据库查询
            List<Comment> comments = db.commentDao().getTopLevelComments(newsId);
            for(Comment comment : comments) {
                User user = db.userDao().getUserByPhone(comment.userId);
                if (user != null) {
                    comment.userNickname = user.nickname != null ? user.nickname : "火星用户";
                    comment.userAvatarPath = user.avatarPath;
                } else {
                    comment.userNickname = "匿名用户";
                }
            }
            
            // 返回UI线程更新列表
            runOnUiThread(() -> {
                commentList.clear();
                commentList.addAll(comments);
                commentAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void postComment(String content, @Nullable Integer parentCommentId) {
        String currentUserId = UserSession.getInstance().getPhone();
        if (currentUserId == null) {
            showLoginDialog();
            return;
        }

        new Thread(() -> {
            User currentUser = db.userDao().getUserByPhone(currentUserId);
            String nickname = (currentUser != null && currentUser.nickname != null) ? currentUser.nickname : "火星用户";
            String avatarPath = (currentUser != null) ? currentUser.avatarPath : null;

            final Comment newComment = new Comment(newsId, currentUserId, parentCommentId, content, nickname, avatarPath);
            db.commentDao().insert(newComment);

            // After inserting, reload all comments to refresh the UI
            loadComments(newsId);
        }).start();
    }

    /**
     * 优先本地加载新闻详情，无本地则API获取并写入本地
     */
    private void loadNewsDetailWithCache(String uniquekey) {
        new Thread(() -> {
            NewsDetailCacheDao detailCacheDao = db.newsDetailCacheDao();
            NewsDetailCache cache = detailCacheDao.getDetailByUniquekey(uniquekey);
            if (cache != null && cache.content != null && !cache.content.isEmpty()) {
                // 本地有缓存，直接展示
                runOnUiThread(() -> showDetailFromCache(cache));
            } else {
                // 本地无缓存，访问API
                runOnUiThread(() -> loadNewsDetailFromApi(uniquekey));
            }
        }).start();
    }

    private void showDetailFromCache(NewsDetailCache cache) {
        tvTitle.setText(cache.title);
        tvInfo.setText(cache.author_name + "  " + cache.date);
        // 处理图片
        List<String> imageUrls = new ArrayList<>();
        if (cache.thumbnail_pic_s != null && !cache.thumbnail_pic_s.isEmpty()) imageUrls.add(cache.thumbnail_pic_s);
        if (cache.thumbnail_pic_s02 != null && !cache.thumbnail_pic_s02.isEmpty()) imageUrls.add(cache.thumbnail_pic_s02);
        if (cache.thumbnail_pic_s03 != null && !cache.thumbnail_pic_s03.isEmpty()) imageUrls.add(cache.thumbnail_pic_s03);
        String htmlContent = cache.content;
        if (htmlContent != null) {
            for (int i = 0; i < imageUrls.size(); i++) {
                htmlContent = htmlContent.replace("<!--IMG#" + i + "-->", "<img src='" + imageUrls.get(i) + "'/>");
            }
            GlideImageGetter imageGetter = new GlideImageGetter(this, tvContent);
            tvContent.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT, imageGetter, null));
            tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            tvContent.setText("");
        }
    }

    /**
     * 访问API获取新闻详情，并写入本地
     */
    private void loadNewsDetailFromApi(String uniquekey) {
        NewsApi newsApi = RetrofitClient.getInstance().create(NewsApi.class);
        newsApi.getNewsContent(NewsApi.NEWS_API_KEY, uniquekey)
                .enqueue(new Callback<NewsContentResponse>() {
                    @Override
                    public void onResponse(Call<NewsContentResponse> call, Response<NewsContentResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().error_code == 0) {
                            NewsContentResult result = response.body().result;
                            NewsDetail detail = result.detail;
                            // 写入本地缓存
                            new Thread(() -> {
                                NewsDetailCacheDao detailCacheDao = db.newsDetailCacheDao();
                                NewsDetailCache cache = new NewsDetailCache(
                                        uniquekey,
                                        detail.title,
                                        detail.author_name,
                                        detail.date,
                                        detail.category,
                                        result.content,
                                        detail.thumbnail_pic_s,
                                        detail.thumbnail_pic_s02,
                                        detail.thumbnail_pic_s03
                                );
                                detailCacheDao.insertOrUpdate(cache);
                            }).start();
                            // 展示内容
                            showDetailFromCache(new NewsDetailCache(
                                    uniquekey,
                                    detail.title,
                                    detail.author_name,
                                    detail.date,
                                    detail.category,
                                    result.content,
                                    detail.thumbnail_pic_s,
                                    detail.thumbnail_pic_s02,
                                    detail.thumbnail_pic_s03
                            ));
                        } else {
                            Toast.makeText(NewsDetailActivity.this, "获取详情失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsContentResponse> call, Throwable t) {
                        Toast.makeText(NewsDetailActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onReplyClick(Comment comment) {
        if (!UserSession.getInstance().isLoggedIn()) {
            showLoginDialog();
        } else {
            CommentDialogFragment dialogFragment = CommentDialogFragment.newInstance(comment.id, comment.userNickname);
            dialogFragment.show(getSupportFragmentManager(), "reply_dialog");
        }
    }

    @Override
    public void onDeleteClick(Comment comment) {
        new AlertDialog.Builder(this)
                .setTitle("删除评论")
                .setMessage("确定要删除这条评论吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    deleteComment(comment);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteComment(Comment comment) {
        String currentUserId = UserSession.getInstance().getPhone();
        if (currentUserId == null || !currentUserId.equals(comment.userId)) {
            Toast.makeText(this, "无法删除别人的评论", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            db.commentDao().delete(comment);
            // Deletion successful, reload comments on UI thread
            runOnUiThread(() -> {
                Toast.makeText(NewsDetailActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                loadComments(newsId);
            });
        }).start();
    }

    @Override
    public void onCommentPosted(String content, @Nullable Integer parentCommentId) {
        postComment(content, parentCommentId);
    }

    private void initTtsService() {
        // Get the service instance from the centralized client
        ttsApiService = TtsRetrofitClient.getInstance().create(TtsApiService.class);
    }

    private void toggleReadAloud() {
        if (isPlaying) {
            stopPlayback();
        } else {
            // Simply get the plain text from the TextView
            String textToRead = tvContent.getText().toString();
            if (!textToRead.trim().isEmpty()) {
                startPlayback(textToRead);
            } else {
                Toast.makeText(this, "没有可以朗读的新闻内容。", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startPlayback(String text) {
        fabReadAloud.setEnabled(false);
        Toast.makeText(this, "正在生成语音...", Toast.LENGTH_SHORT).show();

        // Get API Key directly from the interface
        String apiKey = TtsApiService.API_KEY;
        if (apiKey.equals("YOUR_VALID_TTS_KEY_HERE")) {
            Toast.makeText(this, "请在TtsApiService.java中配置有效的API密钥", Toast.LENGTH_LONG).show();
            fabReadAloud.setEnabled(true);
            return;
        }

        TtsRequest request = new TtsRequest(text);

        // Add "Bearer " prefix to the key
        ttsApiService.getSpeech("Bearer " + apiKey, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String contentType = response.headers().get("Content-Type");
                    // Check if the response is actually audio before trying to play it
                    if (contentType != null && contentType.startsWith("audio")) {
                        new Thread(() -> {
                            if (writeResponseBodyToDisk(response.body())) {
                                runOnUiThread(() -> {
                                    fabReadAloud.setEnabled(true);
                                    playAudio();
                                });
                            } else {
                                runOnUiThread(() -> {
                                    fabReadAloud.setEnabled(true);
                                    Toast.makeText(NewsDetailActivity.this, "保存音频文件失败", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }).start();
                    } else {
                        // It's not audio, likely a JSON error response. Log it and show a toast.
                        try {
                            String errorBody = response.body().string();
                            Log.e("TTS_API_ERROR", "API returned an error: " + errorBody);
                            runOnUiThread(() -> {
                                Toast.makeText(NewsDetailActivity.this, "语音API返回错误", Toast.LENGTH_LONG).show();
                                fabReadAloud.setEnabled(true);
                            });
                        } catch (IOException e) {
                            Log.e("TTS_API_ERROR", "Error reading error body", e);
                            runOnUiThread(() -> {
                                fabReadAloud.setEnabled(true);
                                Toast.makeText(NewsDetailActivity.this, "无法读取API错误信息", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                } else {
                    // HTTP request was not successful
                    String errorInfo = "语音生成失败";
                    if (response.errorBody() != null) {
                        try {
                            errorInfo += ": " + response.errorBody().string();
                        } catch (IOException e) { /* ignore */ }
                    } else {
                        errorInfo += ": " + response.code() + " " + response.message();
                    }
                    final String finalErrorInfo = errorInfo;
                    Log.e("TTS_API_ERROR", "API request failed: " + finalErrorInfo);
                    runOnUiThread(() -> {
                        fabReadAloud.setEnabled(true);
                        Toast.makeText(NewsDetailActivity.this, finalErrorInfo, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                fabReadAloud.setEnabled(true);
                Toast.makeText(NewsDetailActivity.this, "网络请求失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void playAudio() {
        if (tempAudioFile != null && tempAudioFile.exists()) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(tempAudioFile.getAbsolutePath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                isPlaying = true;
                fabReadAloud.setImageResource(android.R.drawable.ic_media_pause); // Change icon to 'stop'
                mediaPlayer.setOnCompletionListener(mp -> stopPlayback());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
                stopPlayback();
            }
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        fabReadAloud.setImageResource(android.R.drawable.ic_btn_speak_now); // Change icon back to 'speak'
        if (tempAudioFile != null && tempAudioFile.exists()) {
            tempAudioFile.delete();
        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            tempAudioFile = new File(getExternalCacheDir(), "temp_audio.mp3");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(tempAudioFile);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopPlayback(); // Stop playback when activity is not visible to prevent resource leak
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlayback(); // Ensure all resources are released when the activity is destroyed
    }
}