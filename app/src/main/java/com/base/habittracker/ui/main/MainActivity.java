package com.base.habittracker.ui.main;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.base.habittracker.R;
import com.base.habittracker.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    NavController navController;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Cấu hình status bar để hiển thị đúng màu theo theme
        setupStatusBar();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment); // (ID từ activity_main.xml)

        navController = navHostFragment.getNavController();
        
        // Yêu cầu quyền thông báo sau 10 giây
        requestNotificationPermissionAfterDelay();
    }
    
    private void requestNotificationPermissionAfterDelay() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Hiển thị hộp thoại xin quyền sau 10 giây
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_NOTIFICATION_PERMISSION);
                }
            }
        }, 3000); // 10000 milliseconds = 10 giây
    }

    private void setupStatusBar() {
        // Kiểm tra nếu đang ở chế độ sáng hay tối
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isLightMode = nightMode == Configuration.UI_MODE_NIGHT_NO;
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        // Đặt màu icon status bar: tối cho Light Mode, sáng cho Dark Mode
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(isLightMode);
    }

    private void setContentBottomMarginDp(int dp) {
        View v = findViewById(R.id.nav_host_fragment);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        float d = getResources().getDisplayMetrics().density;
        lp.bottomMargin = Math.round(dp * d);
        v.setLayoutParams(lp);
    }
}