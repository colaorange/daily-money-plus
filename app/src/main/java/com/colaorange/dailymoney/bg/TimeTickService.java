package com.colaorange.dailymoney.bg;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.colaorange.dailymoney.util.Logger;

/**
 * @author dennis
 */
public class TimeTickService extends Service {

    private TimeTickReceiver receiver;

    public TimeTickService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        Logger.d("time ticker service onCreate");
        if(receiver ==null){
            receiver = new TimeTickReceiver();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("time ticker service onStartCommand");
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, filter);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.d("time ticker service onDestroy");
        if(receiver !=null){
            unregisterReceiver(receiver);
            receiver = null;
        }
    }
}
