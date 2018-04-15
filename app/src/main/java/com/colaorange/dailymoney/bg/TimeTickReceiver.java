package com.colaorange.dailymoney.bg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.context.Contexts;
import com.colaorange.dailymoney.context.Preference;
import com.colaorange.dailymoney.data.DataBackupRestorer;
import com.colaorange.dailymoney.util.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dennis
 */
public class TimeTickReceiver extends BroadcastReceiver {

    AtomicBoolean running = new AtomicBoolean(false);
    String errorDayHour;

    public TimeTickReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.d("time ticker onReceive");

        if (!running.compareAndSet(false, true)) {
            Logger.d("time ticker still running");
            return;
        }

        try {
            Preference pref = Contexts.instance().getPreference();
            if (!pref.isAutoBackup()) {
                return;
            }
            doAutoBackup(pref);
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        } finally {
            running.set(false);
        }


    }

    private void doAutoBackup(Preference pref) throws Exception {
        Logger.d("start autobackup evaluation");

        DateFormat format = new SimpleDateFormat("yyMMddHH");

        CalendarHelper helper = pref.getCalendarHelper();
        Calendar cal = helper.calendar(new Date());

        Long lastBackup = pref.getLastBackupTime();

        //don't backup again in same err hour
        if(errorDayHour!=null && errorDayHour.equals(format.format(cal.getTime()))){
            Logger.d("same errorDayhour, skip");
            return;
        }

        Set<Integer> autoBackupWeekDays = pref.getAutoBackupWeekDays();
        Set<Integer> autoBackupAtHours = pref.getAutoBackupAtHours();
        int yearDay = cal.get(Calendar.DAY_OF_YEAR);
        int weekDay = cal.get(Calendar.DAY_OF_WEEK);
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if(!(autoBackupWeekDays.contains(weekDay) && autoBackupAtHours.contains(hour))){
            //not in week day & hour.
            Logger.d("{},{} not in weekday {} and hours {}, skip", weekDay, hour, autoBackupWeekDays, autoBackupAtHours);
            return;
        }

        if (lastBackup != null){
            //check
            Calendar lastCal = helper.calendar(new Date(lastBackup));
            int lastYearDay = lastCal.get(Calendar.DAY_OF_YEAR);
            int lastHour = lastCal.get(Calendar.HOUR_OF_DAY);
            if(lastYearDay==yearDay && lastHour==hour){
                Logger.d("{},{} same lastYearDay {} and hour {}, skip", yearDay, hour, lastYearDay, lastHour);
                return;
            }
        }

        Logger.d("start to backup");
        DataBackupRestorer.Result r = DataBackupRestorer.backup();
        if(r.isSuccess()){
            pref.setLastBackupTime(cal.getTime().getTime());
            Logger.d("backup finished");
            errorDayHour = null;
        }else{
            Logger.w(r.getErr());
            errorDayHour = format.format(cal.getTime());
        }

    }
}
