package com.base.habittracker.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;


import com.base.habittracker.data.model.Habit;

import java.util.Calendar;
import java.util.Date;

/**
 * Lớp thông báo với hệ điều hành hãy gọi nó vào đúng giờ hoặc không cần
 *
 * */
public class HabitScheduler {
    private static final String TAG = "HabitScheduler";

    // Hàm này dùng để đặt lịch lần đầu hoặc cập nhật
    public static void scheduleReminder(Context context, Habit habit) {
        scheduleInternal(context, habit, false);
    }

    // Hàm này dùng cho Receiver gọi để đặt cho ngày tiếp theo
    public static void scheduleNextReminder(Context context, Habit habit) {
        scheduleInternal(context, habit, true);
    }

    private static void scheduleInternal(Context context, Habit habit, boolean forceTomorrow) {
        if (habit.getReminderTime() == null || habit.getReminderTime().isEmpty() || habit.isNotificationEnabled() == false) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, HabitReminderReceiver.class); /** Tạo 1 intance trỏ đến HabitReminderReceiver(người sẽ nhận tin)*/
        intent.putExtra("EXTRA_HABIT_ID", habit.getId());
        /**Đóng gói Intent đó vào PendingIntent.*/
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                habit.getId(), // mã request phải khác nhau nên dùng id habit
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        String[] timeParts = habit.getReminderTime().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0); // Nên reset cả mili giây

        // Logic thời gian:
        // 1. Nếu được gọi từ Receiver (forceTomorrow = true) -> Luôn cộng 1 ngày
        // 2. Nếu đặt lần đầu và giờ đã qua -> Cộng 1 ngày
        if (forceTomorrow || calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Cộng thêm 1 ngày vào lịch (cho ngày mai).
        }

        if (alarmManager != null) {
            try {
                long triggerTime = calendar.getTimeInMillis();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                        ); // Đây là lệnh mạnh nhất, đảm bảo báo thức nổ chính xác từng giây, ngay cả khi điện thoại đang ngủ (Doze mode).
                    } else {
                        requestExactAlarmPermission(context);
                        Log.w(TAG, "Permission missing, skipping alarm.");
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }
                Log.d(TAG, "Scheduled reminder for habit: " + habit.getName() + " at " + new Date(triggerTime));
            } catch (SecurityException e) {
                Log.e(TAG, "Security exception scheduling alarm: " + e.getMessage());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestExactAlarmPermission(context);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error scheduling alarm: " + e.getMessage());
            }
        }
    }
    // Hàm yêu cầu quyền nổ báo thức chỉ danh cho android 12+
    public static void requestExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        }
    }

    // Hàm huỷ báo thức khi xoá habit hoặc tắt thông báo
    public static void cancelReminder(Context context, int habitId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, HabitReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                habitId, // RequestCode phải khớp
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}