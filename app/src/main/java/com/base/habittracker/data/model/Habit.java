package com.base.habittracker.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "habits")
public class Habit implements Serializable {
    @PrimaryKey(autoGenerate = true)
    int id;
    @NonNull
    String name;
    HabitType type;
    @ColumnInfo(name = "target_value")
    int targetValue;
    @ColumnInfo(name = "color_hex")
    String colorHex;
    @ColumnInfo(name = "icon_id")
    int iconId;
    @ColumnInfo(name = "reminder_time")
    @Nullable
    String reminderTime;
    @ColumnInfo(name = "start_date")
    long startDate;
    @ColumnInfo(name = "end_date")
    @Nullable
    Long endDate;
    @ColumnInfo(name = "missed_value")
    int missedValue;

    @ColumnInfo(name = "current_steak")
    int currentSteak;

    @ColumnInfo(name = "best_streak")
    int bestSteak;
    @ColumnInfo(name = "completed_count")
    int completedCount;


    public boolean isNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        isNotificationEnabled = notificationEnabled;
    }

    @ColumnInfo(name = "is_notification_enabled")
    boolean isNotificationEnabled;

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getMissedValue() {
        return missedValue;
    }

    public void setMissedValue(int missedValue) {
        this.missedValue = missedValue;
    }

    public int getCurrentSteak() {
        return currentSteak;
    }

    public void setCurrentSteak(int currentSteak) {
        this.currentSteak = currentSteak;
    }

    public int getBestSteak() {
        return bestSteak;
    }

    public void setBestSteak(int bestSteak) {
        this.bestSteak = bestSteak;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public HabitType getType() {
        return type;
    }

    public void setType(HabitType type) {
        this.type = type;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(int targetValue) {
        this.targetValue = targetValue;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public int getIconId() {
        return iconId;
    }
    public void setIconId(int iconId) {
        this.iconId = iconId;
    }
    @Nullable
    public String getReminderTime() {
        return reminderTime;
    }
    public void setReminderTime(@Nullable String reminderTime) {
        this.reminderTime = reminderTime;
    }
    public long getStartDate() {
        return startDate;
    }
    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }
    public Long getEndDate() {
        return endDate;
    }
    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Habit( @NonNull String name, HabitType type, int targetValue, String colorHex, int iconId, String reminderTime, long startDate, Long endDate, boolean isNotificationEnabled) {
        this.name = name;
        this.type = type;
        this.targetValue = targetValue;
        this.colorHex = colorHex;
        this.iconId = iconId;
        this.reminderTime = reminderTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isNotificationEnabled = isNotificationEnabled;
    }
}
