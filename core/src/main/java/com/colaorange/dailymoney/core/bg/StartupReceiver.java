package com.colaorange.dailymoney.core.bg;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.colaorange.commons.util.Numbers;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * receive either system booted or app be started
 * <p>
 * 0-1.Device Boot > android.intent.action.BOOT_COMPLETED
 * -> StartupReceiver [1]
 * <p>
 * 0-2.StartupActivity > OnStart
 * broadcast(com.colaorange.dailymoney.broadcast.STARTUP)
 * -> StartupReceiver [1]
 */
public class StartupReceiver extends BroadcastReceiver {

    public static final String ACTION_STARTUP = "com.colaorange.dailymoney.broadcast.STARTUP";
    public static final String ACTION_STARTUP_SCHEDULER = "com.colaorange.dailymoney.broadcast.STARTUP_SCHEDULER";

    private static AtomicBoolean booted = new AtomicBoolean(false);

    public static final int AUTO_BACKUP_JOB_ID = 3001;

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (booted) {
            if (booted.get()) {
                Logger.d(">> StartupReceiver, receive startup event, skip because of booted");
                return;
            }
            //#16 RuntimeException when starting service
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Logger.d(">> StartupReceiver, going to start TimeTickStickyService");

                try {
                    Intent timeTickIntent = new Intent(context, TimeTickStickyService.class);
                    context.startService(timeTickIntent);
                } catch (Exception x) {
                    Logger.e(x.getMessage(), x);
                    Contexts.instance().trackEvent("schedule-service", "fail1", null, 0L);
                }
            } else {
                //android 5+
                Logger.d(">> StartupReceiver, going to send schedule event");
                try {
                    Intent schedulerIntent = new Intent();
                    schedulerIntent.setAction(StartupReceiver.ACTION_STARTUP_SCHEDULER);
                    context.sendBroadcast(schedulerIntent);
                } catch (Exception x) {
                    Logger.e(x.getMessage(), x);
                    Contexts.instance().trackEvent("schedule-service", "fail2", null, 0L);
                }
            }

            booted.set(true);
        }
    }
}
