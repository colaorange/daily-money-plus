package com.colaorange.dailymoney.bg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.colaorange.dailymoney.util.Logger;

/**
 * receive either system booted or app be started
 */
public class StartupReceiver extends BroadcastReceiver {

    private static boolean booted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (StartupReceiver.class){
            if(booted){
                return;
            }
            Logger.d("startup receiver is on going");

            Intent timeTickIntent = new Intent(context, TimeTickService.class);
            context.startService(timeTickIntent);

            booted = true;
        }
    }
}
