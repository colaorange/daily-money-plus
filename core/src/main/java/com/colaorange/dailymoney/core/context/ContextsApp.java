package com.colaorange.dailymoney.core.context;

import android.content.IntentFilter;
import android.os.Build;
import android.support.multidex.MultiDexApplication;

import com.colaorange.dailymoney.core.bg.AlarmManagerReceiver;
import com.colaorange.dailymoney.core.bg.StartupReceiver;
import com.colaorange.dailymoney.core.bg.JobSchedulerReceiver;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.dailymoney.core.util.Notifications;

/**
 * @author Dennis
 */

public class ContextsApp extends MultiDexApplication {

    static {
        //to support poi,
        //read https://github.com/centic9/poi-on-android
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Contexts.instance().initApplication(this);
        try {
            Notifications.initAllChannel(this);
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        }
        try {
            //android 8+
            //https://developer.android.com/about/versions/oreo/background#broadcasts
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //register receiver
                registerReceivers();
            }
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        }
        Logger.d("===============Application Created");

    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(StartupReceiver.ACTION_STARTUP);
        registerReceiver(new StartupReceiver(), filter);

        filter = new IntentFilter();
        filter.addAction(StartupReceiver.ACTION_STARTUP_JOB_SCHEDULER);
        registerReceiver(new JobSchedulerReceiver(), filter);

        filter = new IntentFilter();
        filter.addAction(StartupReceiver.ACTION_STARTUP_ALARM_MANAGER);
        registerReceiver(new AlarmManagerReceiver(), filter);

        //TimeTickReceiver is not necessary, it works on old phone.
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        //this method is not always been called
        Logger.d("===============Application Terminated");
        Contexts.instance().destroyApplication(this);
    }


}
