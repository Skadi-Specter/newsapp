package com.example.newsapp.login;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {

    private static final String PREF_NAME = "UserSession";
    private static final String KEY_PHONE = "phone";
    private static UserSession instance;
    private final SharedPreferences sharedPreferences;

    private UserSession(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized UserSession getInstance(Context context) {
        if (instance == null) {
            instance = new UserSession(context.getApplicationContext());
        }
        return instance;
    }
    
    // Overload for cases where context might not be readily available but instance is expected to exist
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UserSession is not initialized, call getInstance(Context) first.");
        }
        return instance;
    }


    public void saveUser(String phone) {
        sharedPreferences.edit().putString(KEY_PHONE, phone).apply();
    }

    public String getPhone() {
        return sharedPreferences.getString(KEY_PHONE, null);
    }

    public boolean isLoggedIn() {
        return getPhone() != null;
    }

    public void logout() {
        sharedPreferences.edit().remove(KEY_PHONE).apply();
    }
} 