package com.base.habittracker.ui.main;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.base.habittracker.R;
import com.base.habittracker.databinding.FragmentCalendarBinding;
import com.base.habittracker.databinding.ItemDayOfWeekBinding;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


public class CalendarFragment extends Fragment {
    private static final String ARG_EPOCH = "start_epoch_day";
    private FragmentCalendarBinding binding;
    private LocalDate startDate;
    private CalendarViewModel viewModel;
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd");

    public static CalendarFragment newInstance(LocalDate startDate) {
        Bundle args = new Bundle();
        args.putLong(ARG_EPOCH, startDate.toEpochDay());
        CalendarFragment f = new CalendarFragment();
        f.setArguments(args);
        return f;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long epoch = requireArguments().getLong(ARG_EPOCH);
        startDate = LocalDate.ofEpochDay(epoch);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);

        List<ItemDayOfWeekBinding> days = Arrays.asList(
                binding.td1, binding.td2, binding.td3, binding.td4, binding.td5, binding.td6, binding.td7
        );

        for (int i = 0; i < 7; i++) {
            LocalDate d = startDate.plusDays(i);
            ItemDayOfWeekBinding it = days.get(i);

            it.txtTh.setText(getDayName(d.getDayOfWeek()));
            it.txtDayOfMonth.setText(d.format(DAY_FMT));

            final LocalDate clickedDay = d; // hiệu lực final cho lambda
            it.getRoot().setOnClickListener(v -> viewModel.setSelectedDate(clickedDay));
        }

        // Chỉ set selected/normal (màu do selector lo)
        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), sel -> {
            for (int i = 0; i < 7; i++) {
                LocalDate d = startDate.plusDays(i);
                boolean isSel = d.equals(sel);
                ItemDayOfWeekBinding it = days.get(i);
                it.itemday.setSelected(isSel);
                it.txtDayOfMonth.setSelected(isSel);
                int color = ContextCompat.getColor(requireContext(), R.color.primary);
                it.txtDayOfMonth.setTextColor(isSel? color : ContextCompat.getColor(requireContext(), R.color.onPrimary));
                it.txtTh.setSelected(isSel);
                it.txtTh.setTextColor(isSel ? ContextCompat.getColor(requireContext(), R.color.white): ContextCompat.getColor(requireContext(), R.color.onSurface));
            }
        });

        return binding.getRoot();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null; // chống leak
    }

    private String getDayName(DayOfWeek dow) {
        switch (dow) {
            case MONDAY: return "Th 2";
            case TUESDAY: return "Th 3";
            case WEDNESDAY: return "Th 4";
            case THURSDAY: return "Th 5";
            case FRIDAY: return "Th 6";
            case SATURDAY: return "Th 7";
            case SUNDAY: return "CN";
            default: return "";
        }
    }
}
