package com.example.newsapp.login;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newsapp.R;
import com.example.newsapp.database.AppDatabase;
import com.example.newsapp.entity.User;

public class ForgetPwdActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    private EditText et_new_pwd, et_confirm_pwd, et_code;
    private Button btn_confirm, btn_get_code;
    private CheckBox cb_agree;
    private int code;
    private AppDatabase db;
    private String account;
    private android.os.CountDownTimer timer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("ForgetPwdActivity", "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pwd);

        et_new_pwd = findViewById(R.id.et_new_pwd);
        et_confirm_pwd = findViewById(R.id.et_confirm_pwd);
        et_code = findViewById(R.id.et_code);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_get_code = findViewById(R.id.btn_get_code);
        cb_agree = findViewById(R.id.cb_agree);

        btn_confirm.setOnClickListener(this);
        btn_get_code.setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        
        CheckBox cb_show = findViewById(R.id.cb_show);
        cb_show.setOnCheckedChangeListener(this);

        db = AppDatabase.getInstance(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            account = bundle.getString("account");
        }
        else {
            showMsg("数据错误");
            finish();
        }

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_confirm.setEnabled(!et_new_pwd.getText().toString().trim().isEmpty()
                        && !et_confirm_pwd.getText().toString().trim().isEmpty()
                        && !et_code.getText().toString().trim().isEmpty());
            }
            public void afterTextChanged(Editable s) {}
        };
        et_new_pwd.addTextChangedListener(watcher);
        et_confirm_pwd.addTextChangedListener(watcher);
        et_code.addTextChangedListener(watcher);


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_get_code) {
            code = (int) ((Math.random() * 9 + 1) * 1000);
            new AlertDialog.Builder(this)
                    .setMessage("您的验证码是：" + code)
                    .setPositiveButton("确定", null)
                    .show();
            btn_get_code.setEnabled(false);
            timer = new android.os.CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {
                    btn_get_code.setText("" + millisUntilFinished / 1000 + "s");
                }
                public void onFinish() {
                    btn_get_code.setText("获取验证码");
                    btn_get_code.setEnabled(true);
                }
            }.start();
        }
        else if(v.getId() == R.id.btn_confirm) {
            if (!cb_agree.isChecked()) {
                showAgreementDialog();
                return;
            }
            String newPwd = et_new_pwd.getText().toString().trim();
            String confirmPwd = et_confirm_pwd.getText().toString().trim();
            String inputCode = et_code.getText().toString().trim();
            if (newPwd.isEmpty() || confirmPwd.isEmpty()) {
                showMsg("请填写新密码");
                return;
            }
            if (!newPwd.equals(confirmPwd)) {
                showMsg("两次密码不一致");
                return;
            }
            if (inputCode.isEmpty() || !inputCode.equals(String.valueOf(code))) {
                showMsg("验证码错误");
                return;
            }
            new Thread(() -> {
                User user = db.userDao().getUserByPhone(account);
                if (user != null) {
                    user.password = newPwd;
                    db.userDao().updateUser(user);
                    runOnUiThread(() -> {
                        showMsg("密码重置成功");
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("new_pwd", newPwd);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                        Log.d("legion", "ForgetPwdActivity: finish called");
                    });
                }
            }).start();
        }
        else if(v.getId() == R.id.btn_back) {
            finish();
        }
    }

    private void showAgreementDialog() {
        new AlertDialog.Builder(this)
                .setTitle("用户协议与隐私条款")
                .setMessage("请先阅读并同意用户协议、隐私政策")
                .setPositiveButton("同意", (dialog, which) -> cb_agree.setChecked(true))
                .setNegativeButton("不同意", null)
                .show();
    }

    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cb_show) {
            if (isChecked) {
                et_new_pwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                et_confirm_pwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                et_new_pwd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                et_confirm_pwd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        }
    }
}
