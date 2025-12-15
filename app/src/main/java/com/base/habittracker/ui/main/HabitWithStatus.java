package com.base.habittracker.ui.main;
import androidx.room.Embedded;
import androidx.room.Ignore;

import com.base.habittracker.data.model.Habit;

import java.io.Serializable;


public class HabitWithStatus implements Serializable {
    @Embedded
    public Habit habit;
    public int habitCompleteID;
    public int completedValue;
    @Ignore
    public boolean isTimerRunning = false;
    public HabitWithStatus() {}


}

