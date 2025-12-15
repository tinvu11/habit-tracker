package com.base.habittracker.ui.stopwatch;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Locale;

public class TimerCircleView extends View {

    // --- MÀU SẮC ---
    // Đổi sang màu Cam/Vàng cho giống đồng hồ đếm giờ, hoặc giữ màu xanh tùy bạn
    private final int COLOR_PRIMARY = Color.parseColor("#FF9800"); // Màu cam
    private final int COLOR_BG_RING = Color.parseColor("#ECF0F1"); // Màu xám nền
    private final int COLOR_TEXT = Color.parseColor("#2C3E50");    // Màu chữ
    private final int COLOR_DOT = Color.parseColor("#FF5722");     // Màu chấm tròn

    // --- KÍCH THƯỚC ---
    private float STROKE_WIDTH_DP = 12f;    // Độ dày vòng
    private float TEXT_SIZE_SP = 40f;       // Kích thước chữ giờ
    private float LABEL_SIZE_SP = 14f;      // Kích thước chữ chú thích nhỏ
    private float DOT_RADIUS_DP = 8f;       // Bán kính chấm tròn
    private float GAP_DP = 16f;             // Khoảng cách từ vòng đến chấm

    // Biến lưu giá trị pixel
    private float strokeWidthPx;
    private float dotRadiusPx;
    private float gapPx;

    // --- PAINTS & OBJECTS ---
    private Paint bgArcPaint;
    private Paint progressArcPaint;
    private Paint timeTextPaint;
    private Paint labelTextPaint;
    private Paint dotPaint;

    private RectF arcRect = new RectF();

    // --- TRẠNG THÁI THỜI GIAN ---
    private long maxTimeSeconds = 15 * 60; // Mặc định 15 phút (tính bằng giây)
    private long currentTimeSeconds = 0;   // Thời gian hiện tại đang đếm

    public TimerCircleView(Context context) {
        super(context);
        init();
    }

    public TimerCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Convert kích thước
        strokeWidthPx = dpToPx(STROKE_WIDTH_DP);
        dotRadiusPx = dpToPx(DOT_RADIUS_DP);
        gapPx = dpToPx(GAP_DP);
        float textSize = spToPx(TEXT_SIZE_SP);
        float labelSize = spToPx(LABEL_SIZE_SP);

        // 1. Paint nền (Vòng xám)
        bgArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgArcPaint.setColor(COLOR_BG_RING);
        bgArcPaint.setStyle(Paint.Style.STROKE);
        bgArcPaint.setStrokeWidth(strokeWidthPx);
        bgArcPaint.setStrokeCap(Paint.Cap.ROUND);

        // 2. Paint tiến độ (Vòng màu)
        progressArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressArcPaint.setColor(COLOR_PRIMARY);
        progressArcPaint.setStyle(Paint.Style.STROKE);
        progressArcPaint.setStrokeWidth(strokeWidthPx);
        progressArcPaint.setStrokeCap(Paint.Cap.ROUND);

        // 3. Paint chữ thời gian (00:00)
        timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timeTextPaint.setColor(COLOR_TEXT);
        timeTextPaint.setTextSize(textSize);
        timeTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        timeTextPaint.setTextAlign(Paint.Align.CENTER);

        // 4. Paint chữ chú thích (ví dụ: "Time Elapsed")
        labelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelTextPaint.setColor(Color.GRAY);
        labelTextPaint.setTextSize(labelSize);
        labelTextPaint.setTextAlign(Paint.Align.CENTER);

        // 5. Paint chấm tròn (Satellite)
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(COLOR_DOT);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setShadowLayer(6f, 0, 3f, Color.parseColor("#40000000"));
        setLayerType(LAYER_TYPE_SOFTWARE, dotPaint); // Bật software layer để vẽ shadow
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Tính toán padding để không bị cắt nét vẽ
        float padding = (strokeWidthPx / 2f) + gapPx + (dotRadiusPx * 2) + 5f;

        int size = Math.min(w, h);
        float radius = (size / 2f) - padding;

        float cx = w / 2f;
        float cy = h / 2f;

        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        // 1. Vẽ vòng nền 360 độ
        canvas.drawArc(arcRect, 0, 360, false, bgArcPaint);

        // 2. Tính toán góc quét dựa trên thời gian
        // Đảm bảo không chia cho 0
        float ratio = (maxTimeSeconds > 0) ? (float) currentTimeSeconds / maxTimeSeconds : 0;
        // Giới hạn ratio không vượt quá 1 (100%)
        if (ratio > 1f) ratio = 1f;

        float sweepAngle = 360f * ratio;

        // 3. Vẽ vòng tiến độ (Bắt đầu từ -90 độ là 12 giờ)
        canvas.drawArc(arcRect, -90, sweepAngle, false, progressArcPaint);

        // 4. Vẽ chữ ở giữa
        drawCenterText(canvas, centerX, centerY);

        // 5. Vẽ chấm tròn ở đầu mút
        drawProgressDot(canvas, centerX, centerY, sweepAngle);
    }

    private void drawCenterText(Canvas canvas, float cx, float cy) {
        // Định dạng thời gian thành MM:ss
        long minutes = currentTimeSeconds / 60;
        long seconds = currentTimeSeconds % 60;
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        // Căn giữa theo chiều dọc
        Paint.FontMetrics metrics = timeTextPaint.getFontMetrics();
        float yOffset = (metrics.descent + metrics.ascent) / 2f;
        canvas.drawText(timeStr, cx, cy - yOffset, timeTextPaint);

        // Vẽ thêm label nhỏ bên dưới (Tùy chọn)
        // canvas.drawText("Đã chạy", cx, cy - yOffset + dpToPx(24), labelTextPaint);
    }

    private void drawProgressDot(Canvas canvas, float cx, float cy, float sweepAngle) {
        // Góc hiện tại = Góc bắt đầu (-90) + Góc đã quét
        double angleRad = Math.toRadians(-90 + sweepAngle);

        // Bán kính quỹ đạo của chấm tròn
        // Nó nằm cách mép ngoài của vòng tròn chính một khoảng gapPx
        float ringRadius = arcRect.width() / 2f;
        float orbitRadius = ringRadius + (strokeWidthPx / 2f) + gapPx + dotRadiusPx;

        // Nếu bạn muốn chấm tròn nằm TRÊN vòng tròn, thì dùng công thức này:
        // float orbitRadius = ringRadius;

        float dotX = cx + (float) (orbitRadius * Math.cos(angleRad));
        float dotY = cy + (float) (orbitRadius * Math.sin(angleRad));

        canvas.drawCircle(dotX, dotY, dotRadiusPx, dotPaint);
    }

    // --- PUBLIC API ---

    /**
     * Thiết lập tổng thời gian (mục tiêu)
     * @param minutes số phút (ví dụ 15)
     */
    public void setMaxTime(int minutes) {
        this.maxTimeSeconds = minutes * 60L;
        invalidate();
    }

    /**
     * Cập nhật thời gian hiện tại (Count Up)
     * @param seconds số giây đã trôi qua
     */
    public void setCurrentTime(long seconds) {
        this.currentTimeSeconds = seconds;
        invalidate(); // Vẽ lại view
    }

    /**
     * Reset về 00:00
     */
    public void reset() {
        this.currentTimeSeconds = 0;
        invalidate();
    }

    // --- UTILS ---
    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics());
    }
}
