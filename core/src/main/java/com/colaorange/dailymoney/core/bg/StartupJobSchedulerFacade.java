package com.colaorange.dailymoney.core.bg;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.colaorange.commons.util.Numbers;
import com.colaorange.dailymoney.core.util.Logger;

/**
 * @author Dennis
 */
public class StartupJobSchedulerFacade {


    private volatile static boolean started;

    public static final int AUTOBACKUP_JOB_ID = 1;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public synchronized static void startup(Context context) {
        if (started) {
            return;
        }
        try {
            Logger.i("going to startup job scheduler");


            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);


            JobInfo.Builder jb = new JobInfo.Builder(AUTOBACKUP_JOB_ID, new ComponentName(context, AutoBackupJobService.class));
            //todo change to 10 min after test
//            jb.setPeriodic(10*Numbers.MINUTE);
            //god damn it....., it doesn't respect my setting and doesn't mension that in doc.
            //Specified interval for 1 is +1m0s0ms. Clamped to +15m0s0ms
            //Specified flex for 1 is +1m0s0ms. Clamped to +5m0s0ms
            jb.setPeriodic(10 * Numbers.MINUTE);
            //we are possible sync with google drive
//            jb.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);


            int r = jobScheduler.schedule(jb.build());
            if (r == JobScheduler.RESULT_SUCCESS) {
                started = true;
            } else {
                Logger.e("startup autobackup job fail");
            }
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        }
    }

}
