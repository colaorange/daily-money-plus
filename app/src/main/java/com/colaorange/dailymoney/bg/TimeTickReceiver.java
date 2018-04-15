package com.colaorange.dailymoney.bg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.context.Preference;
import com.colaorange.dailymoney.data.DataBackupRestorer;
import com.colaorange.dailymoney.util.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * receiver is running in main thread.
 *
 * @author dennis
 */
public class TimeTickReceiver extends BroadcastReceiver {

    ExecutorService executorService = Executors.newFixedThreadPool(1);

    AutoBackupRunnable autoBackup = new AutoBackupRunnable();

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d("time ticker onReceive");
        executorService.execute(autoBackup);
    }


}
