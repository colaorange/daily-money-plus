package com.colaorange.dailymoney.core.bg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * receive either system booted or app be started
 */
public class StartupReceiver extends BroadcastReceiver {

    public static final String ACTION_STARTUP = "com.colaorange.dailymoney.broadcast.STARTUP";

    private static volatile boolean booted = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        synchronized (StartupReceiver.class) {
            if (booted) {
                return;
            }
            //#16 RuntimeException when starting service
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                Logger.d("going to start startup service");

                try {
                    Intent timeTickIntent = new Intent(context, StartupService.class);
                    context.startService(timeTickIntent);

                    booted = true;
                } catch (Exception x) {
                    Logger.e(x.getMessage(), x);
                    Contexts.instance().trackEvent("startup-service","fail",null, 0L);
                }
            }else{
                //android 5+
                StartupJobSchedulerFacade.startup(context);
            }


        }
    }
}
