package com.colaorange.dailymoney.context;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.util.I18N;
import com.colaorange.dailymoney.util.Logger;
import com.colaorange.dailymoney.R;
import com.colaorange.dailymoney.ui.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashSet;

/**
 * Created by Dennis
 */
public class Preference {

    public static final String FORMAT_DATE_YMD = "Y/M/D";
    public static final String FORMAT_DATE_MDY = "M/D/Y";
    public static final String FORMAT_DATE_DMY = "D/M/Y";
    public static final String FORMAT_TIME_12 = "12";
    public static final String FORMAT_TIME_24 = "24";
    public static final String FORMAT_MONTH_DIGITAL = "digital";
    public static final String FORMAT_MONTH_SHORT = "short";
    public static final String FORMAT_MONTH_FULL = "full";
    private static final LinkedHashSet<String> formatDateSet = new LinkedHashSet<>();
    private static final LinkedHashSet<String> formatTimeSet = new LinkedHashSet<>();
    private static final LinkedHashSet<String> formatMonthSet = new LinkedHashSet<>();

    static {
        formatDateSet.add(FORMAT_DATE_YMD);
        formatDateSet.add(FORMAT_DATE_MDY);
        formatDateSet.add(FORMAT_DATE_DMY);
        formatTimeSet.add(FORMAT_TIME_12);
        formatTimeSet.add(FORMAT_TIME_24);
        formatMonthSet.add(FORMAT_MONTH_DIGITAL);
        formatMonthSet.add(FORMAT_MONTH_SHORT);
        formatMonthSet.add(FORMAT_MONTH_FULL);

    }

    int workingBookId = Contexts.WORKING_BOOK_DEFAULT;


    private CalendarHelper calendarHelper;

    int detailListLayout = 2;
    int maxRecords = -1;//-1 is no limit
    int firstdayWeek = 1;//sunday
    int startdayMonth = 1;//1-28
    int startdayYearMonth = 0;//0-11
    int startdayYearMonthDay = 1;//1-28
    boolean openTestsDesktop = false;
    boolean backupWithTimestamp = true;
    String password = "";
    boolean allowAnalytics = true;
    String csvEncoding = "UTF8";
    boolean hierarchicalBalance = true;
    String lastbackup = "Unknown";

    String dateFormat;
    String timeFormat;
    String dateTimeFormat;
    String monthFormat;
    String monthDateFormat;
    String yearFormat;
    String yearMonthFormat;

    ContextsApp contextsApp;

    public Preference(ContextsApp contextsApp) {
        this.contextsApp = contextsApp;
        calendarHelper = new CalendarHelper();
    }

    void reloadPreference() {
        I18N i18n = Contexts.instance().getI18n();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);

        int bookId = 0;
        try {
            bookId = prefs.getInt(Constants.PREFS_WORKING_BOOK_ID, workingBookId);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        if (bookId < 0) {
            bookId = 0;
        }

        reloadSecurityPref(prefs, i18n);
        reloadDisplayPref(prefs, i18n);
        reloadAccountingPref(prefs, i18n);
        reloadDataPref(prefs, i18n);
        reloadContributionPref(prefs, i18n);
        reloadOtherPref(prefs, i18n);

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

        if (workingBookId != bookId) {
            workingBookId = bookId;
        }

        Logger.d("preference : last backup {}", lastbackup);
        Logger.d("preference : working book {}", workingBookId);

        calendarHelper.setFirstDayOfWeek(getFirstdayWeek());
        calendarHelper.setStartDayOfMonth(getStartdayMonth());
        calendarHelper.setStartMonthOfYear(getStartdayYearMonth());
        calendarHelper.setStartMonthDayOfYear(getStartdayYearMonthDay());
    }

