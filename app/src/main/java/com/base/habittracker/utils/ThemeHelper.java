package com.base.habittracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_THEME = "theme_mode";
    public static final int DEFAULT_MODE = AppCompatDelegate.MODE_NIGHT_NO;
    public static void applyTheme(Context context, int themeMode) {
        // 1. Lưu lựa chọn
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(KEY_THEME, themeMode);
        editor.apply();
        // 2. Áp dụng theme ngay lập tức
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }
    public static int getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME, DEFAULT_MODE);
    }
}
