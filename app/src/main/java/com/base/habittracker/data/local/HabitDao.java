package com.base.habittracker.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.base.habittracker.data.model.Habit;
import com.base.habittracker.data.model.HabitWithHistory;
import com.base.habittracker.ui.main.HabitWithStatus;

import java.util.List;

@Dao
public interface HabitDao {
    @Insert
    void insert(Habit habit);
    @Update
    void update(Habit habit);
    @Delete
    void delete(Habit habit);

    @Query("UPDATE habits SET completed_count = :totalCompleted, current_steak = :currentStreak, best_streak = :bestStreak WHERE id = :habitId")
    void updateStats(int habitId,int totalCompleted, int currentStreak, int bestStreak);
    @Query("SELECT * FROM habits ORDER BY name ASC")
    LiveData<List<Habit>> getAllHabits();

    @Query("SELECT EXISTS(SELECT 1 FROM habits)")
    boolean checkHabitExist();

    @Query("SELECT * FROM habits WHERE id = :id")
    Habit getHabitByIdBlocking(int id);

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId")
    LiveData<HabitWithHistory> getHabitWithHistory(int habitId);

    @Query("SELECT h.*, " +
            "IFNULL(hc.completed_value, 0) AS completedValue, " +
            "IFNULL(hc.id, 0) AS habitCompleteID " +
            "FROM habits AS h " +
            "LEFT JOIN habit_completions AS hc " +
            "ON h.id = hc.habit_id AND hc.date = :date " +
            "ORDER BY h.id")
    LiveData<List<HabitWithStatus>> getHabitsWithStatusForDate(long date);
}
