package com.colaorange.dailymoney.core.bg;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.colaorange.commons.util.Numbers;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.util.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * schedule by event receiver to prevent class loading issue in android 5- env
 */
public class StartupSchedulerReceiver extends BroadcastReceiver {

    public static final int AUTO_BACKUP_JOB_ID = 3001;


    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d(">> StartupSchedulerReceiver, receive scheduler event");
        try {
            schedule(context);
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
            Contexts.instance().trackEvent("schedule-service", "fail2", null, 0L);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void schedule(Context context) throws Exception {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);


        JobInfo.Builder jb = new JobInfo.Builder(AUTO_BACKUP_JOB_ID, new ComponentName(context, AutoBackupJobService.class));
        //todo change to 10 min after test
//            jb.setPeriodic(1*Numbers.MINUTE);
        //god damn it....., it doesn't respect my setting and doesn't mension that in doc.
        //Specified interval for 1 is +1m0s0ms. Clamped to +15m0s0ms
        //Specified flex for 1 is +1m0s0ms. Clamped to +5m0s0ms
        jb.setPeriodic(10 * Numbers.MINUTE);
        //we are possible sync with google drive
//            jb.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

        JobInfo job = jb.build();
        int r = jobScheduler.schedule(job);

        if (r != JobScheduler.RESULT_SUCCESS) {
            throw new IllegalStateException("schedule job fail, result " + r);
        }else{
            Logger.d(">> schedule job {}, {}", job.getId(), job.getIntervalMillis());
        }
    }
}
