package com.example.newsapp.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_PICK_AVATAR = 1;
    private static final int REQUEST_PICK_COVER = 2;

    private ImageView iv_avatar, iv_cover;
    private TextView tv_nickname, tv_signature, tv_gender, tv_age, tv_birthday, tv_location;
    private AppDatabase db;
    private User currentUser;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        iv_avatar = findViewById(R.id.iv_avatar);
        iv_cover = findViewById(R.id.iv_cover);
        tv_nickname = findViewById(R.id.tv_nickname);
        tv_signature = findViewById(R.id.tv_signature);
        tv_gender = findViewById(R.id.tv_gender);
        tv_age = findViewById(R.id.tv_age);
        tv_birthday = findViewById(R.id.tv_birthday);
        tv_location = findViewById(R.id.tv_location);
        
        findViewById(R.id.btn_back).setOnClickListener(this);
        
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("编辑资料");

        db = AppDatabase.getInstance(this);
        loadInitialData();
        
        // 点击事件
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.layout_avatar).setOnClickListener(v -> pickImage(REQUEST_PICK_AVATAR));
        findViewById(R.id.layout_cover).setOnClickListener(v -> pickImage(REQUEST_PICK_COVER));
        findViewById(R.id.layout_nickname).setOnClickListener(v -> showEditDialog("修改昵称", tv_nickname.getText().toString(), value -> {
            updateUserField(user -> user.nickname = value, () -> tv_nickname.setText(value));
        }));
        findViewById(R.id.layout_signature).setOnClickListener(v -> showEditDialog("修改个性签名", tv_signature.getText().toString(), value -> {
            updateUserField(user -> user.signature = value, () -> tv_signature.setText(value));
        }));
        findViewById(R.id.layout_gender).setOnClickListener(v -> showGenderPicker());
        findViewById(R.id.layout_age).setOnClickListener(v -> showAgePicker());
        findViewById(R.id.layout_birthday).setOnClickListener(v -> showBirthdayPicker());
        findViewById(R.id.layout_location).setOnClickListener(v -> showEditDialog("修改所在地", tv_location.getText().toString(), value -> {
            updateUserField(user -> user.location = value, () -> tv_location.setText(value));
        }));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back) {
            finish();
        }
        else if(v.getId() == R.id.tv_gender) {

        }
    }

    private void loadInitialData() {
        executor.execute(() -> {
            SharedPreferences sp = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String phone = sp.getString("current_user_phone", null);
            if (phone != null) {
                currentUser = db.userDao().getUserByPhone(phone);
                if (currentUser != null) {
                    runOnUiThread(this::fillUserInfo);
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "用户信息获取失败，请重新登录", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "未登录，无法编辑", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void fillUserInfo() {
        if (currentUser.avatarPath != null && !currentUser.avatarPath.isEmpty()) {
            Glide.with(this).load(currentUser.avatarPath).into(iv_avatar);
        }
        if (currentUser.bgPath != null && !currentUser.bgPath.isEmpty()) {
            Glide.with(this).load(currentUser.bgPath).into(iv_cover);
        } else {
            iv_cover.setImageResource(R.drawable.bg_default); // 可自定义默认封面图
        }
        tv_nickname.setText(currentUser.nickname == null ? "未设置" : currentUser.nickname);
        tv_signature.setText(currentUser.signature == null ? "未设置" : currentUser.signature);
        tv_gender.setText(currentUser.gender == null ? "未设置" : currentUser.gender);
        tv_age.setText(currentUser.age == 0 ? "未设置" : String.valueOf(currentUser.age));
        tv_birthday.setText(currentUser.birthday == null ? "未设置" : currentUser.birthday);
        tv_location.setText(currentUser.location == null ? "未设置" : currentUser.location);
    }

    private void pickImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                if (requestCode == REQUEST_PICK_AVATAR) {
                    updateUserField(user -> user.avatarPath = uri.toString(), () -> Glide.with(this).load(uri).into(iv_avatar));
                } else if (requestCode == REQUEST_PICK_COVER) {
                    updateUserField(user -> user.bgPath = uri.toString(), () -> Glide.with(this).load(uri).into(iv_cover));
                }
            }
        }
    }

    private void showEditDialog(String title, String oldValue, OnValueSetListener listener) {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT);
        et.setText(oldValue);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(et)
                .setPositiveButton("确定", (dialog, which) -> {
                    String value = et.getText().toString().trim();
                    if (!value.isEmpty()) listener.onSet(value);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showGenderPicker() {
        final String[] genders = {"男", "女"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择性别");
        builder.setItems(genders, (dialog, which) -> {
            updateUserField(user -> user.gender = genders[which], () -> tv_gender.setText(genders[which]));
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    // 年龄选择
    private void showAgePicker() {
        final int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        final String[] years = new String[100];
        for (int i = 0; i < 100; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择出生年份");
        builder.setItems(years, (dialog, which) -> {
            int birthYear = Integer.parseInt(years[which]);
            int age = currentYear - birthYear;
            
            updateUserField(user -> {
                user.age = age;
                if (user.birthday == null || user.birthday.isEmpty()) {
                    user.birthday = birthYear + "-01-01";
                } else {
                    String[] parts = user.birthday.split("-");
                    if (parts.length == 3) {
                        user.birthday = birthYear + "-" + parts[1] + "-" + parts[2];
                    }
                }
            }, () -> {
                tv_age.setText(String.valueOf(age));
                if (currentUser.birthday != null) {
                     String[] parts = currentUser.birthday.split("-");
                     if (parts.length == 3) {
                         tv_birthday.setText(parts[1] + "月" + parts[2] + "日");
                     }
                }
            });
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showBirthdayPicker() {
        // 获取当前生日的月日
        int month = 1, day = 1;
        if (currentUser.birthday != null && currentUser.birthday.length() == 10) {
            String[] parts = currentUser.birthday.split("-");
            if (parts.length == 3) {
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
            }
        }

        // 创建自定义View
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(40, 40, 40, 40);

        final NumberPicker npMonth = new NumberPicker(this);
        final NumberPicker npDay = new NumberPicker(this);

        npMonth.setMinValue(1);
        npMonth.setMaxValue(12);
        npMonth.setValue(month);

        npDay.setMinValue(1);
        npDay.setMaxValue(31);
        npDay.setValue(day);

        // 月份变化时，自动调整天数
        npMonth.setOnValueChangedListener((picker, oldVal, newVal) -> {
            int maxDay = getDaysOfMonth(newVal);
            npDay.setMaxValue(maxDay);
            if (npDay.getValue() > maxDay) {
                npDay.setValue(maxDay);
            }
        });

        layout.addView(npMonth);
        layout.addView(npDay);

        new AlertDialog.Builder(this)
                .setTitle("选择生日")
                .setView(layout)
                .setPositiveButton("确定", (dialog, which) -> {
                    int m = npMonth.getValue();
                    int d = npDay.getValue();
                    int year = getBirthdayYear();
                    String birthday = year + "-" + String.format("%02d", m) + "-" + String.format("%02d", d);
                    int age = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) - year;

                    updateUserField(user -> {
                        user.birthday = birthday;
                        user.age = age;
                    }, () -> {
                        tv_birthday.setText(m + "月" + d + "日");
                        tv_age.setText(String.valueOf(age));
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private int getBirthdayYear() {
        if (currentUser.birthday != null && currentUser.birthday.length() == 10) {
            return Integer.parseInt(currentUser.birthday.substring(0, 4));
        }
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
    }

    private int getDaysOfMonth(int month) {
        switch (month) {
            case 2: return 29; // 简化处理，闰年不判断
            case 4: case 6: case 9: case 11: return 30;
            default: return 31;
        }
    }

    private void updateUserField(java.util.function.Consumer<User> fieldSetter, Runnable uiUpdater) {
        executor.execute(() -> {
            if (currentUser != null) {
                fieldSetter.accept(currentUser);
                db.userDao().updateUser(currentUser);
                
                // Sync the updated data to all posts and comments
                syncUserDataToPostsAndComments(currentUser.nickname, currentUser.avatarPath);

                runOnUiThread(uiUpdater);
            }
        });
    }

    private void syncUserDataToPostsAndComments(String newNickname, String newAvatarPath) {
        // This should also be on a background thread, which it is since it's called from one.
        if (currentUser != null) {
            db.userPostDao().updateAuthorInfoByPhone(currentUser.phone, newNickname, newAvatarPath);
            db.commentDao().updateCommenterInfo(currentUser.phone, newNickname, newAvatarPath);
        }
    }

    interface OnValueSetListener {
        void onSet(String value);
    }
}