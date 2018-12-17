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
public class AutoBackupJobService extends JobService {

    AutoBackupRunnable job;

    @Override
    public boolean onStartJob(final JobParameters params) {
        Logger.d(">> AutoBackupJobService onStartJob");
        job = AutoBackupRunnable.asyncSingletonRun(new AutoBackupRunnable.Callback() {
            @Override
            public void onFinish(AutoBackupRunnable runnable) {
                jobFinished(params, false);
                job = null;
            }

            @Override
            public void onError(AutoBackupRunnable runnable, Exception x) {
                jobFinished(params, false);
                job = null;
            }
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Logger.d(">> AutoBackupJobService onStopJob");
        if (job != null) {
            job.cancel();
            job = null;
        }
        return false;
    }
}
