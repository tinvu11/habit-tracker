package com.base.habittracker.ui.main;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.time.LocalDate;
public class CalendarAdapter extends FragmentStateAdapter {
    private static final int MIDDLE_POSITION = Integer.MAX_VALUE / 2;

    private final LocalDate referenceWeekStartDate;

    public CalendarAdapter(@NonNull FragmentActivity fragmentActivity, LocalDate referenceWeekStartDate) {
        super(fragmentActivity);
        this.referenceWeekStartDate = referenceWeekStartDate;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        LocalDate startDate = getStartDateForPosition(position);
        return CalendarFragment.newInstance(startDate);
    }
    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }
    public int getMiddlePosition() {
        return MIDDLE_POSITION;
    }
    public LocalDate getStartDateForPosition(int position) {
        int offset = position - MIDDLE_POSITION;
        return referenceWeekStartDate.plusWeeks(offset);
    }
}