package com.base.habittracker.ui.addedit;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.base.habittracker.databinding.ItemColorBinding;
import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {
    private List<String> colorItems;
    private OnColorClickListener clickListener;
    public interface OnColorClickListener {
        void onColorClick(String color);
    }


    // --- THAY ĐỔI 1: Thêm biến để lưu vị trí đang được chọn ---
    // Khởi tạo là -1 (RecyclerView.NO_POSITION) nghĩa là chưa chọn gì
    private int selectedPosition = RecyclerView.NO_POSITION;
    private String colorSelected;


    ColorAdapter(List<String> colorItems, String colorSelected , OnColorClickListener listener) {
        this.colorItems = colorItems;
        this.colorSelected = colorSelected;
        this.clickListener = listener;
        if(!colorSelected.isEmpty()){
            this.selectedPosition = colorItems.indexOf(colorSelected);
        }

    }

    public class ColorViewHolder extends RecyclerView.ViewHolder {
        private final ItemColorBinding binding;

        public ColorViewHolder(@NonNull ItemColorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.viewColor.setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                if (clickedPosition == RecyclerView.NO_POSITION) {
                    return;
                }
                    notifyItemChanged(selectedPosition);
                    selectedPosition = clickedPosition;
                    clickListener.onColorClick(colorItems.get(clickedPosition));
                    notifyItemChanged(selectedPosition);
            });
         }
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemColorBinding binding = ItemColorBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new ColorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        String color = colorItems.get(position);
        int dynamicColor = Color.parseColor(color);
        holder.binding.viewColor.setBackgroundResource(com.base.habittracker.R.drawable.circle_selector);
        Drawable backgroundDrawable = holder.binding.viewColor.getBackground();
        Drawable wrappedDrawable = DrawableCompat.wrap(backgroundDrawable).mutate();
        DrawableCompat.setTint(wrappedDrawable, dynamicColor);
        holder.binding.viewColor.setBackground(wrappedDrawable);
        holder.binding.viewColor.setSelected(selectedPosition == position);
    }


    @Override
    public int getItemCount() {
        return colorItems.size();
    }
    public String getSelectedColor() {
        if (selectedPosition != RecyclerView.NO_POSITION) {
            return colorItems.get(selectedPosition);
        }
        return null;
    }

    public void setSelection(String colorHex) {

        int newPosition = colorItems.indexOf(colorHex);

        if (newPosition == -1 || newPosition == selectedPosition) {
            return;
        }

        notifyItemChanged(selectedPosition);
        selectedPosition = newPosition;
        notifyItemChanged(selectedPosition);
    }
}