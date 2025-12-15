package com.base.habittracker.ui.addedit;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.base.habittracker.R;
import com.base.habittracker.databinding.DialogIconPickerBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;
import java.util.List;

public class IconPickerFragment extends DialogFragment {
    private DialogIconPickerBinding binding;
    private AddEditHabitViewModel addEditHabitViewModel;
    @Override
    public void onStart() {
        super.onStart();
        
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            try {
                DisplayMetrics dm = new DisplayMetrics();
                Activity activity = requireActivity();
                if (activity != null && activity.getWindowManager() != null) {
                    activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
                    int width  = (int) (dm.widthPixels * 0.70);
                    int height = (int) (dm.heightPixels * 0.40);
                    dialog.getWindow().setLayout(width, height);
                }
            } catch (Exception e) {
                Log.e("IconPickerFragment", "Error setting dialog size: " + e.getMessage());
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogIconPickerBinding.inflate(inflater, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Integer> iconList =
                Arrays.asList(
                // === Sức khỏe & Thể chất ===
                R.drawable.ic_running,      // Chạy bộ
                R.drawable.ic_gym,          // Tập gym
                R.drawable.ic_cycling,      // Đạp xe
                R.drawable.ic_swimmer,      // Bơi lội
                R.drawable.ic_water,        // Uống nước
                R.drawable.ic_broccoli,     // Ăn uống lành mạnh
                R.drawable.ic_capsules,     // Uống vitamin
                R.drawable.ic_moon,         // Ngủ
                R.drawable.ic_heart,        // Sức khỏe tim mạch
                R.drawable.ic_massage,      // Thư giãn / Giãn cơ
                R.drawable.ic_meditation,   // Thiền định
                R.drawable.ic_soap,         // Vệ sinh cá nhân

                // === Tinh thần & Năng suất ===
                R.drawable.ic_book,         // Đọc sách
                R.drawable.ic_language,     // Học ngôn ngữ
                R.drawable.ic_edit,         // Viết lách
                R.drawable.ic_note,         // Ghi chú / Kế hoạch
                R.drawable.ic_alrm,         // Đặt báo thức
                R.drawable.ic_sunrise,      // Dậy sớm
                R.drawable.ic_no_social,    // Không dùng mạng xã hội

                // === Sở thích & Sáng tạo ===
                R.drawable.ic_paint,        // Vẽ
                R.drawable.ic_code,         // Lập trình
                R.drawable.ic_chef,         // Nấu ăn
                R.drawable.ic_music,        // Chơi nhạc
                R.drawable.ic_microphone,   // Hát / Podcast
                R.drawable.ic_plant,        // Làm vườn
                R.drawable.ic_flower,       // Trồng hoa

                // === Việc nhà & Tài chính ===
                R.drawable.ic_broom,        // Dọn dẹp
                R.drawable.ic_money,        // Tiết kiệm tiền

                // === Mối quan hệ & Xã hội ===
                R.drawable.ic_volunteer,    // Tình nguyện
                R.drawable.ic_goat,         // Chăm thú cưng (Giả định)

                // === Bỏ thói quen xấu ===
                R.drawable.ic_smoking,      // Bỏ thuốc lá
                R.drawable.ic_beer,         // Bỏ uống rượu
                R.drawable.ic_candy,        // Bỏ ăn đồ ngọt

                // === Mục tiêu & Khác ===
                R.drawable.ic_star,         // Mục tiêu chung
                R.drawable.ic_rocket        // Phát triển bản thân
        );
        addEditHabitViewModel = new ViewModelProvider(requireActivity()).get(AddEditHabitViewModel.class);

        int iconSelected = addEditHabitViewModel.getSelectedIcon().getValue();
        IconAdapter adapter = new IconAdapter(getContext(), iconList, iconResId -> {
            addEditHabitViewModel.updateSelectedIcon(iconResId);
        }, iconSelected, addEditHabitViewModel.getSelectedColor().getValue());

        binding.rvIcons.setAdapter(adapter);
        binding.rvIcons.setLayoutManager(new GridLayoutManager(getContext(), 5));
        
        addEditHabitViewModel.getSelectedColor().observe(getViewLifecycleOwner(), color -> {
            if (color != null && !color.isEmpty()) {
                adapter.updateSelectedColor(Color.parseColor(color));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}