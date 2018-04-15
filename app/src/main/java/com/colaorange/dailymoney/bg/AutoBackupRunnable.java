package com.colaorange.dailymoney.bg;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.context.Preference;
import com.colaorange.dailymoney.data.DataBackupRestorer;
import com.colaorange.dailymoney.util.GUIs;
import com.colaorange.dailymoney.util.I18N;
import com.colaorange.dailymoney.util.Logger;

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

    AtomicBoolean running = new AtomicBoolean(false);

    String errorDayHour;

    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            Logger.d("time ticker still running");
            return;
        }

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
        }
    }


    private void doAutoBackup(Contexts contexts, Preference pref, I18N i18n) throws Exception {
        Logger.d("start autobackup evaluation");

        DateFormat format = new SimpleDateFormat("yyMMddHH");

        CalendarHelper helper = pref.getCalendarHelper();
        Calendar cal = helper.calendar(new Date());

        Long lastBackup = pref.getLastBackupTime();

        //don't backup again in same err hour
        if (errorDayHour != null && errorDayHour.equals(format.format(cal.getTime()))) {
            Logger.d("same errorDayhour, skip");
            return;
        }

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
        DataBackupRestorer.Result r = DataBackupRestorer.backup();
        if (r.isSuccess()) {
            pref.setLastBackupTime(cal.getTime().getTime());
            Logger.d("backup finished");
            errorDayHour = null;

            String count = "" + (r.getDb() + r.getPref());
            String msg = i18n.string(R.string.msg_db_backuped, count, r.getLastFolder());

            GUIs.sendNotification(contexts.getApp(), GUIs.NotificationTarget.SYSTEM_BAR, GUIs.NotificationLevel.INFO,
                    i18n.string(R.string.label_backup_data), msg, null, 0);
        } else {
            Logger.w(r.getErr());
            errorDayHour = format.format(cal.getTime());
            GUIs.sendNotification(contexts.getApp(), GUIs.NotificationTarget.SYSTEM_BAR, GUIs.NotificationLevel.WARN,
                    i18n.string(R.string.label_backup_data), r.getErr(), null, 0);
        }

    }
}
