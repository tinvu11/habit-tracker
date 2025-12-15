package com.base.habittracker.ui.main;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.base.habittracker.R;
import com.base.habittracker.data.model.Habit;
import com.base.habittracker.data.model.HabitType;
import com.base.habittracker.databinding.HabitItemBinding;
// import com.base.habittracker.data.model.HabitWithStatus; // Đảm bảo import đúng class này

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    // SỬA 1: Khởi tạo list ngay lập tức để tránh NullPointerException
    private List<HabitWithStatus> habitST = new ArrayList<>();

    private final OnHabitClickListener clickListener;
    private final OnCheckBoxClickListener checkBoxListener;
    private final OnShowOptionsDialog showOptionsListener;

    public interface OnHabitClickListener {
        void onHabitClick(HabitWithStatus habit);
    }

    public interface OnCheckBoxClickListener {
        void onCheckBoxClick(HabitWithStatus habit);
    }

    public interface OnShowOptionsDialog {
        void onShowOptions(HabitWithStatus habit);
    }
    public void updateData(List<HabitWithStatus> listItem) {
        this.habitST = listItem;
        notifyDataSetChanged();
    }


    public HabitAdapter(OnHabitClickListener listener,
                        OnCheckBoxClickListener listenerCheckBox,
                        OnShowOptionsDialog showOptionsListener) {
        this.clickListener = listener;
        this.checkBoxListener = listenerCheckBox;
        this.showOptionsListener = showOptionsListener;
    }

    // SỬA 2: Thêm hàm cập nhật dữ liệu
    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<HabitWithStatus> newList) {
        if (newList != null) {
            this.habitST = newList;
            notifyDataSetChanged(); // Cập nhật lại toàn bộ giao diện
        }
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HabitItemBinding binding = HabitItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HabitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        // SỬA 3: Lấy item từ list thay vì gọi hàm getItem()
        holder.bind(habitST.get(position));
    }

    @Override
    public int getItemCount() {
        return habitST.size();
    }

    public class HabitViewHolder extends RecyclerView.ViewHolder {
        private final HabitItemBinding binding;

        public HabitViewHolder(@NonNull HabitItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && clickListener != null) {
                    // Lấy item từ list
                    clickListener.onHabitClick(habitST.get(position));
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && showOptionsListener != null) {
                    showOptionsListener.onShowOptions(habitST.get(position));
                    return true;
                }
                return false;
            });

            binding.habitCheckbox.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && checkBoxListener != null) {
                    checkBoxListener.onCheckBoxClick(habitST.get(position));
                }
            });
        }

        public void bind(HabitWithStatus item) {
            Habit habit = item.habit;

            // An toàn khi load icon
            try {
                binding.imgIconMain.setImageResource(habit.getIconId());
            } catch (Exception e) {
                binding.imgIconMain.setImageResource(R.drawable.ic_launcher_foreground);
            }

            binding.txthabitMain.setText(habit.getName());
            boolean isExpired = isHabitExpired(habit);

            // An toàn khi parse màu
            int baseColor;
            try {
                baseColor = Color.parseColor(habit.getColorHex());
            } catch (IllegalArgumentException | NullPointerException e) {
                baseColor = Color.GRAY;
            }

            int lighterColor = ColorUtils.blendARGB(baseColor, Color.WHITE, 0.45f);

            if (isExpired) {
                int washedOutColor = ColorUtils.blendARGB(lighterColor, Color.WHITE, 0.5f);
                int fadedBackgroundColor = ColorUtils.setAlphaComponent(washedOutColor, (int)(255 * 0.7));
                int fadedIconColor = ColorUtils.setAlphaComponent(baseColor, (int)(255 * 0.3));

                binding.habitItem.setCardBackgroundColor(ColorStateList.valueOf(fadedBackgroundColor));
                binding.cardIconMain.setCardBackgroundColor(ColorStateList.valueOf(fadedIconColor));
                binding.imgIconMain.setColorFilter(ColorUtils.setAlphaComponent(Color.WHITE, (int)(255 * 0.7)), PorterDuff.Mode.SRC_IN);
                binding.habitCheckbox.setVisibility(View.GONE);
                binding.imgCheckedHabit.setVisibility(View.GONE);
                binding.txtStatusHabit.setTextColor(Color.GRAY);
                binding.txthabitMain.setTextColor(Color.GRAY);
                binding.txtStatusHabit.setText("Đã hoàn thành thói quen");
            } else {
                binding.habitItem.setCardBackgroundColor(ColorStateList.valueOf(lighterColor));
                binding.cardIconMain.setCardBackgroundColor(ColorStateList.valueOf(baseColor));
                binding.imgIconMain.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                binding.habitCheckbox.setVisibility(View.VISIBLE);
                binding.habitCheckbox.setStrokeColor(baseColor);
                binding.imgCheckedHabit.setVisibility(View.VISIBLE);
                binding.txtStatusHabit.setTextColor(Color.BLACK);
                binding.txthabitMain.setTextColor(Color.BLACK);
                int currentVal = item.completedValue;
                int targetVal = habit.getTargetValue();
                boolean isFinished = currentVal >= targetVal;

                binding.imgCheckedHabit.setColorFilter(baseColor);
                if (isFinished) {
                    binding.imgCheckedHabit.setImageResource(R.drawable.ic_check);
                    if (habit.getType() == HabitType.TASK) {
                        binding.txtStatusHabit.setText("Đã hoàn thành");
                    } else if (habit.getType() == HabitType.TIME) {
                        updateCountUpText(currentVal, habit.getTargetValue());
                    } else {
                        binding.txtStatusHabit.setText(currentVal + " / " + habit.getTargetValue() + " lần");
                    }

                } else {
                    switch (habit.getType()) {
                        case TASK:
                            binding.imgCheckedHabit.setImageResource(0);
                            binding.txtStatusHabit.setText("Chưa hoàn thành");
                            break;

                        case TIME:
                            updateCountUpText(currentVal, habit.getTargetValue());
                            binding.imgCheckedHabit.setImageResource(R.drawable.ic_play);
                            break;
                        case QUANTITY:
                            binding.txtStatusHabit.setText(currentVal + " / " + habit.getTargetValue() + " lần");
                            binding.imgCheckedHabit.setImageResource(R.drawable.ic_add);
                            break;
                    }
                }
            }
        }

        private boolean isHabitExpired(Habit habit) {
            if (habit.getEndDate() == null || habit.getEndDate() == 0L) {
                return false;
            }
            return LocalDate.now().toEpochDay() > habit.getEndDate();
        }

        private void updateCountUpText(int secondsElapsed, int targetMillis) {
            int minutes = secondsElapsed / 60;
            int seconds = secondsElapsed % 60;
            String formatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            int targetMinutes = targetMillis / 60;
            binding.txtStatusHabit.setText(formatted + " / " + targetMinutes + ":00");
        }
    }
}