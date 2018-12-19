package com.colaorange.dailymoney.core.bg;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.colaorange.commons.util.Numbers;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.Calendar;

/**
 * TimeTick receiver for android 5-
 * <p>
 * Note: receiver is running in main thread.
 *
 * @author dennis
 */
public class AlarmManagerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Logger.d(">> AlarmManagerReceiver onReceive {}", action);
        if (StartupReceiver.ACTION_STARTUP_ALARM_MANAGER.equals(action)) {
            AutoBackupRunnable.asyncSingletonRun(null);
            alertMe(context, 60000);
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void alertMe(Context context, int millisecondLater){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, millisecondLater);

        Intent alertIntent = new Intent(context, AlarmManagerReceiver.class);
        alertIntent.setAction(StartupReceiver.ACTION_STARTUP_ALARM_MANAGER);

        PendingIntent pi = PendingIntent.getBroadcast(context, 1, alertIntent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
    }
}
