package com.colaorange.dailymoney.core.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.colaorange.dailymoney.core.R;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Dennis
 */
public class Notifications {

    public enum Level {
        INFO, WARN, ERROR
    }

    public enum Target {
        SYSTEM_BAR, APP_ICON
    }

    public static final String CHANNEL_ID_SYSTEM = "com.colaorange.dailymoney.system";
    public static final String CHANNEL_ID_APP_ICON = "com.colaorange.dailymoney.appicon";

    private static final Set<String> channelIdCreated = java.util.Collections.synchronizedSet(new HashSet<String>());

    public static void send(Context context, Target target, Level level, String msg, @Nullable String title, @Nullable Intent intent, int groupId) {
        String channelId;
        switch (target) {
            case APP_ICON:
                channelId = CHANNEL_ID_APP_ICON;
                break;
            case SYSTEM_BAR:
            default:
                channelId = CHANNEL_ID_SYSTEM;
                break;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        initChannel(channelId, manager);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setContentTitle(msg)
                        .setContentText(title);

        mBuilder.setSmallIcon(R.drawable.ic_notification);

        //TODO different level, icon coloring.
        switch (level) {
            case INFO:
                break;
            case WARN:
                break;
            case ERROR:
                break;
        }

        PendingIntent notifyPendingIntent = null;
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            notifyPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            mBuilder.setContentIntent(notifyPendingIntent);
        }


        // mId allows you to update the notification later on.

        manager.notify(groupId, mBuilder.build());
    }

    private synchronized static void initChannel(String channelId, NotificationManager manager) {
        //api 26, android 8.0 only
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        if (channelIdCreated.contains(channelId)) {
            return;
        }

        try {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        }

        channelIdCreated.add(channelId);

    }
}
