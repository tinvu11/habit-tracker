package com.base.habittracker.ui.addedit;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.base.habittracker.R;
import com.base.habittracker.data.model.Habit;
import com.base.habittracker.data.model.HabitType;
import com.base.habittracker.databinding.FragmentAddBinding; // Đảm bảo import này đúng với tên layout XML của bạn
import com.base.habittracker.ui.addedit.AddEditHabitViewModel;
import com.base.habittracker.ui.addedit.ColorAdapter;
import com.base.habittracker.ui.addedit.IconPickerFragment;
import com.base.habittracker.utils.PremiumManager;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.textfield.TextInputLayout;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    private AddEditHabitViewModel addEditHabitViewModel;
    private NavController navController;
    private ColorAdapter colorAdapter;
    private LinearLayoutManager colorLayoutManager;
    private Habit habit;
    private String currentColor = "#FF9800";
    private HabitType habitType = HabitType.TASK;
    private int iconDrawable = R.drawable.ic_check;
    private long dateStart =  LocalDate.now().toEpochDay();
    private List<String> colorsHexList = new ArrayList<>();
    private static final int REQUEST_NOTIFICATION_PERMISSION = 102;
    private InterstitialAd mInterstitialAd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadColorPalette();
        addEditHabitViewModel = new ViewModelProvider(requireActivity()).get(AddEditHabitViewModel.class);
        if (getArguments() != null) {
            habit = (Habit) getArguments().getSerializable("EXTRA_HABIT_OBJ");
            if (habit != null) {
                addEditHabitViewModel.setCurrentHabitID(habit.getId());
                addEditHabitViewModel.setCurrentHabit(habit);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAddBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        initViews();
        initListeners();
        initObservers();
        loadInterstitialAd();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void loadColorPalette() {
        int[] colorValues = requireContext().getResources().getIntArray(R.array.habit_colors_palette);
        for (int colorInt : colorValues) {
            String hex = String.format("#%06X", (0xFFFFFF & colorInt));
            colorsHexList.add(hex);
        }
    }
    private void initViews() {
        colorAdapter = new ColorAdapter(colorsHexList, currentColor, color -> {
            addEditHabitViewModel.updateSelectedColor(color);
            currentColor = color;
        });
        colorLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.rvColors.setLayoutManager(colorLayoutManager);
        binding.rvColors.setAdapter(colorAdapter);
        addEditHabitViewModel.updateSelectedIcon(R.drawable.ic_broccoli);
    }

    private void initListeners() {
        binding.btnTask.setOnClickListener(v -> setHabitType(HabitType.TASK));
        binding.btnNumber.setOnClickListener(v -> {
            setHabitType(HabitType.QUANTITY);
            addEditHabitViewModel.updateTimeslected(1);
        });
        binding.btnTime.setOnClickListener(v -> {
            setHabitType(HabitType.TIME);
            addEditHabitViewModel.updateTimeslected(15);
        });
        binding.inputTimeHabit.setOnClickListener(v -> showNumberTimeDialog(habitType));
        binding.viewRemind.setOnClickListener(v -> showTimePicker());
        binding.viewDateEnd.setOnClickListener(v -> showEndDateBottomSheet());
        binding.iconImg.setOnClickListener(v -> {
            IconPickerFragment iconPickerFragment = new IconPickerFragment();
            iconPickerFragment.show(getChildFragmentManager(), "ICON_PICKER");
        });
        binding.switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !checkNotificationPermission()) {
                // Tắt switch nếu không có quyền
                binding.switchNotification.setChecked(false);
                requestNotificationPermission();
            }
        });
        binding.btnSubmit.setOnClickListener(v -> onSaveHabit());
        binding.materialToolbar.setNavigationOnClickListener(v -> navController.popBackStack());
    }

    private void initObservers() {
        // Sửa
            if (habit == null) {
                setupCreateMode();
            } else {
                // Thêm mới
                setupEditMode(habit);
            }
        addEditHabitViewModel.getTimeslected().observe(getViewLifecycleOwner(), selected ->
                binding.txtNumberTime.setText(String.valueOf(selected)));
        addEditHabitViewModel.getSelectTimeEnd().observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                LocalDate date1 = LocalDate.ofEpochDay(date);
                String d = date1.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                binding.txtTimeEnd.setText(d);
            } else {
                binding.txtTimeEnd.setText("Không bao giờ");
            }
        });
        addEditHabitViewModel.getSelectedIcon().observe(getViewLifecycleOwner(), iconResId -> {
            if (iconResId != null && iconResId != -1) {
                iconDrawable = iconResId;
                binding.iconImg.setImageResource(iconResId);
            }
        });
        addEditHabitViewModel.getSelectedColor().observe(getViewLifecycleOwner(), color -> {
            if (color == null || color.isEmpty()) return;
            currentColor = color;
            int myColor = Color.parseColor(color);
            binding.btnSubmit.setBackgroundColor(myColor);
            binding.cardViewIcon.setCardBackgroundColor(myColor);
            updateSwitchColor(myColor);
            
            // Cập nhật lại màu các button type
            updateButtonState(binding.btnTask, habitType == HabitType.TASK);
            updateButtonState(binding.btnTime, habitType == HabitType.TIME);
            updateButtonState(binding.btnNumber, habitType == HabitType.QUANTITY);
        });
    }


    private void updateSwitchColor(int selectedColor) {
        int[][] thumbStates = new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
        };
        int[] thumbColors = new int[] {
                selectedColor,  // Màu khi bật
                Color.WHITE     // Màu khi tắt
        };
        ColorStateList thumbColorStateList = new ColorStateList(thumbStates, thumbColors);

        // Tạo ColorStateList cho track (thanh nền)
        int lightColor = Color.argb(
                (int)(255 * 0.5), // 50% opacity
                Color.red(selectedColor),
                Color.green(selectedColor),
                Color.blue(selectedColor)
        );
        int[] trackColors = new int[] {
                lightColor,     // Màu khi bật
                Color.parseColor("#E0E0E0")  // Màu khi tắt
        };
        ColorStateList trackColorStateList = new ColorStateList(thumbStates, trackColors);

        // Áp dụng màu
        binding.switchNotification.setThumbTintList(thumbColorStateList);
        binding.switchNotification.setTrackTintList(trackColorStateList);
    }
    private void loadInterstitialAd() {

        PremiumManager.getInstance(requireContext()).getPremiumStatus().observe(getViewLifecycleOwner(), isPremium -> {
            if (isPremium) {
                mInterstitialAd = null; // Đảm bảo biến này rỗng
                return;
            }
        AdRequest adRequest = new AdRequest.Builder().build();

        // ID TEST cho Interstitial: ca-app-pub-3940256099942544/1033173712
        InterstitialAd.load(requireContext(), "ca-app-pub-3940256099942544/1033173712", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;

                        // Cài đặt sự kiện khi quảng cáo được tắt
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Quảng cáo tắt -> Thoát màn hình
                                navController.popBackStack();
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
        });
    }

    private void setupCreateMode() {
        binding.btnSubmit.setText("Tạo");
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        binding.txtTimeRemind.setText(time);
        binding.switchNotification.setChecked(false);
        setHabitType(HabitType.TASK);
    }


    private void setupEditMode(com.base.habittracker.data.model.Habit habit) {
        binding.edtNameHabit.setText(habit.getName());
        binding.txtTimeRemind.setText(habit.getReminderTime());
        binding.switchNotification.setChecked(habit.isNotificationEnabled());
        addEditHabitViewModel.updateSelectedIcon(habit.getIconId());
        addEditHabitViewModel.updateSelectedColor(habit.getColorHex());
        binding.btnSubmit.setText("Lưu");
        currentColor = habit.getColorHex();
        dateStart = habit.getStartDate();

        if (habit.getEndDate() != null) {
            LocalDate date1 = LocalDate.ofEpochDay(habit.getEndDate());
            String d = date1.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            binding.txtTimeEnd.setText(d);
        } else {
            binding.txtTimeEnd.setText("Không bao giờ");
        }
        setHabitType(habit.getType());
        if(habit.getType() == HabitType.TIME){
            addEditHabitViewModel.updateTimeslected(habit.getTargetValue()/60);
        }
        else if(habit.getType() == HabitType.QUANTITY){
            addEditHabitViewModel.updateTimeslected(habit.getTargetValue());
        }
        colorAdapter.setSelection(habit.getColorHex());
        int position = colorsHexList.indexOf(habit.getColorHex());
        if (position != -1) {
            colorLayoutManager.scrollToPosition(position);
        }
    }

    private void setHabitType(HabitType type) {
        this.habitType = type;
        boolean isTask = (type == HabitType.TASK);
        binding.inputTimeHabit.setVisibility(isTask ? View.GONE : View.VISIBLE);
        binding.txtNumberTime.setVisibility(isTask ? View.GONE : View.VISIBLE);
        updateButtonState(binding.btnTask, type == HabitType.TASK);
        updateButtonState(binding.btnTime, type == HabitType.TIME);
        updateButtonState(binding.btnNumber, type == HabitType.QUANTITY);
    }

    private void updateButtonState(MaterialButton button, boolean isSelected) {
        button.setChecked(isSelected);

        int primaryColor = Color.parseColor(currentColor);
        int colorUnselectedText = ContextCompat.getColor(requireContext(), R.color.onSurfaceVariant);
        int colorUnselectedBg = ContextCompat.getColor(requireContext(), R.color.surfaceVariant);
        int lightBgColor = Color.argb(77, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor));

        if (isSelected) {
            button.setBackgroundColor(lightBgColor);
            button.setStrokeColor(ColorStateList.valueOf(primaryColor));
            button.setTextColor(primaryColor);
            button.setIconTint(ColorStateList.valueOf(primaryColor));
        } else {
            button.setBackgroundColor(colorUnselectedBg);
            button.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));
            button.setTextColor(colorUnselectedText);
            button.setIconTint(ColorStateList.valueOf(colorUnselectedText));
        }
    }

    private void showTimePicker() {
        final Dialog dialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_time_picker);
        TimePicker simpleTimePicker = dialog.findViewById(R.id.simpleTimePicker);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnOK = dialog.findViewById(R.id.btnOk);
        simpleTimePicker.setIs24HourView(true);
        int colorInt = Color.parseColor(currentColor);
        btnOK.setTextColor(colorInt);
        btnCancel.setTextColor(colorInt);


        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnOK.setOnClickListener(v -> {
            int hour = simpleTimePicker.getHour();
            int minute = simpleTimePicker.getMinute();
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            binding.txtTimeRemind.setText(time);
            dialog.dismiss();
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.show();
    }

    private void showEndDateBottomSheet() {
        final Dialog dialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottom_sheet_time_end);
        LinearLayout txtNeverEnd = dialog.findViewById(R.id.llNeverEnd);
        LinearLayout txtOneDateEnd = dialog.findViewById(R.id.llOneDateEnd);
        txtNeverEnd.setOnClickListener(v -> {
            addEditHabitViewModel.updateSeletedTimeEnd(null);
            dialog.dismiss();
        });

        txtOneDateEnd.setOnClickListener(v -> {
            dialog.dismiss();
            final Dialog dialog1 = new Dialog(requireContext(), R.style.CustomBottomSheetDialog);
            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog1.setContentView(R.layout.dialog_date_picker);
            CalendarView datePicker = dialog1.findViewById(R.id.simpleDatePicker);
            MaterialButton btnOk = dialog1.findViewById(R.id.btnOk);
            MaterialButton btnCancel = dialog1.findViewById(R.id.btnCancel);
            int colorInt = Color.parseColor(currentColor);
            btnOk.setTextColor(colorInt);
            btnCancel.setTextColor(colorInt);
            // Đặt ngày tối thiểu là ngày mai
            Calendar minDate = Calendar.getInstance();
            minDate.add(Calendar.DAY_OF_MONTH, 1);
            minDate.set(Calendar.HOUR_OF_DAY, 0);
            minDate.set(Calendar.MINUTE, 0);
            minDate.set(Calendar.SECOND, 0);
            minDate.set(Calendar.MILLISECOND, 0);
            datePicker.setMinDate(minDate.getTimeInMillis());
            datePicker.setDate(minDate.getTimeInMillis());

            final long[] selectedDateMillis = {minDate.getTimeInMillis()};
            
            datePicker.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                selectedDateMillis[0] = calendar.getTimeInMillis();
            });



            btnCancel.setOnClickListener(v1 -> dialog1.dismiss());
            btnOk.setOnClickListener(v1 -> {
                LocalDate date = Instant.ofEpochMilli(selectedDateMillis[0])
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                
                long epochDay = date.toEpochDay();
                addEditHabitViewModel.updateSeletedTimeEnd(epochDay);
                dialog1.dismiss();
            });

            dialog1.show();
            dialog1.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog1.getWindow().setGravity(Gravity.CENTER);

        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

        private void showNumberTimeDialog(HabitType type) {
            if (binding == null) return;

            final Dialog dialog = new Dialog(requireContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_select_number_time);

            EditText edtInput = dialog.findViewById(R.id.edtNumberTime);
            TextInputLayout tilInput = dialog.findViewById(R.id.tilNumberTime);
            Button btnSave = dialog.findViewById(R.id.btnOKSelect);
            Button btnCancel = dialog.findViewById(R.id.btnCacleSelect);
            TextView txtTitle = dialog.findViewById(R.id.txtDialogInputNumber);

            edtInput.requestFocus();
            String unit = (type == HabitType.TIME) ? "số phút" : "số lần";
            txtTitle.setText("Nhập " + unit);
            int colorInt = Color.parseColor(currentColor);

            try {
                btnSave.setBackgroundColor(colorInt);
                tilInput.setBoxStrokeColor(colorInt);
                int lightColor = Color.argb(50, android.graphics.Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt));
                btnCancel.setBackgroundColor(lightColor);
                btnCancel.setTextColor(colorInt);
            } catch (Exception e) {

                btnSave.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary));
                btnCancel.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.surfaceVariant));
            }


            btnCancel.setOnClickListener(v -> dialog.dismiss());
            btnSave.setOnClickListener(v -> {
                String input = edtInput.getText().toString().trim();
                if (!input.isEmpty() && !input.equals("0")) {
                    try {
                        int value = Integer.parseInt(input);
                        if (value > 0) {
                            addEditHabitViewModel.updateTimeslected(value);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Vui lòng nhập số lớn hơn 0", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Vui lòng nhập " + unit, Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show();
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

    private void onSaveHabit() {
        if (binding == null) return;
        
        String habitName = binding.edtNameHabit.getText().toString().trim();
        if (habitName.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên thói quen", Toast.LENGTH_SHORT).show();
            return;
        }

        Integer numberSelected = addEditHabitViewModel.getTimeslected().getValue();
        if (numberSelected == null) numberSelected = 0;

        if ((habitType == HabitType.QUANTITY || habitType == HabitType.TIME) && numberSelected <= 0) {
            Toast.makeText(requireContext(), "Vui lòng chọn số lần/thời gian", Toast.LENGTH_SHORT).show();
            return;
        }
        if(habitType == HabitType.TIME && numberSelected > 0){
            numberSelected = numberSelected * 60;
        }

        Long timeEnd = addEditHabitViewModel.getSelectTimeEnd().getValue();
        boolean isSuccess = addEditHabitViewModel.saveHabit(
                habitName,
                habitType,
                currentColor,
                iconDrawable,
                numberSelected,
                timeEnd != null ? timeEnd : 0L,
                dateStart,
                binding.txtTimeRemind.getText().toString(),
                binding.switchNotification.isChecked()
        );
        if (isSuccess) {
            try {
                if (mInterstitialAd != null) {
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            mInterstitialAd = null;
                            // Không làm gì cả vì đã thoát từ trước rồi
                        }
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            mInterstitialAd = null;
                        }
                    });
                    mInterstitialAd.show(requireActivity());
                    navController.popBackStack();
                    // (Lưu ý: navController.popBackStack() sẽ được gọi trong onAdDismissedFullScreenContent)
                } else {
                    // Nếu quảng cáo chưa tải xong hoặc lỗi, thoát luôn cho nhanh
                    navController.popBackStack();
                }
            } catch (Exception e) {
                // Handle navigation error gracefully
                requireActivity().onBackPressed();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        addEditHabitViewModel.resetData();
        binding = null;
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Các phiên bản cũ hơn Android 13 không cần quyền runtime
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Người dùng đã từ chối quyền trước đó, hiển thị dialog giải thích
                showPermissionRationaleDialog();
            } else {
                // Yêu cầu quyền lần đầu
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            }
        }
    }

    private void showPermissionRationaleDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cần quyền thông báo")
                .setMessage("Ứng dụng cần quyền thông báo để nhắc nhở bạn về thói quen. Vui lòng cấp quyền trong cài đặt.")
                .setPositiveButton("Đi đến cài đặt", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền được cấp, bật switch
                binding.switchNotification.setChecked(true);
                Toast.makeText(requireContext(), "Đã bật thông báo nhắc nhở", Toast.LENGTH_SHORT).show();
            } else {
                // Quyền bị từ chối
                Toast.makeText(requireContext(), "Không thể bật thông báo nếu không có quyền", Toast.LENGTH_SHORT).show();
            }
        }
    }
}