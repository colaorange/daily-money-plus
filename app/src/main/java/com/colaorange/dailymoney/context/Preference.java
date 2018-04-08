package com.colaorange.dailymoney.context;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Logger;
import com.colaorange.dailymoney.ui.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Dennis
 */
public class Preference {


    int workingBookId = Contexts.WORKING_BOOK_DEFAULT;


    private CalendarHelper calendarHelper;

    int detailListLayout = 2;
    int maxRecords = -1;//-1 is no limit
    int firstdayWeek = 1;//sunday
    int startdayMonth = 1;//
    boolean openTestsDesktop = false;
    boolean backupCSV = true;
    String password = "";
    boolean allowAnalytics = true;
    String csvEncoding = "UTF8";
    boolean hierarchicalBalance = true;
    String lastbackup = "Unknown";

    ContextsApp contextsApp;

    public Preference(ContextsApp contextsApp) {
        this.contextsApp = contextsApp;
        calendarHelper = new CalendarHelper();
    }

    void reloadPreference() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);

        int bookid = 0;
        try {
            bookid = prefs.getInt(Constants.PREFS_WORKING_BOOK_ID, workingBookId);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        if (bookid < 0) {
            bookid = 0;
        }

        try {
            String pd1 = prefs.getString(Constants.PREFS_PASSWORD, password);
            String pd2 = prefs.getString(Constants.PREFS_PASSWORDVD, password);
            if (pd1.equals(pd2)) {
                password = pd1;
            } else {
                password = "";
            }
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }


        try {
            detailListLayout = Integer.parseInt(prefs.getString(Constants.PREFS_DETAIL_LIST_LAYOUT, String.valueOf(detailListLayout)));
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        try {
            firstdayWeek = Integer.parseInt(prefs.getString(Constants.PREFS_FIRSTDAY_WEEK, String.valueOf(firstdayWeek)));
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        try {
            startdayMonth = Integer.parseInt(prefs.getString(Constants.PREFS_STARTDAY_MONTH, String.valueOf(startdayMonth)));
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        try {
            maxRecords = Integer.parseInt(prefs.getString(Constants.PREFS_MAX_RECORDS, String.valueOf(maxRecords)));
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        try {
            openTestsDesktop = prefs.getBoolean(Constants.PREFS_OPEN_TESTS_DESKTOP, false);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }

        try {
            backupCSV = prefs.getBoolean(Constants.PREFS_BACKUP_CSV, backupCSV);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        try {
            allowAnalytics = prefs.getBoolean(Constants.PREFS_ALLOW_ANALYTICS, allowAnalytics);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        try {
            csvEncoding = prefs.getString(Constants.PREFS_CSV_ENCODING, csvEncoding);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }

        try {
            hierarchicalBalance = prefs.getBoolean(Constants.PREFS_HIERARCHICAL_REPORT, hierarchicalBalance);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }

        try {
            lastbackup = prefs.getString(Constants.PREFS_LAST_BACKUP, lastbackup);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }


        if (workingBookId != bookid) {
            workingBookId = bookid;
        }

        if (Contexts.DEBUG) {
            Logger.d("preference : working book " + workingBookId);
            Logger.d("preference : detail layout " + detailListLayout);
            Logger.d("preference : firstday of week " + firstdayWeek);
            Logger.d("preference : startday of month " + startdayMonth);
            Logger.d("preference : max records " + maxRecords);
            Logger.d("preference : open tests desktop " + openTestsDesktop);
            Logger.d("preference : backup csv " + backupCSV);
            Logger.d("preference : csv encoding " + csvEncoding);
            Logger.d("preference : last backup " + lastbackup);
        }
        calendarHelper.setFirstDayOfWeek(getFirstdayWeek());
        calendarHelper.setStartDayOfMonth(getStartdayMonth());
    }

    public int getWorkingBookId() {
        return workingBookId;
    }

    void setWorkingBookId(int id) {
        if (id < 0) {
            id = Contexts.WORKING_BOOK_DEFAULT;
        }
        if (workingBookId != id) {
            workingBookId = id;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Constants.PREFS_WORKING_BOOK_ID, id);
            editor.commit();
        }
    }

    public String getPassword() {
        return password;
    }

    public boolean isAllowAnalytics() {
        return allowAnalytics;
    }

    public String getCSVEncoding() {
        return csvEncoding;
    }

    public boolean isBackupCSV() {
        return backupCSV;
    }

    public boolean isHierarchicalBalance() {
        return hierarchicalBalance;
    }

    public void setHierarchicalBalance(boolean hierarchic) {
        hierarchicalBalance = hierarchic;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREFS_HIERARCHICAL_REPORT, hierarchicalBalance);
        editor.commit();
    }

    public int getDetailListLayout() {
        return detailListLayout;
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public int getFirstdayWeek() {
        return firstdayWeek;
    }

    public int getStartdayMonth() {
        return startdayMonth > 28 ? 28 : (startdayMonth < 1 ? 1 : startdayMonth);
    }

    public boolean isOpenTestsDesktop() {
        return openTestsDesktop;
    }

    public CalendarHelper getCalendarHelper() {
        return calendarHelper;
    }

    private DateFormat getLastBackupFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public void setLastBackupTime(long date) {
        DateFormat fmt = getLastBackupFormat();
        lastbackup = fmt.format(date);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREFS_LAST_BACKUP, lastbackup);
        editor.commit();
    }

    public String getLastBackup() {
        return lastbackup;
    }

    public Long getLastBackupTime() {
        DateFormat fmt = getLastBackupFormat();
        try {
            return fmt.parse(lastbackup).getTime();
        } catch (Exception x) {
        }
        return null;
    }
}
