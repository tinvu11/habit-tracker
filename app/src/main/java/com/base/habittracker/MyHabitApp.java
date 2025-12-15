package com.base.habittracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.base.habittracker.utils.ThemeHelper;
import com.google.android.gms.ads.MobileAds;

public class MyHabitApp extends Application {
    public static final String CHANNEL_ID = "habit_reminder_channel";
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        int savedTheme = ThemeHelper.getSavedTheme(this);
        AppCompatDelegate.setDefaultNightMode(savedTheme);
        MobileAds.initialize(this, initializationStatus -> {
        });
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc nhở thói quen",
                    NotificationManager.IMPORTANCE_HIGH //để hiện popup
            );
            channel.setDescription("Thông báo nhắc nhở thực hiện thói quen");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}
