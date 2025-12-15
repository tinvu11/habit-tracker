package com.base.habittracker.ui.main;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.base.habittracker.data.model.Habit;
import com.base.habittracker.data.model.HabitCompletion;
import com.base.habittracker.data.repository.HabitRepository;
import java.time.LocalDate;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final HabitRepository repository;
    private LiveData<List<HabitWithStatus>> roomSource;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = HabitRepository.getInstance(application);
        loadDataForToday();
    }
    public LiveData<List<HabitWithStatus>> getHabitsWithStatus() {
        return roomSource;
    }
    private void loadDataForToday() {
        long today = LocalDate.now().toEpochDay();
        roomSource = repository.getHabitsWithStatusForDate(today);
    }
    public void deleteHabit(Habit habit) {
        repository.delete(habit);
    }

    @Deprecated
    boolean checkHabitExist(){
        return repository.checkHabitExist();
    }
    
    public void toggleTaskCompletion(HabitWithStatus item) {
        long today = LocalDate.now().toEpochDay();
        if (item.completedValue >= item.habit.getTargetValue()) {
            HabitCompletion completion = new HabitCompletion(item.habitCompleteID, item.habit.getId(), today, item.completedValue, item.habit.getTargetValue());
            repository.delete(completion);
            repository.recalculateStats(item.habit);
        } else {
            HabitCompletion completion = new HabitCompletion(0, item.habit.getId(), today, 1, item.habit.getTargetValue());
            repository.insert(completion);
            repository.recalculateStats(item.habit);
        }
    }

    public void incrementQuantity(HabitWithStatus item) {
        if(item.completedValue < item.habit.getTargetValue()) {
            long today = LocalDate.now().toEpochDay();
            int newValue = item.completedValue + 1;
            HabitCompletion completion = new HabitCompletion(item.habitCompleteID, item.habit.getId(), today, newValue, item.habit.getTargetValue());
            repository.insertOrUpdate(completion);
            repository.recalculateStats(item.habit);

        }
    }

}