package com.base.habittracker.utils;
import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.base.habittracker.MyHabitApp;
import com.base.habittracker.R;
import com.base.habittracker.data.model.Habit;
import com.base.habittracker.data.repository.HabitRepository;
import com.base.habittracker.ui.main.MainActivity;
import android.util.Log;

import java.time.LocalDate;
import java.util.concurrent.Executors;


/**Lớp này chịu trách nhiệm nhận tín hiệu báo thức từ hệ thống và quyết định xem có nên hiển thị thông báo hay không.
 * - Kiểm tra database trước khi gửi thông báo
 * */
public class HabitReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "HabitReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        int habitId = intent.getIntExtra("EXTRA_HABIT_ID", -1);
        if (habitId == -1) return;

        // 1. QUAN TRỌNG: Gọi goAsync() để giữ Receiver sống lâu hơn cho tác vụ nền
        final PendingResult result = goAsync();
        /**
         * Vấn đề: BroadcastReceiver mặc định chỉ sống trong khoảng 10 giây trên luồng chính (Main Thread). Nếu bạn thực hiện tác vụ nặng (như truy vấn Database) mà quá thời gian này, hệ thống sẽ giết Receiver.
         * Giải pháp: goAsync() báo cho hệ thống biết: "Tôi cần thêm thời gian để xử lý ở luồng phụ, đừng giết tôi vội".
         * Kết thúc: Bạn bắt buộc phải gọi result.finish() trong khối finally ở cuối để báo hiệu đã xong việc.
         * */

        MyHabitApp app = (MyHabitApp) context.getApplicationContext();
        HabitRepository repository = HabitRepository.getInstance(app);

        // Đẩy vào luồng phụ với timeout protection
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Add timeout to prevent hanging
                long startTime = System.currentTimeMillis();
                final long TIMEOUT_MS = 8000; // 8 seconds max
                
                Habit habit = repository.getHabitByIdBlocking(habitId);
                
                if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
                    Log.w(TAG, "Operation timed out");
                    return;
                }

                if (habit != null) {
                    // Lấy trạng thái hoàn thành hôm nay
                    int habitCompletion = repository.getHabitCompletionToDay(LocalDate.now().toEpochDay(), habitId);

                    // 2. LOGIC HIỆN THÔNG BÁO
                    // Chỉ hiện nếu chưa hoàn thành (completion == 0) VÀ có giờ nhắc
                    if (habitCompletion == 0 && habit.getReminderTime() != null && !habit.getReminderTime().isEmpty()) {
                        showNotification(context, habit);
                    } else {
                        Log.d(TAG, "Habit done today or reminder removed. Silent.");
                    }
                    // 3. QUAN TRỌNG: ĐẶT LỊCH LẠI CHO NGÀY MAI
                    // Vì setExact chỉ chạy 1 lần, ta phải thủ công đặt cho ngày tiếp theo
                    HabitScheduler.scheduleNextReminder(context, habit);
                } else {
                    HabitScheduler.cancelReminder(context, habitId);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in reminder receiver: " + e.getMessage(), e);
            } finally {
                // 4. BẮT BUỘC: Kết thúc goAsync để giải phóng tài nguyên
                try {
                    result.finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error finishing async result: " + e.getMessage());
                }
            }
        });
    }
    private void showNotification(Context context, Habit habit) {
        // Intent để khi nhấn vào thông báo sẽ mở App
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE);
        /**PendingIntent là gì? Bình thường, Intent được dùng để mở màn hình ngay lập tức. Nhưng ở đây, bạn chưa muốn mở ngay. Bạn muốn gói cái Intent đó lại và đưa cho hệ thống Android giữ. Hệ thống sẽ chỉ mở nó khi và chỉ khi người dùng nhấn vào thông báo. Nó giống như một "tấm vé chờ".
         getActivity: Báo cho hệ thống biết tấm vé này dùng để mở một Activity (màn hình), không phải mở Service hay Broadcast.
         FLAG_IMMUTABLE: Đây là cờ bảo mật bắt buộc từ Android 12 trở lên. Nó có nghĩa là: "Cái Intent này đóng gói xong là chốt, không ai được sửa đổi nội dung bên trong nó nữa".*/

        /**(NotificationCompat.Builder builder Bản thiết kế thông báo)*/
        // Nó nói thông báo này thuộc về "kênh" nào (ví dụ: kênh "Nhắc nhở"). Nếu ID này sai hoặc kênh chưa được tạo, thông báo sẽ không hiện.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MyHabitApp.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // (Icon nhỏ trên thanh trạng thái)
                .setContentTitle("Đến giờ rồi!")
                .setContentText("Hãy thực hiện thói quen: " + habit.getName())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Kiểm tra quyền Android 13 trước khi hiện thông báo
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(habit.getId(), builder.build()); // Dùng habitId làm ID thông báo
            Log.d(TAG, "Notification sent successfully for habit ID: " + habit.getId());
        } else {
            Log.e(TAG, "POST_NOTIFICATIONS permission not granted");
        }
    }
}