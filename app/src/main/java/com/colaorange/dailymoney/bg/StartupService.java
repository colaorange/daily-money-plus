package com.colaorange.dailymoney.bg;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.colaorange.dailymoney.util.Logger;

/**
 * @author dennis
 */
public class StartupService extends Service {

    private TimeTickReceiver timeTickReceiver;

    public StartupService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Logger.d("startup service onCreate");
        if (timeTickReceiver == null) {
            timeTickReceiver = new TimeTickReceiver();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("startup service onStartCommand");


        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(TimeTickReceiver.ACTION_CLEAR_BACKUP_ERROR);

        registerReceiver(timeTickReceiver, filter);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.d("startup service onDestroy");
        if (timeTickReceiver != null) {
            unregisterReceiver(timeTickReceiver);
            timeTickReceiver = null;
        }
    }
}
