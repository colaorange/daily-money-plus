package com.colaorange.dailymoney.core.bg;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.colaorange.dailymoney.core.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Dennis
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AutoBackupJobService extends JobService{

    ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public boolean onStartJob(final JobParameters params) {
        executorService.execute(new Runnable(){
            @Override
            public void run() {
                try {
                    Logger.d("start autobackup job");
                    AutoBackupRunnable.singleton().run();
                }catch(Exception x){
                    Logger.e(x.getMessage(), x);
                }
                jobFinished(params, false);
            }
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        AutoBackupRunnable.singleton().cancel();
        return false;
    }
}
