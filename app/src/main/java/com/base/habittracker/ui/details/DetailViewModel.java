package com.base.habittracker.ui.details;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.base.habittracker.data.model.HabitWithHistory;
import com.base.habittracker.data.repository.HabitRepository;

public class DetailViewModel extends AndroidViewModel {
    private final HabitRepository repository;
    private LiveData<HabitWithHistory> data;
    public DetailViewModel(@NonNull Application application) {
        super(application);
        repository = HabitRepository.getInstance(application);
    }
    public LiveData<HabitWithHistory> getHabitHistory() {
        return data;
    }
    public void loadHabitWithHistory(int habitId) {
        data = repository.getHabitWithHistory(habitId);
    }
}
