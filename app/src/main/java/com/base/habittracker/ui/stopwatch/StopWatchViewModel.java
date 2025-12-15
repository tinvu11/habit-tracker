package com.base.habittracker.ui.stopwatch;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.base.habittracker.data.model.Habit;
import com.base.habittracker.data.model.HabitCompletion;
import com.base.habittracker.data.repository.HabitRepository;

public class StopWatchViewModel  extends AndroidViewModel {

    private HabitRepository repository;

    public StopWatchViewModel(@NonNull Application application) {
        super(application);
        repository =  HabitRepository.getInstance(application);
    }

    public void saveHabitTimeCompleted(int habitCompleteID,Habit habit, int completedValue,int getTargetValue, long today){
        HabitCompletion completion = new HabitCompletion(
                habitCompleteID,
                habit.getId(),
                today,
                completedValue,
                getTargetValue
        );
        repository.insertOrUpdate(completion);
        repository.recalculateStats(habit);
    }
}
