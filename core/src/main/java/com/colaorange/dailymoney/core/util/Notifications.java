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
import com.colaorange.dailymoney.core.context.Contexts;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dennis
 */
public class Notifications {

    private static AtomicInteger commonGroupId = new AtomicInteger(0);

    public static int nextGroupId() {
        int i = commonGroupId.getAndIncrement();
        return i;
    }

    public static int currGroupId() {
        return commonGroupId.get();
    }

    public enum Channel {
        DEFAULT, BACKUP
    }

    public enum Level {
        INFO, WARN, ERROR;

    }

    @Deprecated
    public static final String CHANNEL_ID_SYSTEM = "com.colaorange.dailymoney.system";
    @Deprecated
    public static final String CHANNEL_ID_APP_ICON = "com.colaorange.dailymoney.appicon";

    public static final String CHANNEL_ID_DEFAULT = "com.colaorange.dailymoney.default";
    public static final String CHANNEL_ID_BACKUP = "com.colaorange.dailymoney.backup";

    private static final Set<String> channelIdCreated = java.util.Collections.synchronizedSet(new HashSet<String>());

    public static void initAllChannel(Context context) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        I18N i18n = Contexts.instance().getI18n();

        deleteChannel(CHANNEL_ID_SYSTEM, manager);
        deleteChannel(CHANNEL_ID_APP_ICON, manager);


        initChannel(CHANNEL_ID_DEFAULT, i18n.string(R.string.label_channel_default), manager);
        initChannel(CHANNEL_ID_BACKUP, i18n.string(R.string.label_channel_backup), manager);
    }

    public static void send(Context context, int groupId, String msg, @Nullable String title,
                            @Nullable Channel channel, @Nullable Level level, @Nullable Intent intent) {
        String channelId;
        channel = channel == null ? Channel.DEFAULT : channel;
        switch (channel) {
            case DEFAULT:
                channelId = CHANNEL_ID_DEFAULT;
                break;
            case BACKUP:
            default:
                channelId = CHANNEL_ID_BACKUP;
                break;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setContentText(msg)
                        .setContentTitle(title);

        mBuilder.setSmallIcon(R.drawable.ic_notification);

        level = level == null ? Level.INFO : level;
        //TODO different level, icon coloring.
        switch (level) {
            case INFO:
                break;
            case WARN:
                mBuilder.setColorized(true);
                mBuilder.setColor(0xeeffcc00);
                break;
            case ERROR:
                mBuilder.setColorized(true);
                mBuilder.setColor(0xeecc3300);
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

    private synchronized static void deleteChannel(String channelId, NotificationManager manager){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        try {
           manager.deleteNotificationChannel(channelId);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        channelIdCreated.add(channelId);
    }

    private synchronized static void initChannel(String channelId, String channelName, NotificationManager manager) {
        //api 26, android 8.0 only
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        if (channelIdCreated.contains(channelId)) {
            return;
        }

        try {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        }

        channelIdCreated.add(channelId);

    }
}
