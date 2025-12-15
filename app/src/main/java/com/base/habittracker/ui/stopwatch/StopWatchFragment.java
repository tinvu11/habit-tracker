package com.base.habittracker.ui.stopwatch;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.base.habittracker.R;
import com.base.habittracker.data.model.Habit;
import com.base.habittracker.databinding.FragmentStropWatchBinding;
import com.base.habittracker.ui.main.HabitWithStatus;

import java.time.LocalDate;
import java.util.Locale;

public class StopWatchFragment extends Fragment {

    private FragmentStropWatchBinding binding;
    private NavController navController;

    private Handler handler;
    private Runnable countUpRunnable;

    private HabitWithStatus habitST;
    private Habit habit;

    private int currentSeconds = 0;
    private StopWatchViewModel viewModel;
    private int maxTimeSeconds = 15 * 60;
    private boolean isRunning = false;
    private int primaryColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(StopWatchViewModel.class);
        if (getArguments() != null) {
            habitST = (HabitWithStatus) getArguments().getSerializable("EXTRA_HABIT_OBJ");
            habit = habitST.habit;
        }
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStropWatchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        binding.materialToolbar2.setNavigationOnClickListener(v -> navController.popBackStack());

        if (habit != null) {
            setupUI();
        }
    }

    private void setupUI() {
        binding.tvNameHabit.setText(habit.getName());
        int d = Color.parseColor(habit.getColorHex());
        binding.tvNameHabit.setTextColor(d);
        try {
            primaryColor = Color.parseColor(habit.getColorHex());
        } catch (Exception e) {
            primaryColor = Color.parseColor("#FF9800");
        }

        binding.circularProgressIndicator.setIndicatorColor(primaryColor);
        binding.btnPlayPause.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        maxTimeSeconds = habit.getTargetValue();
        currentSeconds = habitST.completedValue;
        binding.circularProgressIndicator.setMax(maxTimeSeconds);
        binding.targetTime.setText("Mục tiêu: "+ maxTimeSeconds/60 +" phút");
        updateTimerUI(currentSeconds);
        startTimer();

        binding.btnPlayPause.setOnClickListener(v -> toggleTimer());
        binding.btnExist.setOnClickListener(v -> {navController.popBackStack();});
    }

    private void toggleTimer() {
        if (!isRunning && currentSeconds < maxTimeSeconds) {
            startTimer();
        } else {
            pauseTimer();

        }
    }
    private void startTimer() {
        isRunning = true;
        binding.imgPlayPause.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause));
        countUpRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentSeconds < maxTimeSeconds) {
                    currentSeconds++;
                    updateTimerUI(currentSeconds);
                    handler.postDelayed(this, 1000);
                } else {
                    finishSession();
                }
            }
        };
        handler.post(countUpRunnable);
    }

    private void pauseTimer() {
        isRunning = false;
        binding.imgPlayPause.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play));
//        long date = LocalDate.now().toEpochDay();
//        viewModel.saveHabitTimeCompleted(habitST.habitCompleteID,habitST.habit,(int)currentSeconds,habitST.habit.getTargetValue(),date);
        if (countUpRunnable != null) {
            handler.removeCallbacks(countUpRunnable);
        }
    }
    private void playPingSound() {
        // Tạo MediaPlayer đính với file nhạc trong raw
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.ding);

        // Bắt đầu phát
        mediaPlayer.start();

        // Quan trọng: Giải phóng bộ nhớ sau khi phát xong để tránh rác bộ nhớ
        mediaPlayer.setOnCompletionListener(mp -> {
            mp.release();
        });
    }


    private void resetTimer() {
        pauseTimer();
        currentSeconds = 0;
        updateTimerUI(0);

    }

    private void finishSession() {
        pauseTimer();
        playPingSound();
        long date = LocalDate.now().toEpochDay();
        viewModel.saveHabitTimeCompleted(habitST.habitCompleteID,habitST.habit,currentSeconds,habitST.habit.getTargetValue(),date);
        navController.popBackStack();
    }


    private void updateTimerUI(long seconds) {
        binding.circularProgressIndicator.setProgressCompat((int) seconds, true);
        long minutes = seconds / 60;
        long secs = seconds % 60;
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, secs);
        binding.tvTimer.setText(timeStr);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && countUpRunnable != null) {
            handler.removeCallbacks(countUpRunnable);
        }
        long date = LocalDate.now().toEpochDay();
        viewModel.saveHabitTimeCompleted(habitST.habitCompleteID,habitST.habit,currentSeconds,habitST.habit.getTargetValue(),date);
        binding = null;
    }
}