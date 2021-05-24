package py.com.opentech.drawerwithbottomnavigation.firebase;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import py.com.opentech.drawerwithbottomnavigation.R;
import py.com.opentech.drawerwithbottomnavigation.SplashScreen;

public class PdfReaderFirebaseMessageService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d("hoangpm", s);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//
//        Map<String, String> params = remoteMessage.getData();
//
//
//        if (isAppIsInBackground(getApplicationContext())) {
//            setupNotification(params);
//
//        } else {
//
//            try {
//
//                setupNotification(params);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//        LogUtils.printLog();

    }

    private void setupNotification(Map<String, String> dataObj) {
        try {
            JSONObject jsonObject = new JSONObject(dataObj);
            String message = jsonObject.getString("message");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String channel_id = createNotificationChannel(getApplicationContext());

            String appName = getString(R.string.app_name);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channel_id)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(appName)
                    .setContentText(message)
                    .setAutoCancel(true);
            //Intent notificationIntent = LoginFragment.Companion.newIntent(getApplicationContext(), "transaction_detail");
            Intent notificationIntent = new Intent(getApplicationContext(), SplashScreen.class);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0,
                    notificationIntent, 0);

            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

            mBuilder.setContentIntent(intent);
            if (notificationManager != null) {
                notificationManager.notify(1, mBuilder.build());
            }

        } catch (Exception e) {
            e.printStackTrace();

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String channel_id = createNotificationChannel(getApplicationContext());

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channel_id)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(dataObj.toString())
                    .setAutoCancel(true);

            Intent notificationIntent = new Intent(getApplicationContext(), SplashScreen.class);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0,
                    notificationIntent, 0);
            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            mBuilder.setContentIntent(intent);
            notificationManager.notify(1, mBuilder.build());
        }
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

    public static String createNotificationChannel(Context context) {

        // NotificationChannels are required for Notifications on O (API 26) and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // The id of the channel.
            String channelId = "Channel_id";

            // The user-visible name of the channel.
            CharSequence channelName = "BVB_SOTP";
            // The user-visible description of the channel.
            String channelDescription = "BVB SOTP Alert";
            int channelImportance = NotificationManager.IMPORTANCE_DEFAULT;
//            boolean channelEnableVibrate = true;
//            int channelLockscreenVisibility = Notification.;

            // Initializes NotificationChannel.
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, channelImportance);
            notificationChannel.setDescription(channelDescription);
//            notificationChannel.enableVibration(channelEnableVibrate);
//            notificationChannel.setLockscreenVisibility(channelLockscreenVisibility);

            // Adds NotificationChannel to system. Attempting to create an existing notification
            // channel with its original values performs no operation, so it's safe to perform the
            // below sequence.
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);

            return channelId;
        } else {
            // Returns null for pre-O (26) devices.
            return "BVB";
        }
    }

}
