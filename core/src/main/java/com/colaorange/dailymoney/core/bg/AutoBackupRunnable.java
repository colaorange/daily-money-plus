package com.colaorange.dailymoney.core.bg;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.Preference;
import com.colaorange.dailymoney.core.data.DataBackupRestorer;
import com.colaorange.dailymoney.core.drive.GoogleDriveBackupRestorer;
import com.colaorange.dailymoney.core.drive.GoogleDriveHelper;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.dailymoney.core.util.Notifications;
import com.colaorange.dailymoney.core.util.Threads;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author by Dennis
 */
public class AutoBackupRunnable implements Runnable {

    private static AutoBackupRunnable instance;

    AtomicBoolean running = new AtomicBoolean(false);

    AtomicBoolean canceling = new AtomicBoolean(false);

    private AutoBackupRunnable() {
    }

    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            Logger.d("autobackup is still running");
            return;
        }
        canceling.set(false);

        try {
            Preference pref = Contexts.instance().getPreference();
            if (!pref.isAutoBackup()) {
                return;
            }
            doAutoBackup(Contexts.instance(), pref, Contexts.instance().getI18n());
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        } finally {
            running.set(false);
            canceling.set(false);
        }
    }


    private void doAutoBackup(final Contexts contexts, final Preference pref, final I18N i18n) throws Exception {
        Logger.d("start autobackup evaluation");

        DateFormat format = new SimpleDateFormat("yyMMddHH");

        CalendarHelper helper = pref.getCalendarHelper();
        Calendar cal = helper.calendar(new Date());

        Long lastBackup = pref.getLastBackupTime();

        Set<Integer> autoBackupWeekDays = pref.getAutoBackupWeekDays();
        Set<Integer> autoBackupAtHours = pref.getAutoBackupAtHours();
        int yearDay = cal.get(Calendar.DAY_OF_YEAR);
        int weekDay = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (!(autoBackupWeekDays.contains(weekDay) && autoBackupAtHours.contains(hour))) {
            //not in week day & hour.
            Logger.d("{},{} not in weekday {} and hours {}, skip", weekDay, hour, autoBackupWeekDays, autoBackupAtHours);
            return;
        }

        if (lastBackup != null) {
            //check
            Calendar lastCal = helper.calendar(new Date(lastBackup));
            int lastYearDay = lastCal.get(Calendar.DAY_OF_YEAR);
            int lastHour = lastCal.get(Calendar.HOUR_OF_DAY);
            if (lastYearDay == yearDay && lastHour == hour) {
                Logger.d("{},{} same lastYearDay {} and hour {}, skip", yearDay, hour, lastYearDay, lastHour);
                return;
            }
        }

        Logger.d("start to backup");

        contexts.trackEvent(Contexts.getTrackerPath(getClass()), Contexts.TE.BACKUP + "a", "", null);

        DataBackupRestorer.Result r = new DataBackupRestorer().backup(canceling);
        if (r.isSuccess()) {
            final File lastFolder = r.getLastFolder();
            pref.setLastBackupTime(cal.getTime().getTime());
            Logger.d("backup finished");

            String msg = i18n.string(R.string.msg_db_backuped, r.getDb() + r.getPref(), r.getLastFolder());

            Notifications.send(contexts.getApp(), Notifications.nextGroupId(), msg, i18n.string(R.string.label_backup_data),
                    Notifications.Channel.BACKUP, Notifications.Level.INFO, null);

            //do drive sync if sing success
            GoogleDriveHelper.signIn(contexts.getApp()).addOnSuccessListener(new OnSuccessListener<GoogleDriveHelper>() {
                @Override
                public void onSuccess(final GoogleDriveHelper helper) {
                    Runnable job = new Runnable() {
                        @Override
                        public void run() {
                            //must not run in main thread. Tasks.await
                            GoogleDriveBackupRestorer.BackupResult result = new GoogleDriveBackupRestorer(helper).backup(lastFolder);
                            if (result.isSuccess()) {
                                Logger.d("drive-backup finished");
                                String msg = i18n.string(R.string.msg_db_backuped, result.getCount(), result.getFileName());

                                Notifications.send(contexts.getApp(), Notifications.nextGroupId(), msg, i18n.string(R.string.label_google_drive),
                                        Notifications.Channel.DRIVE, Notifications.Level.INFO, null);
                            } else {
                                Logger.w(result.getErr());
                                Notifications.send(contexts.getApp(), Notifications.nextGroupId(), result.getErr(), i18n.string(R.string.label_google_drive),
                                        Notifications.Channel.DRIVE, Notifications.Level.WARN, null);
                                contexts.trackEvent(Contexts.getTrackerPath(getClass()), Contexts.TE.DRIVE_BACKUP + "a-fail", "", null);
                            }
                        }
                    };
                    Threads.execute(job);
                }
            });
        } else {
            Logger.w(r.getErr());
            Notifications.send(contexts.getApp(), Notifications.nextGroupId(), r.getErr(), i18n.string(R.string.label_backup_data),
                    Notifications.Channel.BACKUP, Notifications.Level.WARN, null);
            contexts.trackEvent(Contexts.getTrackerPath(getClass()), Contexts.TE.BACKUP + "a-fail", "", null);
        }

    }


    public static synchronized AutoBackupRunnable singleton() {
        if (instance == null) {
            instance = new AutoBackupRunnable();
        }
        return instance;
    }

    public void cancel() {
        canceling.set(true);
    }
}