    private void reloadContributionPref(SharedPreferences prefs, I18N i18n) {
        try {
            allowAnalytics = prefs.getBoolean(Constants.PREFS_ALLOW_ANALYTICS, allowAnalytics);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
    }

    private void reloadOtherPref(SharedPreferences prefs, I18N i18n) {

        try {
            csvEncoding = prefs.getString(Constants.PREFS_CSV_ENCODING, csvEncoding);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }

        try {
            openTestsDesktop = prefs.getBoolean(Constants.PREFS_OPEN_TESTS_DESKTOP, false);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        Logger.d("preference : csv encoding {}", csvEncoding);

        Logger.d("preference : open tests desktop {}", openTestsDesktop);
    }

    private void reloadDataPref(SharedPreferences prefs, I18N i18n) {
        try {
            backupWithTimestamp = prefs.getBoolean(Constants.PREFS_BACKUP_WITH_TIMESTAMP, backupWithTimestamp);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        Logger.d("preference : backup wiht timestamp {}", backupWithTimestamp);
    }

    private void reloadSecurityPref(SharedPreferences prefs, I18N i18n) {
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
        Logger.d("preference : password {}", Strings.isBlank(password) ? "" : "********");
    }

    private void reloadAccountingPref(SharedPreferences prefs, I18N i18n) {
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
            startdayYearMonth = Integer.parseInt(prefs.getString(Constants.PREFS_STARTDAY_YEAR_MONTH, String.valueOf(startdayYearMonth)));
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        try {
            startdayYearMonthDay = Integer.parseInt(prefs.getString(Constants.PREFS_STARTDAY_YEAR_MONTH_DAY, String.valueOf(startdayYearMonthDay)));
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }


        Logger.d("preference : firstday of week {}", firstdayWeek);
        Logger.d("preference : startday of month {}", startdayMonth);
        Logger.d("preference : startday of year {}/{}", startdayYearMonth,startdayYearMonthDay);
    }

    private void reloadDisplayPref(SharedPreferences prefs, I18N i18n) {

        try {
            detailListLayout = Integer.parseInt(prefs.getString(Constants.PREFS_DETAIL_LIST_LAYOUT, String.valueOf(detailListLayout)));
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        try {
            maxRecords = Integer.parseInt(prefs.getString(Constants.PREFS_MAX_RECORDS, String.valueOf(maxRecords)));
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }

        String formatDate = i18n.string(R.string.default_prefs_format_date);
        String formatTime = i18n.string(R.string.default_prefs_format_time);
        String formatMonth = i18n.string(R.string.default_prefs_format_month);
        try {
            formatDate = prefs.getString(Constants.PREFS_FORMAT_DATE, formatDate);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        if (!formatDateSet.contains(formatDate)) {
            formatDate = formatDateSet.iterator().next();
        }
        try {
            formatTime = prefs.getString(Constants.PREFS_FORMAT_TIME, formatTime);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        if (!formatTimeSet.contains(formatTime)) {
            formatTime = formatTimeSet.iterator().next();
        }
        try {
            formatMonth = prefs.getString(Constants.PREFS_FORMAT_MONTH, formatMonth);
        } catch (Exception x) {
            Logger.e(x.getMessage());
        }
        if (!formatMonthSet.contains(formatMonth)) {
            formatMonth = formatMonthSet.iterator().next();
        }

        boolean monthDigital = false;
        yearFormat = "yyyy";

        switch (formatMonth) {
            case FORMAT_MONTH_FULL:
                monthFormat = "MMMM";
                break;
            case FORMAT_MONTH_SHORT:
                monthFormat = "MMM";
                break;
            case FORMAT_MONTH_DIGITAL:
            default:
                monthFormat = "MM";
                monthDigital = true;
                break;
        }

        switch (formatTime) {
            case FORMAT_TIME_12:
                timeFormat = "aa hh:mm:ss";
                break;
            case FORMAT_TIME_24:
            default:
                timeFormat = "HH:mm:ss";
                break;
        }

        if (!monthDigital) {
            monthDateFormat = monthFormat + " dd";
            yearMonthFormat = yearFormat + " " + monthFormat;
        }

        switch (formatDate) {
            case FORMAT_DATE_DMY:
                dateFormat = "dd/MM/" + yearFormat;
                if (monthDigital) {
                    monthDateFormat = "dd/" + monthFormat;
                    yearMonthFormat = monthFormat + "/" + yearFormat;
                }
                break;
            case FORMAT_DATE_MDY:
                dateFormat = "MM/dd/" + yearFormat;
                if (monthDigital) {
                    monthDateFormat = monthFormat + "/dd";
                    yearMonthFormat = monthFormat + "/" + yearFormat;
                }
                break;
            case FORMAT_DATE_YMD:
            default:
                dateFormat = yearFormat + "/MM/dd";
                if (monthDigital) {
                    monthDateFormat = "dd/" + monthFormat;
                    yearMonthFormat = yearFormat + "/" + monthFormat;
                }
                break;
        }

        dateTimeFormat = dateFormat + " " + timeFormat;


        Logger.d("preference : dateFormat {}", dateFormat);
        Logger.d("preference : timeFormat {}", timeFormat);
        Logger.d("preference : dateTimeFormat {}", dateTimeFormat);
        Logger.d("preference : monthFormat {}", monthFormat);
        Logger.d("preference : monthDateFormat {}", monthDateFormat);
        Logger.d("preference : yearFormat {}", yearFormat);
        Logger.d("preference : yearMonthFormat {}", yearMonthFormat);
        Logger.d("preference : detail layout {}", detailListLayout);
        Logger.d("preference : max records {}", maxRecords);

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

    public int getStartdayYearMonth() {
        return startdayYearMonth;
    }

    public int getStartdayYearMonthDay() {
        return startdayYearMonthDay;
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

    public boolean isBackupWithTimestamp() {
        return backupWithTimestamp;
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

    public static final String DEFAULT_BACKUP_DATE_TIME_FORMAT = "yyMMdd-HHmmss";
    public static final String DEFAULT_BACKUP_Month_FORMAT = "yyMM";

    public DateFormat getBackupDateTimeFormat() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        try {
            String format = prefs.getString("backupDateTimeFormat", DEFAULT_BACKUP_DATE_TIME_FORMAT);
            return new SimpleDateFormat(format);
        } catch (Exception x) {
            Logger.w(x.getMessage());
        }
        return new SimpleDateFormat(DEFAULT_BACKUP_DATE_TIME_FORMAT);
    }

    public DateFormat getBackupMonthFormat() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        try {
            String format = prefs.getString("backupMonthFormat", DEFAULT_BACKUP_Month_FORMAT);
            return new SimpleDateFormat(format);
        } catch (Exception x) {
            Logger.w(x.getMessage());
        }
        return new SimpleDateFormat(DEFAULT_BACKUP_Month_FORMAT);
    }


    public DateFormat getDateFormat() {
        return new SimpleDateFormat(dateFormat);
    }

    public DateFormat getTimeFormat() {
        return new SimpleDateFormat(timeFormat);
    }

    public DateFormat getDateTimeFormat() {
        return new SimpleDateFormat(dateTimeFormat);
    }

    public DateFormat getMonthFormat() {
        return new SimpleDateFormat(monthFormat);
    }

    public DateFormat getMonthDateFormat() {
        return new SimpleDateFormat(monthDateFormat);
    }

    public DateFormat getYearFormat() {
        return new SimpleDateFormat(yearFormat);
    }

    public DateFormat getYearMonthFormat() {
        return new SimpleDateFormat(yearMonthFormat);
    }


    public void setLastBackupTime(long date) {
        lastbackup = getBackupDateTimeFormat().format(date);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREFS_LAST_BACKUP, lastbackup);
        editor.commit();
    }

    public String getLastBackup() {
        return lastbackup;
    }

    public Long getLastBackupTime() {
        try {
            return getBackupDateTimeFormat().parse(lastbackup).getTime();
        } catch (Exception x) {
        }
        return null;
    }
}
