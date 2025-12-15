package com.base.habittracker.ui.addedit;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.base.habittracker.R;
import com.base.habittracker.databinding.ItemIconPickerBinding;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

    private List<Integer> iconList;
    private Context context;
    private int iconId;
    private int selectedPosition = 0;
    private String color;
    private OnIconSelectedListener listener;


    public interface OnIconSelectedListener {
        void onIconSelected(int iconResId);
    }

    // 2. Constructor mới
    public IconAdapter(Context context, List<Integer> iconList, OnIconSelectedListener listener, int iconId, String color) {
        this.context = context;
        this.iconList = iconList;
        this.listener = listener;
        this.iconId = iconId;
        this.color = color;
        this.selectedPosition = iconList.indexOf(iconId);
    }

    public void updateSelectedColor(int color) {
        notifyItemChanged(selectedPosition);
    }

    // 3. ViewHolder với ViewBinding
    public class IconViewHolder extends RecyclerView.ViewHolder {
        private final ItemIconPickerBinding binding;
        
        public IconViewHolder(@NonNull ItemIconPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                if (clickedPosition == RecyclerView.NO_POSITION || clickedPosition == selectedPosition) {
                    return; // Không làm gì nếu click vào cái đã chọn
                }

                // Thông báo cho 2 item thay đổi
                notifyItemChanged(selectedPosition); // Bỏ chọn cái cũ
                selectedPosition = clickedPosition;
                notifyItemChanged(selectedPosition); // Chọn cái mới

                // 5. Gửi ID icon về Fragment
                if (listener != null) {
                    listener.onIconSelected(iconList.get(selectedPosition));
                }
            });
        }
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIconPickerBinding binding = ItemIconPickerBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new IconViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        int iconIdInList = iconList.get(position);
        holder.binding.imgIcon.setImageResource(iconIdInList);

        if (position == selectedPosition) {
            holder.binding.imgIcon.setColorFilter(Color.WHITE);
            int colorInt = Color.parseColor(color);
            holder.binding.cardIcon.setCardBackgroundColor(colorInt);
        } else {
            int defaultColor = ContextCompat.getColor(context, R.color.black); // Thay bằng màu của bạn
            int cardBackground = ContextCompat.getColor(context, R.color.surfaceVariant);
            holder.binding.imgIcon.setColorFilter(defaultColor);
            holder.binding.cardIcon.setCardBackgroundColor(cardBackground);
        }
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }
}