package com.base.habittracker.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.base.habittracker.data.model.Habit;
import com.base.habittracker.data.model.HabitCompletion;

@Database(entities = {Habit.class, HabitCompletion.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HabitDao habitDao();
    public abstract HabitCompletionDao habitCompletionDao();
    private static volatile AppDatabase INSTANCE;
    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                            .build();
                }
            }
        }
        return  INSTANCE;
    }
}
