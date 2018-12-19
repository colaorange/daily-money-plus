package com.colaorange.dailymoney.core.bg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.colaorange.dailymoney.core.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TimeTick receiver for android 5-
 * <p>
 * Note: receiver is running in main thread.
 *
 * @author dennis
 */
public class TimeTickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Logger.d(">> TimeTickReceiver onReceive {}", action);

        if (Intent.ACTION_TIME_TICK.equals(action)) {
            AutoBackupRunnable.asyncSingletonRun(null);
        }

    }
}
