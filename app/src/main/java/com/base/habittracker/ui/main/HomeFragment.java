package com.base.habittracker.ui.main;
import static android.view.View.GONE;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import com.base.habittracker.R;
import com.base.habittracker.data.model.HabitType;
import com.base.habittracker.databinding.FragmentHomeBinding;
import com.base.habittracker.ui.details.DetailViewModel;
import com.base.habittracker.utils.PremiumManager;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private CalendarViewModel viewModel;
    private MainViewModel mv;
    private DetailViewModel detailViewModel;
    private HabitAdapter habitAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PremiumManager.getInstance(requireContext()).getPremiumStatus().observe(getViewLifecycleOwner(), isPremium -> {
            if (isPremium) {
              binding.imgPremium.setVisibility(GONE);
            }
        });
        viewModel = new ViewModelProvider(requireActivity()).get(CalendarViewModel.class);
        mv = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        detailViewModel = new ViewModelProvider(requireActivity()).get(DetailViewModel.class);



        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);

        CalendarAdapter adapter = new CalendarAdapter(requireActivity(), thisWeekStart);
        binding.pageview2.setAdapter(adapter);
        binding.pageview2.setCurrentItem(adapter.getMiddlePosition(), false);

        viewModel.setVisibleWeekStartDate(thisWeekStart);

        binding.btnAddHabit.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);

            navController.navigate(R.id.action_home_to_addEditHabit, null, null, null);
        });
        binding.btnSettings.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.settings);
        });

        viewModel.getVisibleWeekStartDate().observe(getViewLifecycleOwner(), weekStartDate -> {
            if (weekStartDate == null) return;
            LocalDate todayObs = LocalDate.now();
            LocalDate weekEndDate = weekStartDate.plusDays(6);
            boolean isThisWeekVisible = !todayObs.isBefore(weekStartDate) && !todayObs.isAfter(weekEndDate);
            var d = viewModel.getSelectedDate().getValue();
            if (isThisWeekVisible && d != null && d.isEqual(todayObs)) {
                binding.today.setText("");
            } else {
                binding.today.setText("Hôm nay");
            }
            String monthText = "Tháng " + weekEndDate.getMonthValue() +  " " + weekEndDate.getYear();
            binding.month.setText(monthText);
        });

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), selectedDate -> {
            if (selectedDate != null) {
                if (selectedDate.isEqual(LocalDate.now())) {
                    binding.today.setText("");
                } else {
                    binding.today.setText("Hôm nay");
                }
            }
        });

        binding.imgPremium.setOnClickListener(v -> {
            showPremiumDialog();
        });


        binding.today.setOnClickListener(v -> {
            viewModel.setSelectedDate(LocalDate.now());
            binding.pageview2.setCurrentItem(adapter.getMiddlePosition(), true);
        });
        binding.pageview2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                LocalDate newWeekStart = adapter.getStartDateForPosition(position);
                viewModel.setVisibleWeekStartDate(newWeekStart);
            }
        });
        NavController navController = Navigation.findNavController(view);
        habitAdapter = new HabitAdapter(
                habitST -> {
                    detailViewModel.loadHabitWithHistory(habitST.habit.getId());
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("EXTRA_HABIT_OBJ",habitST.habit);
                    navController.navigate(R.id.action_home_to_detail, bundle);
                },

                habitST -> {
                    HabitType type = habitST.habit.getType();
                    if (type == HabitType.TASK) {
                        mv.toggleTaskCompletion(habitST);
                    } else if (type == HabitType.QUANTITY) {
                        mv.incrementQuantity(habitST);
                    } else if (type == HabitType.TIME) {
                        if(habitST.completedValue >= habitST.habit.getTargetValue()){
                            return;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("EXTRA_HABIT_OBJ",habitST);
                        navController.navigate(R.id.action_home_to_stropWatchFragment, bundle);
                    }
                },

                habitST -> {
                    showOptionsDialog(habitST);
                }
        );
        binding.listItem.setAdapter(habitAdapter);

        mv.getHabitsWithStatus().observe(getViewLifecycleOwner(), habits -> {
             habitAdapter.updateData(habits);
             if (habits == null || habits.isEmpty()) {
                 // TODO: Show empty view
                  binding.ImageEmpty.setVisibility(View.VISIBLE);
                  binding.listItem.setVisibility(View.GONE);
             } else {
                  binding.ImageEmpty.setVisibility(View.GONE);
                  binding.listItem.setVisibility(View.VISIBLE);
             }
        });
    }



    private void showOptionsDialog(HabitWithStatus habit) {
        final Dialog dialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheet_options);

        TextView txtEdit = dialog.findViewById(R.id.txtEdit);
        TextView txtDelete = dialog.findViewById(R.id.txtDelete);

        txtEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("EXTRA_HABIT_OBJ", habit.habit);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_home_to_addEditHabit, bundle);
            dialog.dismiss();
        });

        txtDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmationDialog(habit);
        });

        showDialog(dialog);
    }

    private void showDeleteConfirmationDialog(HabitWithStatus habit) {
        final Dialog deleteDialog = new Dialog(requireContext(), R.style.CustomBottomSheetDialog);
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deleteDialog.setContentView(R.layout.bottom_sheet_delete);

        View viewDelete = deleteDialog.findViewById(R.id.viewDelete);
        View viewCancel = deleteDialog.findViewById(R.id.viewCancel);

        viewDelete.setOnClickListener(v -> {
            mv.deleteHabit(habit.habit);
            deleteDialog.dismiss();
        });

        viewCancel.setOnClickListener(v -> deleteDialog.dismiss());

        showDialog(deleteDialog);
    }

    private void showDialog(Dialog dialog) {
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void showPremiumDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_premium, null);
        Dialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();
        setupPremiumDialogClickListeners(dialogView, dialog);
        dialog.show();
    }

    private void setupPremiumDialogClickListeners(View dialogView, Dialog dialog) {
        // Click vào gói 1 tháng
        dialogView.findViewById(R.id.cvMonthNoAds).setOnClickListener(v -> {
            // Xử lý logic mua gói 1 tháng
            // TODO: Implement monthly subscription logic
            dialog.dismiss();
        });

        // Click vào gói trọn đời
        dialogView.findViewById(R.id.cvLifetimeNoAds).setOnClickListener(v -> {
            // Xử lý logic mua gói trọn đời
            // TODO: Implement lifetime subscription logic
            dialog.dismiss();
        });

        // Click vào restore purchase
        dialogView.findViewById(R.id.tvRestore).setOnClickListener(v -> {
            // Xử lý logic khôi phục giao dịch
            // TODO: Implement restore purchase logic
            dialog.dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}