package com.base.habittracker.data.local;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.base.habittracker.data.model.HabitCompletion;
import java.util.List;

@Dao
public interface HabitCompletionDao {
    @Insert
    void insert(HabitCompletion habitCompletion);
    @Update
    void update(HabitCompletion habitCompletion);
    @Delete
    void delete(HabitCompletion habitCompletion);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdate(HabitCompletion HabitCompletion);
    @Query("SELECT * FROM habit_completions")
    LiveData<List<HabitCompletion>> getAllHabitCompletions();
    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId")
    List<HabitCompletion> getHabitCompletionById(int habitId);
    @Query("SELECT Count(*) FROM habit_completions WHERE habit_id = :habitId AND date = :date")
    int getHabitCompletionToDay(Long date, int habitId);



}
