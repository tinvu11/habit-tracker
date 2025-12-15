package com.base.habittracker.ui.settings;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import com.base.habittracker.data.repository.HabitRepository;

public class SettingViewModel extends AndroidViewModel {
    private final HabitRepository repository;

    public SettingViewModel(@NonNull Application application, HabitRepository repository) {
        super(application);
        this.repository = repository;
    }

}
