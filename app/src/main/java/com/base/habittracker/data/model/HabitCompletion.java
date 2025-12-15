package com.base.habittracker.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_completions",
        indices = {@Index(value = {"habit_id", "date"}, unique = true)},
        foreignKeys = @ForeignKey(entity = Habit.class,
                parentColumns = "id",
                childColumns = "habit_id",
                onDelete = ForeignKey.CASCADE))
public class HabitCompletion {
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "habit_id", index = true)
    int habitId;
    long date;
    @ColumnInfo(name = "completed_value")
    int completedValue;
    @ColumnInfo(name = "target_complete_value")
    int targetCompleteValue;

    public int getTargetCompleteValue() {
        return targetCompleteValue;
    }

    public void setTargetCompleteValue(int targetCompleteValue) {
        this.targetCompleteValue = targetCompleteValue;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHabitId() {
        return habitId;
    }

    public void setHabitId(int habitId) {
        this.habitId = habitId;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getCompletedValue() {
        return completedValue;
    }

    public HabitCompletion(int id , int habitId, long date, int completedValue, int targetCompleteValue) {
        this.id = id;
        this.habitId = habitId;
        this.date = date;
        this.completedValue = completedValue;
        this.targetCompleteValue = targetCompleteValue;
    }
}
