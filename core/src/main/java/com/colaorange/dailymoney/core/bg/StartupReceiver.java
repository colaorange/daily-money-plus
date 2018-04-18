package com.colaorange.dailymoney.core.bg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.colaorange.dailymoney.core.util.Logger;

/**
 * receive either system booted or app be started
 */
public class StartupReceiver extends BroadcastReceiver {

    public static final String ACTION_STARTUP = "com.colaorange.dailymoney.broadcast.STARTUP";

    private static boolean booted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (StartupReceiver.class){
            if(booted){
                return;
            }

            Logger.d("going to start startup service");

            Intent timeTickIntent = new Intent(context, StartupService.class);
            context.startService(timeTickIntent);

            booted = true;
        }
    }
}
