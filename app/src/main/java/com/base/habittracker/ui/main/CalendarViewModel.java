package com.base.habittracker.ui.main;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.time.LocalDate;
import java.util.Objects;

public class CalendarViewModel extends ViewModel {
    private final MutableLiveData<LocalDate> selectedDate = new MutableLiveData<>();
    private final MutableLiveData<LocalDate> visibleWeekStartDate = new MutableLiveData<>();
    public CalendarViewModel() {
        selectedDate.setValue(LocalDate.now());
    }

    public LiveData<LocalDate> getVisibleWeekStartDate() {
        return visibleWeekStartDate;
    }
    public void setVisibleWeekStartDate(LocalDate date) {
        if (!Objects.equals(visibleWeekStartDate.getValue(), date)) {
            visibleWeekStartDate.setValue(date);
        }
    }
    public void setSelectedDate(LocalDate date) {
        if (!Objects.equals(selectedDate.getValue(), date)) {
            selectedDate.setValue(date);
        }
    }
    public LiveData<LocalDate> getSelectedDate() {
        return selectedDate;
    }
}

