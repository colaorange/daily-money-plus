package com.colaorange.dailymoney.core.bg;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.colaorange.dailymoney.core.util.Logger;

/**
 * the service to stick time ticker
 * @author dennis
 */
public class TimeTickStickyService extends Service {

    private TimeTickReceiver timeTickReceiver;

    public TimeTickStickyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Logger.d(">> TimeTickStickyService service onCreate");
        if (timeTickReceiver == null) {
            timeTickReceiver = new TimeTickReceiver();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d(">> TimeTickStickyService onStartCommand");

        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_TICK);

        registerReceiver(timeTickReceiver, filter);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.d(">> TimeTickStickyService onDestroy");
        if (timeTickReceiver != null) {
            unregisterReceiver(timeTickReceiver);
            timeTickReceiver = null;
        }
    }
}
