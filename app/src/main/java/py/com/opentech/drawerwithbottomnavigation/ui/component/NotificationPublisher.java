package py.com.opentech.drawerwithbottomnavigation.ui.component;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

import py.com.opentech.drawerwithbottomnavigation.R;
import py.com.opentech.drawerwithbottomnavigation.SplashScreen;
import py.com.opentech.drawerwithbottomnavigation.utils.Constants;

import static android.content.Context.MODE_PRIVATE;


public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static String NOTIFICATION_CHANEL_ID = "NOTIFICATION_CHANEL_ID";

    public void onReceive(Context context, Intent intent) {

        if (!isAppIsInBackground(context)) {
            Log.d("hoangpm2", "not showing notification");
            return;
        }

//        Bitmap largeImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_app_border);

        Intent intentOpen = new Intent(context, SplashScreen.class);
        intentOpen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentOpen, 0);
        SharedPreferences prefs = context.getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE);
        String content = prefs.getString(Constants.NOTIFICATION_CONTENT, "");
        System.out.println("-content----------------"+content);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANEL_ID)
                    .setSmallIcon(R.drawable.ic_icon_app)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(content)
//                    .setLargeIcon(largeImage)
                    .setContentIntent(pendingIntent)
//                    .setStyle(new NotificationCompat.BigTextStyle())
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify((int) (Math.random() * 1000), builder.build());
        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANEL_ID)
                    .setSmallIcon(R.drawable.ic_icon_app)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(content)
//                    .setLargeIcon(largeImage)
                    .setContentIntent(pendingIntent)
//                    .setStyle(new NotificationCompat.BigTextStyle())
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify((int) (Math.random() * 1000), builder.build());
        }
        Log.d("hoangpm2", "show notification");


    }
    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        isInBackground = false;
                    }
                }
            }
        }

        return isInBackground;
    }

}