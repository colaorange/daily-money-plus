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

    private static transient boolean booted = false;

    private static transient int err = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (StartupReceiver.class) {
            if (booted) {
                return;
            }
            if(err >= 9){
                //give up to start receiver service
                return;
            }

            Logger.d("going to start startup service");

            //#16 RuntimeException when starting service
            //I don't know why, maybe the booted transient issue.
            //so just prevent it crashing app and set a max-retry
            try {
                Intent timeTickIntent = new Intent(context, StartupService.class);
                context.startService(timeTickIntent);

                booted = true;
            } catch (Exception x) {
                Logger.e(x.getMessage(), x);
                err++;
            }


        }
    }
}
