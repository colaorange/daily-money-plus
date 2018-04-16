package com.colaorange.dailymoney.bg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.colaorange.dailymoney.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * receiver is running in main thread.
 *
 * @author dennis
 */
public class TimeTickReceiver extends BroadcastReceiver {

    public static final String ACTION_CLEAR_BACKUP_ERROR = "com.colaorange.dailymoney.broadcast.CLEAR_BACKUP_ERROR";
    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Logger.d("time ticker onReceive {}", action);

        if (ACTION_CLEAR_BACKUP_ERROR.equals(action)) {
            AutoBackupRunnable.singleton().clearErrorDayHour();
        } else if (Intent.ACTION_TIME_TICK.equals(action)) {
            executorService.execute(AutoBackupRunnable.singleton());
        }

    }


}
