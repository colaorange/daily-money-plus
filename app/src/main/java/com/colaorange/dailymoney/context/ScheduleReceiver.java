package com.colaorange.dailymoney.context;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.colaorange.dailymoney.data.BackupRestorer;
import com.colaorange.dailymoney.ui.Constants;

/**
 * needs re-implementation
 */
public class ScheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long now = System.currentTimeMillis();
        if (Constants.BACKUP_JOB.equals(intent.getAction())) {
            //TODO ctxs is possible not ok if no ui been called
            //needs to check again.
            Contexts ctxs = Contexts.instance();
            try {
                int count = 0;
                count += BackupRestorer.copyDatabases(ctxs.getAppDbFolder(), ctxs.getWorkingFolder(), now);
                count += BackupRestorer.copyPrefFile(ctxs.getAppPrefFolder(), ctxs.getWorkingFolder(), now);
                if (count > 0) {
                    ctxs.setPrefLastBackup(now);
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
