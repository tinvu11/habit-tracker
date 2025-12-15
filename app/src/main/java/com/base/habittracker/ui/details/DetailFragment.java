package com.base.habittracker.ui.details;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;

import com.base.habittracker.R;
import com.base.habittracker.data.model.Habit;
import com.base.habittracker.data.model.HabitCompletion;
import com.base.habittracker.databinding.FragmentDetailBinding;
import com.base.habittracker.databinding.ItemDetailCardBinding;
import com.base.habittracker.utils.PremiumManager;
import com.google.android.gms.ads.AdRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailFragment extends Fragment {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yy");
    private FragmentDetailBinding binding;
    private DetailViewModel detailViewModel;
    private NavController navController;
    private Habit habit;
    private int colorCode;

    private ItemDetailCardBinding cardStartBinding;
    private ItemDetailCardBinding cardFinishedBinding;
    private ItemDetailCardBinding cardBestBinding;
    private ItemDetailCardBinding cardMissBinding;
    private final int[] colors = {
            Color.parseColor("#EBEDF0"), // Level 0
            Color.parseColor("#9BE9A8"), // Level 1
            Color.parseColor("#40C463"), // Level 2
            Color.parseColor("#30A14E"), // Level 3
            Color.parseColor("#216E39")  // Level 4
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(requireActivity().getWindow(), false);
        detailViewModel = new ViewModelProvider(requireActivity()).get(DetailViewModel.class);
        if (getArguments() != null) {
            habit = (Habit) getArguments().getSerializable("EXTRA_HABIT_OBJ");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDetailBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        if (habit == null) return;

        bindIncludeCards();
        setupCardTitles();
        setupRootUI();
        setupHeatMap();
        observeHabitHistory();
        setupAds();
    }


    private void bindIncludeCards() {
        View root = binding.getRoot();
        cardStartBinding = ItemDetailCardBinding.bind(root.findViewById(R.id.cardDateStart));
        cardFinishedBinding = ItemDetailCardBinding.bind(root.findViewById(R.id.cardFinished));
        cardBestBinding = ItemDetailCardBinding.bind(root.findViewById(R.id.cardBest));
        cardMissBinding = ItemDetailCardBinding.bind(root.findViewById(R.id.cardMiss));
    }

    private void setupCardTitles() {
        cardStartBinding.lblTitle.setText("Bắt đầu");
        cardFinishedBinding.lblTitle.setText("Hoàn thành");
        cardBestBinding.lblTitle.setText("Dài nhất");
        cardMissBinding.lblTitle.setText("Bỏ lỡ");
        cardStartBinding.iconContainer.setImageResource(R.drawable.ic_calendar_day);
        cardFinishedBinding.iconContainer.setImageResource(R.drawable.ic_check);
        cardBestBinding.iconContainer.setImageResource(R.drawable.ic_arrow_trend_up);
        cardMissBinding.iconContainer.setImageResource(R.drawable.ic_cross_circle);
    }

    private void setupRootUI() {
        colorCode = Color.parseColor(habit.getColorHex());
        setGlowColor(habit.getColorHex());
        setBaseColor(colorCode);
        binding.txtCurrentSteak.setTextColor(colorCode);
        binding.txtTitleCurrent.setTextColor(colorCode);
        binding.cviewLv1.setCardBackgroundColor(colors[0]);
        binding.cviewLv2.setCardBackgroundColor(colors[1]);
        binding.cviewLv3.setCardBackgroundColor(colors[2]);
        binding.cviewLv4.setCardBackgroundColor(colors[3]);
        binding.txtCurrentSteak.setText(String.valueOf(habit.getCurrentSteak()));
        binding.txtNameHabit.setText(habit.getName());
        binding.materialToolbar.setNavigationOnClickListener(v -> navController.popBackStack());
        cardBestBinding.valContent.setText(String.valueOf(habit.getBestSteak()));
        cardFinishedBinding.valContent.setText(String.valueOf(habit.getCompletedCount()));
        cardStartBinding.valContent.setText(convertEpochDayToDate(habit.getStartDate()));


    }

    private void setupHeatMap() {
        int baseColor = Color.parseColor(habit.getColorHex());
        binding.heatMapView.setBaseColor(baseColor, ContextCompat.getColor(requireContext(), R.color.itemBottomSheet));
        LocalDate mapStartDate = LocalDate.ofEpochDay(habit.getStartDate());
        LocalDate mapEndDate = LocalDate.now().plusMonths(12);
        binding.heatMapView.setDateRange(mapStartDate, mapEndDate);
    }

    private void observeHabitHistory() {
        detailViewModel.getHabitHistory()
                .observe(getViewLifecycleOwner(), historyList -> {
                    Map<String, Integer> dataMap = new HashMap<>();
                    if (historyList != null && historyList.history != null) {
                        for (HabitCompletion completion : historyList.history) {
                            LocalDate date = LocalDate.ofEpochDay(completion.getDate());
                            int completed = completion.getCompletedValue();
                            int targetAtThatTime = habit.getTargetValue();
                            if (targetAtThatTime <= 0) targetAtThatTime = 1;
                            int level = calculateLevel(completed, targetAtThatTime);
                            dataMap.put(date.toString(), level);
                        }
                        habit = historyList.habit;
                    }
                    binding.heatMapView.setContributionData(dataMap);
                    Long endEpochDay = habit.getEndDate();
                    binding.heatMapView.setHabitEndDate(endEpochDay);
                    calculateAndShowMissedDays(habit, historyList.history, LocalDate.now() );
                });
    }

    private void setupAds() {
        PremiumManager.getInstance(requireContext())
                .getPremiumStatus()
                .observe(getViewLifecycleOwner(), isPremium -> {
                    if (Boolean.TRUE.equals(isPremium)) {
                        binding.adView.setVisibility(View.GONE);
                        binding.adView.destroy();
                    } else {
                        binding.adView.setVisibility(View.VISIBLE);
                        AdRequest adRequest = new AdRequest.Builder().build();
                        binding.adView.loadAd(adRequest);
                    }
                });
    }

    private int calculateLevel(int completed, int target) {
        if (completed <= 0) return 0;
        float percentage = (float) completed / target;
        if (percentage <= 0.25f) return 1;
        if (percentage <= 0.50f) return 2;
        if (percentage <= 0.75f) return 3;
        return 4;
    }
    public void setBaseColor(int color) {
        colors[0] = ColorUtils.setAlphaComponent(color, 80);
        colors[1] = ColorUtils.setAlphaComponent(color, 140);
        colors[2] = ColorUtils.setAlphaComponent(color, 200);
        colors[3] = color;
    }

    private void calculateAndShowMissedDays(
            Habit habit,
            List<HabitCompletion> completions,
            LocalDate todayDate
    ) {
        LocalDate startDate = LocalDate.ofEpochDay(habit.getStartDate());
        LocalDate endDate = (habit.getEndDate() != null && habit.getEndDate() != 0)
                ? LocalDate.ofEpochDay(habit.getEndDate())
                : null;

        // Xác định khoảng thời gian cần tính miss
        LocalDate rangeEnd;
        if (endDate != null && endDate.isBefore(todayDate)) {
            // Habit đã kết thúc -> tính từ start đến end
            rangeEnd = endDate;
        } else {
            // Habit đang chạy -> tính từ start đến HÔM QUA
            rangeEnd = todayDate.minusDays(1);
        }

        if (rangeEnd.isBefore(startDate)) {
            // Chưa có ngày nào trôi qua -> miss = 0
            cardMissBinding.valContent.setText("0");
            return;
        }

        long startEpoch = startDate.toEpochDay();
        long rangeEndEpoch = rangeEnd.toEpochDay();

        // Nếu mỗi ngày chỉ có 1 record:
        // -> chỉ cần đếm số ngày trong khoảng có completion "đạt target"
        int targetValue = habit.getTargetValue();  // nếu có kiểu time/quantity thì check đủ/thiếu ở đây
        int successDays = 0;

        for (HabitCompletion c : completions) {
            long d = c.getDate();
            if (d < startEpoch || d > rangeEndEpoch) continue; // bỏ ngoài range

            int completed = c.getCompletedValue();
            int targetAtThatTime = c.getTargetCompleteValue(); // TODO: nếu mỗi ngày target khác thì lấy từ c

            if (completed >= targetAtThatTime) {
                successDays++;
            }
        }

        long targetDaysCount = ChronoUnit.DAYS.between(startDate, rangeEnd) + 1; // inclusive
        int missedDays = (int) Math.max(0, targetDaysCount - successDays);
        cardMissBinding.valContent.setText(String.valueOf(missedDays));
    }


    // endregion
    // region Helpers
    private void setGlowColor(String colorHex) {
        if (binding == null) return;

        try {
            // 1. Lấy màu gốc (Màu đậm nhất ở trên cùng)
            int baseColor = Color.parseColor(colorHex);

            // 2. Tạo mảng màu cho Gradient (Từ Đậm -> Trong suốt)
            // Bạn có thể dùng 2 màu hoặc 3 màu tùy thích độ mượt
//            int[] colors = new int[]{
//                    baseColor,            // Bắt đầu: Màu gốc đậm đặc (trên cùng)
//                    Color.TRANSPARENT     // Kết thúc: Trong suốt (dưới cùng)
//            };

            // --- (Tùy chọn) Nếu muốn mượt hơn thì dùng 3 màu ---
             int midColor = ColorUtils.setAlphaComponent(baseColor, 100); // Màu ở giữa hơi mờ
             int[] colors = new int[]{ baseColor, midColor, Color.TRANSPARENT };
            // ----------------------------------------------------


            // 3. Khởi tạo GradientDrawable với hướng từ TRÊN XUỐNG DƯỚI
            GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM, // Hướng quan trọng nhất
                    colors
            );

            // 4. Đặt hình dạng là hình chữ nhật
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            // Với linear gradient, ta KHÔNG CẦN setGradientRadius hay setGradientCenter nữa.

            // 5. Áp dụng vào View
            binding.viewGlow.setBackground(gradientDrawable);

        } catch (Exception e) {
            // Fallback nếu có lỗi màu sắc
            if (binding.viewGlow != null) {
                // Đảm bảo file xml này đã được cập nhật theo Bước 1
                binding.viewGlow.setBackgroundResource(R.drawable.bg_fire_glow);
            }
        }
    }

    private String convertEpochDayToDate(long epochDay) {
        if (epochDay == 0L) return "Hôm nay";
        LocalDate date = LocalDate.ofEpochDay(epochDay);
        return date.format(DATE_FORMATTER);
    }

    // endregion
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
