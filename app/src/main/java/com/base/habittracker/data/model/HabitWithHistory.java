package com.base.habittracker.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.io.Serializable;
import java.util.List;

public class HabitWithHistory  {
    @Embedded
    public Habit habit;
    @Relation(
            parentColumn = "id",
            entityColumn = "habit_id"
    )
    public List<HabitCompletion> history;
}
