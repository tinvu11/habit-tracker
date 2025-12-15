package com.base.habittracker.ui.settings;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.base.habittracker.R;
import com.base.habittracker.databinding.FragmentSettingBinding;
import com.base.habittracker.utils.PremiumManager;
import com.base.habittracker.utils.ThemeHelper;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingFragment extends Fragment {

    private FragmentSettingBinding binding;
    private NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateThemeText(binding.txtTheme);
        navController = Navigation.findNavController(view);
        binding.toolbarSetting.setNavigationOnClickListener(v -> navController.popBackStack());
        binding.cardTheme.setOnClickListener(v -> {
            AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Giao diện")
                    .setItems(new String[]{"Tự động", "Giao diện Sáng", "Giao diện Tối"}, (d, which) -> {
                        if(which == 0) {
                            ThemeHelper.applyTheme(requireContext(), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        }
                        if(which == 1) {
                            ThemeHelper.applyTheme(requireContext(), AppCompatDelegate.MODE_NIGHT_NO);
                        }
                        if(which == 2) {
                            ThemeHelper.applyTheme(requireContext(), AppCompatDelegate.MODE_NIGHT_YES);
                        }
                        updateThemeText(binding.txtTheme);
                    })
                    .create();
            dialog.show();

            // Đoạn chỉnh kích thước giữ nguyên
            if (dialog.getWindow() != null) {
                int widthInDp = 300;
                int widthInPixel = (int) (widthInDp * getResources().getDisplayMetrics().density);
                dialog.getWindow().setLayout(widthInPixel, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        binding.btnSetPremium.setOnClickListener(v -> {
            new Handler().postDelayed(() -> {
                PremiumManager.getInstance(requireContext()).setPremium(true);
                Toast.makeText(getContext(), "Thanh toán thành công! (Mock)", Toast.LENGTH_SHORT).show();
            }, 1000);
        });
        binding.btnResetPremium.setOnClickListener(v -> {
            new Handler().postDelayed(() -> {
                PremiumManager.getInstance(requireContext()).setPremium(false);
                Toast.makeText(getContext(), "Đã hủy thanh toán! (Mock)", Toast.LENGTH_SHORT).show();
            }, 1000);
        });
        binding.llSendFeedBack.setOnClickListener(v -> sendFeedback());
        binding.llRateOnStore.setOnClickListener(v -> rateApp());
        binding.llShareWithFriend.setOnClickListener(v -> shareApp());
        binding.llTermOfService.setOnClickListener(v -> openWebPage("https://stirring-plume-c88.notion.site/i-u-kho-n-d-ch-v-Habit-tracker-2b981fa04cc28093a00ce6581d54c4de?pvs=74"));
        binding.llPrivacyPolicy.setOnClickListener(v -> openWebPage("https://stirring-plume-c88.notion.site/Ch-nh-s-ch-b-o-m-t-app-Habit-tracker-2b981fa04cc2801796b8fff9a12db0fb?pvs=73"));
        setAppVersion();
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Chỉ mở các ứng dụng email
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"vutin686@gmail.com"}); // Thay email của bạn vào đây
        intent.putExtra(Intent.EXTRA_SUBJECT, "Góp ý cho Habit Tracker");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "Không tìm thấy ứng dụng Email", Toast.LENGTH_SHORT).show();
        }
    }
    private void rateApp() {
        String packageName = requireContext().getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
        }
    }
    private void shareApp() {
        String packageName = requireContext().getPackageName();
        String shareContent = "Hãy thử ứng dụng Habit Tracker tuyệt vời này: https://play.google.com/store/apps/details?id=" + packageName;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Chia sẻ ứng dụng");
        startActivity(shareIntent);
    }
    private void openWebPage(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không thể mở trình duyệt", Toast.LENGTH_SHORT).show();
        }
    }
    private void setAppVersion() {
        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;

             binding.tvVersionApp.setText("v" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateThemeText(TextView textView) {
        int savedMode = ThemeHelper.getSavedTheme(requireContext());
        switch (savedMode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                textView.setText("Tối");
                break;

            case AppCompatDelegate.MODE_NIGHT_NO:
                textView.setText("Sáng");
                break;

            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
            default:
                textView.setText("Tự động");
                break;
        }
    }
}