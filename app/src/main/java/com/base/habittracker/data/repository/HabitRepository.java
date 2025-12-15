package com.base.habittracker.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.base.habittracker.data.local.AppDatabase;
import com.base.habittracker.data.local.HabitCompletionDao;
import com.base.habittracker.data.local.HabitDao;
import com.base.habittracker.data.model.Habit;
import com.base.habittracker.data.model.HabitCompletion;
import com.base.habittracker.data.model.HabitType;
import com.base.habittracker.data.model.HabitWithHistory;
import com.base.habittracker.ui.main.HabitWithStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitRepository {
    private final HabitDao habitDao;
    private final LiveData<List<Habit>> allHabits;
    private final ExecutorService executorService;
    private final HabitCompletionDao habitCompletionDao;

    private static HabitRepository instance;

    private HabitRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        habitDao = db.habitDao();
        allHabits = habitDao.getAllHabits();
        habitCompletionDao = db.habitCompletionDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public Habit getHabitByIdBlocking(int id){
        return habitDao.getHabitByIdBlocking(id);
    }

    public static HabitRepository getInstance(Application application) {
        // Kiểm tra lần 1 (nhanh, không khóa)
        if (instance == null) {
            // Khóa lại để đảm bảo chỉ 1 luồng được vào
            synchronized (HabitRepository.class) {
                // Kiểm tra lại lần 2 (vì có thể 1 luồng khác đã tạo nó)
                // synchronized chỉ cho phép một luồng đi qua tại một thời điểm.
                if (instance == null) {
                    instance = new HabitRepository(application);
                }
            }
        }
        return instance;
    }

    public boolean checkHabitExist(){
        return habitDao.checkHabitExist();
    }

    public void insert(Habit habit) {
        executorService.execute(() -> habitDao.insert(habit));
    }
    public void update(Habit habit) {
        executorService.execute(() -> habitDao.update(habit));
    }
    public void delete(Habit habit) {
        executorService.execute(() -> habitDao.delete(habit));
    }

    public void insert(HabitCompletion habitCompletion) {
        executorService.execute(() -> habitCompletionDao.insert(habitCompletion));
    }
    public void update(HabitCompletion habitCompletion) {
        executorService.execute(() -> habitCompletionDao.update(habitCompletion));
    }
    public void delete(HabitCompletion habitCompletion) {
        executorService.execute(() -> habitCompletionDao.delete(habitCompletion));
    }

    public void insertOrUpdate(HabitCompletion habitCompletion) {
        executorService.execute(() -> habitCompletionDao.insertOrUpdate(habitCompletion));
    }

    public LiveData<List<HabitWithStatus>> getHabitsWithStatusForDate(Long date) {
        return habitDao.getHabitsWithStatusForDate(date);
    }
    public int getHabitCompletionToDay(Long date, int habitId) {
        return habitCompletionDao.getHabitCompletionToDay(date, habitId);
    }

    public LiveData<HabitWithHistory> getHabitWithHistory(int habitId) {
        return habitDao.getHabitWithHistory(habitId);
    }


    public void recalculateStats(Habit habit) {
        if (habit == null) return;
        
        executorService.execute(() -> {
            try {
                List<HabitCompletion> completions = habitCompletionDao.getHabitCompletionById(habit.getId());
                if (completions == null) completions = new ArrayList<>();
                
                long targetValue = habit.getTargetValue();
                int currentStreak = 0;
                int bestStreak = 0;
                long lastDay = 0;
                int totalCompleted = 0;

                for (HabitCompletion completion : completions) {
                    if (completion == null) continue;
                    
                    long today = completion.getDate();
                    if (completion.getCompletedValue() >= 1) {
                        totalCompleted++;

                        if (lastDay == 0) {
                            currentStreak = 1;
                        } else if (today == lastDay + 1) {
                            currentStreak++;
                        } else if (today > lastDay + 1) {
                            currentStreak = 1;
                        }
                        if (currentStreak > bestStreak) {
                            bestStreak = currentStreak;
                        }
                        lastDay = today;
                    } else {
                        currentStreak = 0;
                        lastDay = today;
                    }
                }
                if (LocalDate.now().toEpochDay() > lastDay + 1) {
                    currentStreak = 0;
                }
                habitDao.updateStats(habit.getId(), totalCompleted, currentStreak, bestStreak);
            } catch (Exception e) {
                Log.e("HabitRepository", "Error recalculating stats: " + e.getMessage());
            }
        });
    }
}
