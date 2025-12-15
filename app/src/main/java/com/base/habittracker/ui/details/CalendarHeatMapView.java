package com.base.habittracker.ui.details;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.base.habittracker.R;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class CalendarHeatMapView extends View {

    // TỐI ƯU 1: Dùng Long (Epoch Day) làm key thay vì String -> Nhanh hơn nhiều
    private final Map<Long, Integer> contributionData = new HashMap<>();

    // Các thông số hiển thị (Sẽ được tính theo DP)
    private float cellSize;
    private float cellPadding;
    private float monthLabelHeight;
    private float dayLabelWidth;
    private float cornerRadius;

    // TỐI ƯU 2: Tái sử dụng Object, không new trong onDraw
    private final RectF sharedRect = new RectF();

    // Bút vẽ (Paint)
    private Paint cellPaint;
    private Paint textPaint;
    private Paint datePaint;

    // Cache tính toán text để căn giữa
    private float textYOffset;

    // Khoảng thời gian
    private LocalDate startDate = LocalDate.now();
    private LocalDate endDate = startDate.plusDays(364);
    private static final String[] VIETNAMESE_MONTHS = {
            "Th1", "Th2", "Th3", "Th4", "Th5", "Th6",
            "Th7", "Th8", "Th9", "Th10", "Th11", "Th12"
    };
    int colorDay = Color.parseColor("#AAAAAA");

    @Nullable
    private Long habitEndEpochDay = null;
    public void setHabitEndDate(Long epochDay) {
        if (epochDay == null) {
            habitEndEpochDay = null;
        } else {
            habitEndEpochDay = epochDay;
        }
        invalidate();
    }


    // Màu sắc
    private final int[] colors = {
            Color.parseColor("#EBEDF0"), // Level 0
            Color.parseColor("#9BE9A8"), // Level 1
            Color.parseColor("#40C463"), // Level 2
            Color.parseColor("#30A14E"), // Level 3
            Color.parseColor("#216E39")  // Level 4
    };

    // Constructors
    public CalendarHeatMapView(Context context) { super(context); init(context); }
    public CalendarHeatMapView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context); }
    public CalendarHeatMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context); }


    private void init(Context context) {
        // TỐI ƯU 3: Chuyển đổi dp sang px để hiển thị đều trên mọi màn hình
        cellSize = dpToPx(context, 28f);       // Giảm size xuống cho vừa đẹp (gốc 80 là quá to)
        cellPadding = dpToPx(context, 4f);
        monthLabelHeight = dpToPx(context, 30f);
        dayLabelWidth = dpToPx(context, 30f);
        cornerRadius = dpToPx(context, 4f);

        // Khởi tạo bút vẽ
        cellPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(dpToPx(context, 12f));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.onSurfaceVariant)); // Màu chữ label (Tháng/Thứ)

        datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        datePaint.setTextSize(dpToPx(context, 10f)); // Chữ ngày nhỏ hơn chút
        datePaint.setTextAlign(Paint.Align.CENTER);
        datePaint.setColor(ContextCompat.getColor(getContext(), R.color.onSurfaceVariant));
        // TỐI ƯU 4: Tính toán offset để căn giữa text theo chiều dọc chính xác
        Paint.FontMetrics metrics = datePaint.getFontMetrics();
        textYOffset = (metrics.descent - metrics.ascent) / 2 - metrics.descent;
        colorDay = ContextCompat.getColor(getContext(), R.color.onSurfaceVariant);

    }

    // Hàm public để set dữ liệu (Convert từ String/Date sang Long ở đây 1 lần thôi)
    public void setContributionData(Map<String, Integer> data) {
        contributionData.clear();
        // Convert dữ liệu sang dạng Map<Long, Integer> để vẽ cho nhanh
        // Giả sử key đầu vào là String yyyy-MM-dd
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            try {
                LocalDate date = LocalDate.parse(entry.getKey());
                contributionData.put(date.toEpochDay(), entry.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        invalidate();
    }
    public void setDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) return;
        startDate = start;
        endDate = end;
        requestLayout(); // Gọi lại onMeasure vì kích thước có thể đổi
        invalidate();
    }

    public void setBaseColor(int color, int transparentColor) {
        colors[0] = transparentColor; // Level 0 nên sáng nhẹ hoặc xám nhạt
        colors[1] = ColorUtils.setAlphaComponent(color, 80);
        colors[2] = ColorUtils.setAlphaComponent(color, 140);
        colors[3] = ColorUtils.setAlphaComponent(color, 200);
        colors[4] = color;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        long weeks = ChronoUnit.WEEKS.between(startDate, endDate) + 1;

        int desiredWidth = (int) (weeks * (cellSize + cellPadding));

        // SỬA LỖI: Dùng dấu + thay vì dấu -
        int desiredHeight = (int) (monthLabelHeight + 7 * (cellSize + cellPadding));

        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Tách hàm vẽ riêng biệt để dễ quản lý, nhưng vẫn vẽ trực tiếp lên canvas này
        drawMonthLabels(canvas);
        drawHeatMap(canvas);
    }
    private void drawMonthLabels(Canvas canvas) {
        LocalDate iterDate = startDate;
        int lastMonth = -1;
        int weekIndex = 0;

        // chuyển Sunday (7) -> 6, Monday (1) -> 0
        int currentDayOfWeek = startDate.getDayOfWeek().getValue() - 1;
        if (currentDayOfWeek < 0) currentDayOfWeek = 6;

        while (!iterDate.isAfter(endDate)) {
            if (iterDate.getDayOfMonth() <= 7) {  // Chỉ vẽ khi đang trong tuần đầu tháng
                int month = iterDate.getMonthValue();

                if (month != lastMonth) {
                    float x = weekIndex * (cellSize + cellPadding) + cellSize / 2;

                    String monthName = VIETNAMESE_MONTHS[month - 1];

                    canvas.drawText(monthName, x, monthLabelHeight - dpToPx(getContext(), 8f), textPaint);
                    lastMonth = month;
                }
            }

            // chuyển sang tuần tiếp theo
            if (currentDayOfWeek == 6) { // Chủ nhật -> reset tuần
                weekIndex++;
                currentDayOfWeek = 0;
            } else {
                currentDayOfWeek++;
            }

            iterDate = iterDate.plusDays(1);
        }
    }


    private void drawHeatMap(Canvas canvas) {
        LocalDate currentDate = startDate;
        int weekIndex = 0;

        int dayOfWeek = currentDate.getDayOfWeek().getValue() - 1;
        if (dayOfWeek < 0) dayOfWeek = 6;

        LocalDate today = LocalDate.now().minusDays(1);

        while (!currentDate.isAfter(endDate)) {

            float x = weekIndex * (cellSize + cellPadding);
            float y = monthLabelHeight + dayOfWeek * (cellSize + cellPadding);

            long epochDay = currentDate.toEpochDay();

            Integer level = contributionData.get(epochDay);
            if (level == null) level = 0;

            cellPaint.setColor(colors[level]);

            sharedRect.set(x, y, x + cellSize, y + cellSize);
            canvas.drawRoundRect(sharedRect, cornerRadius, cornerRadius, cellPaint);

            // ====== QUYẾT ĐỊNH VẼ SỐ HAY VẼ ICON ======
            String textToDraw;

            boolean isHabitEndDay = (habitEndEpochDay != null && epochDay == habitEndEpochDay);

            // Ngày "không hoàn thành": level == 0 và ngày này đã xảy ra (<= hôm nay)
            boolean isMissedDay = (level == 0 && !currentDate.isAfter(today));

            if (isHabitEndDay) {
                textToDraw = "🎯";
            } else if (isMissedDay) {
                textToDraw = "❌";
            } else {
                textToDraw = String.valueOf(currentDate.getDayOfMonth());
            }
            // =========================================
            float textX = x + cellSize / 2;
            float textY = y + cellSize / 2 + textYOffset;

            // Màu chữ
            if (level >= 3) {
                datePaint.setColor(Color.WHITE);
            } else {
                datePaint.setColor(level == 0 ? colorDay : Color.WHITE);
            }

            // Icon cũng là text nên vẫn dùng đoạn này như cũ
            if (cellSize > dpToPx(getContext(), 15f)) {
                canvas.drawText(textToDraw, textX, textY, datePaint);
            }

            if (dayOfWeek == 6) {
                weekIndex++;
                dayOfWeek = 0;
            } else {
                dayOfWeek++;
            }
            currentDate = currentDate.plusDays(1);
        }
    }


    // Helper: Convert dp to px
    private float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}