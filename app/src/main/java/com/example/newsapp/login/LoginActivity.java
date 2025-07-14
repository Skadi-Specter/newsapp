package com.example.newsapp.login;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.User;
import com.example.newsapp.login.UserSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private GridLayout gl_phone_login, gl_pwd_login;
    private EditText et_phone, et_code, et_account, et_pwd;
    private Button btn_get_code, btn_login;
    private TextView tv_title;
    private CheckBox cb_agree;
    private int code;
    private CountDownTimer timer;
    private AppDatabase db;
    private ActivityResultLauncher<Intent> forgetPasswordLauncher;
    private SharedPreferences sp;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        gl_phone_login = findViewById(R.id.gl_phone_login);
        gl_pwd_login = findViewById(R.id.gl_pwd_login);
        et_phone = findViewById(R.id.et_phone);
        et_code = findViewById(R.id.et_code);
        et_account = findViewById(R.id.et_account);
        et_pwd = findViewById(R.id.et_pwd);
        tv_title = findViewById(R.id.tv_title);
        cb_agree = findViewById(R.id.cb_agree);
        btn_get_code = findViewById(R.id.btn_get_code);
        btn_login = findViewById(R.id.btn_login);

        btn_get_code.setOnClickListener(this);
        btn_login.setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.btn_forget_pwd).setOnClickListener(this);
        findViewById(R.id.tv_switch).setOnClickListener(this);
        
        CheckBox cb_show = findViewById(R.id.cb_show);
        cb_show.setOnCheckedChangeListener(this);

        db = AppDatabase.getInstance(this);

        // 获取 SharedPreferences 实例
        sp = getSharedPreferences("user", MODE_PRIVATE);

        // 输入监听，控制按钮可用
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (gl_phone_login.getVisibility() == View.VISIBLE) {
                    btn_login.setEnabled(!et_phone.getText().toString().trim().isEmpty()
                            && !et_code.getText().toString().trim().isEmpty());
                } else {
                    btn_login.setEnabled(!et_account.getText().toString().trim().isEmpty()
                            && !et_pwd.getText().toString().trim().isEmpty());
                }
            }
            public void afterTextChanged(Editable s) {}
        };
        et_phone.addTextChangedListener(watcher);
        et_code.addTextChangedListener(watcher);
        et_account.addTextChangedListener(watcher);
        et_pwd.addTextChangedListener(watcher);

        // 创建前往忘记密码界面的启动器
        forgetPasswordLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        String new_pwd = data.getStringExtra("new_pwd");
                        if (new_pwd != null) {
                            gl_phone_login.setVisibility(View.GONE);
                            gl_pwd_login.setVisibility(View.VISIBLE);
                            tv_title.setText("账号密码登录");
                            // 填充新密码到输入框
                            et_pwd.setText(new_pwd);
                        }
                    }
                }
        );

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tv_switch) {
            if (gl_phone_login.getVisibility() == View.VISIBLE) {
                gl_phone_login.setVisibility(View.GONE);
                gl_pwd_login.setVisibility(View.VISIBLE);
                tv_title.setText("账号密码登录");
            } else {
                gl_phone_login.setVisibility(View.VISIBLE);
                gl_pwd_login.setVisibility(View.GONE);
                tv_title.setText("手机号登录/注册");
            }
        }
        else if(v.getId() == R.id.btn_get_code) {
            code = (int)((Math.random() * 9 + 1) * 1000);
            new AlertDialog.Builder(this)
                    .setMessage("您的验证码是：" + code)
                    .setPositiveButton("确定", null)
                    .show();
            btn_get_code.setEnabled(false);
            timer = new CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {
                    btn_get_code.setText("" + millisUntilFinished / 1000 + "s");
                }
                public void onFinish() {
                    btn_get_code.setText("获取验证码");
                    btn_get_code.setEnabled(true);
                }
            }.start();
        }
        else if(v.getId() == R.id.btn_login) {
            if (!cb_agree.isChecked()) {
                showAgreementDialog();
                return;
            }
            if (gl_phone_login.getVisibility() == View.VISIBLE) {
                // 手机号登录
                String phone = et_phone.getText().toString().trim();
                String inputCode = et_code.getText().toString().trim();
                if (inputCode.equals(String.valueOf(code))) {
                    executor.execute(() -> {
                        User user = db.userDao().getUserByPhone(phone);
                        runOnUiThread(() -> {
                            if (user == null) {
                                // 新用户注册，弹窗输入密码
                                showSetPasswordDialog(phone);
                            } else {
                                // 登录成功，保存会话和用户信息
                                loginSuccess(user);
                            }
                        });
                    });
                } else {
                    showMsg("验证码错误");
                }
            } else {
                // 账号密码登录
                String phone = et_account.getText().toString().trim();
                String pwd = et_pwd.getText().toString().trim();
                executor.execute(() -> {
                    User user = db.userDao().getUserByPhone(phone);
                    runOnUiThread(() -> {
                        if (user == null) {
                            showMsg("用户不存在");
                        } else if (!pwd.equals(user.password)) {
                            showMsg("密码错误");
                        } else {
                            // 登录成功，保存会话和用户信息
                            loginSuccess(user);
                        }
                    });
                });
            }
        }
        else if(v.getId() == R.id.btn_forget_pwd) {
            String account = et_account.getText().toString().trim();
            if (account.isEmpty()) {
                showMsg("请先填写账号名称");
                return;
            }
            executor.execute(() -> {
                User user = db.userDao().getUserByPhone(account);
                runOnUiThread(() -> {
                    if (user == null) {
                        showMsg("该账号不存在");
                        return;
                    }
                    Intent intent = new Intent(this, ForgetPwdActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("account", account);
                    intent.putExtras(bundle);
                    // 使用 launcher 启动目标页面
                    forgetPasswordLauncher.launch(intent);
                });
            });
        }
        else if(v.getId() == R.id.btn_back) {
            finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cb_show) {
            if (isChecked) {
                // 显示密码
                et_pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // 隐藏密码
                et_pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }
    }

    private void showAgreementDialog() {
        new AlertDialog.Builder(this)
                .setTitle("用户协议与隐私条款")
                .setMessage("请先阅读并同意用户协议、隐私政策")
                .setPositiveButton("同意并登录", (dialog, which) -> cb_agree.setChecked(true))
                .setNegativeButton("不同意", null)
                .show();
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void showSetPasswordDialog(String phone) {
        final EditText etPwd = new EditText(this);
        etPwd.setHint("请设置密码");
        etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("设置密码")
                .setView(etPwd)
                .setPositiveButton("确定", (dialog, which) -> {
                    String pwd = etPwd.getText().toString().trim();
                    if (pwd.isEmpty()) {
                        showMsg("密码不能为空");
                    } else {
                        executor.execute(()->{
                            User newUser = new User();
                            newUser.phone = phone;
                            newUser.password = pwd;
                            // 新账号注册时头像和封面字段初始化为空，防止继承上一个账号的图片
                            newUser.avatarPath = "";
                            newUser.bgPath = "";
                            db.userDao().insertUser(newUser);
                            runOnUiThread(()->{
                                // 注册成功后自动登录
                                loginSuccess(newUser);
                            });
                        });
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loginSuccess(User user) {
        UserSession.getInstance(LoginActivity.this).saveUser(user.phone);

        // 写入 user_prefs 兼容老逻辑
        SharedPreferences.Editor editor1 = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
        editor1.putString("current_user_phone", user.phone);
        editor1.putInt("current_user_id", user.phone.hashCode());
        editor1.apply();

        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("legion", "LoginActivity: onDestroy called");
    }

}