package com.example.newsapp.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey
    @NonNull
    public String phone; // 以手机号为主键
    public String password;

    // 新增的个人信息字段
    public String nickname;
    public String signature;
    public String gender;
    public int age;
    public String birthday;
    public String location;
    public String avatarPath;
    public String bgPath;
    public int followCount;
    public int fansCount;
    public int postCount;
}