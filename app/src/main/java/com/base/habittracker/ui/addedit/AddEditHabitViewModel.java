package com.base.habittracker.ui.addedit;
import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.base.habittracker.data.model.Habit;
import com.base.habittracker.data.model.HabitType;
import com.base.habittracker.data.repository.HabitRepository;
import com.base.habittracker.utils.HabitScheduler;

public class AddEditHabitViewModel extends AndroidViewModel {
    private final HabitRepository repository;
    private LiveData<Habit> currentHabit;
    private MutableLiveData<Integer> timeslected = new MutableLiveData<>(1);
    private MutableLiveData<String> selectedColor = new MutableLiveData<>("#FF9800");

    private MutableLiveData<Integer> iconResId = new MutableLiveData<>(-1);
    private int currentHabitId = -1;

    private final MutableLiveData<Long> selectTimeEnd = new MutableLiveData<>(null);

    public AddEditHabitViewModel(@NonNull Application application) {
        super(application);
        repository = HabitRepository.getInstance(application);
    }

    public void setCurrentHabit(Habit habit) {
        this.currentHabit = new MutableLiveData<>(habit);
    }

    public void setCurrentHabitID(int habitId) {
        this.currentHabitId = habitId;
    }

    public boolean saveHabit( String habitName, HabitType habitType, String color, int iconResId, int numberSlected, Long dateEnd, Long dateStart, String reminderTime, boolean isNotificationEnabled) {
        if (habitName.trim().isEmpty()) {
            return false;
        }
        if (currentHabitId == -1) {
            Habit newHabit = new Habit(habitName, habitType, numberSlected, color, iconResId, reminderTime, dateStart, dateEnd, isNotificationEnabled);
            repository.insert(newHabit);

            Context context = getApplication().getApplicationContext();
            if (newHabit.getReminderTime() != null && !newHabit.getReminderTime().isEmpty()) {
                HabitScheduler.scheduleReminder(context, newHabit);
            } else {
                HabitScheduler.cancelReminder(context, newHabit.getId());
            }
        } else {
            Habit oldHabit = currentHabit.getValue();
            if (oldHabit != null) {
                oldHabit.setName(habitName);
                oldHabit.setType(habitType);
                oldHabit.setTargetValue(numberSlected);
                oldHabit.setColorHex(color);
                oldHabit.setIconId(iconResId);
                oldHabit.setReminderTime(reminderTime);
                oldHabit.setStartDate(dateStart);
                oldHabit.setEndDate(dateEnd);
                oldHabit.setNotificationEnabled(isNotificationEnabled);
                repository.update(oldHabit);
                Context context = getApplication().getApplicationContext();
                if (oldHabit.getReminderTime() != null && !oldHabit.getReminderTime().isEmpty()) {
                    HabitScheduler.scheduleReminder(context, oldHabit);
                } else {
                    HabitScheduler.cancelReminder(context, oldHabit.getId());
                }
            }
        }
        return true;
    }


    public void updateSelectedColor(String color) {
        selectedColor.setValue(color);
    }
    public LiveData<String> getSelectedColor() {
        return selectedColor;
    }
    public void updateTimeslected(int times) {
        timeslected.setValue(times);
    }
    public LiveData<Integer> getTimeslected() {
        return timeslected;
    }
    public void updateSelectedIcon(int iconRes) {
        iconResId.setValue(iconRes);
    }
    public LiveData<Integer> getSelectedIcon() {
        return iconResId;
    }
    public void updateSeletedTimeEnd(Long date) {
        selectTimeEnd.setValue(date);
    }
    public LiveData<Long> getSelectTimeEnd() {
        return selectTimeEnd;
    }

    public void update(Habit habit) {
        repository.update(habit);
    }

    public void resetData() {
        timeslected.setValue(1);
        selectedColor.setValue("#FF9800");
        iconResId.setValue(-1);
        currentHabitId = -1;
        selectTimeEnd.setValue(null);
        currentHabit = null;
    }

}
