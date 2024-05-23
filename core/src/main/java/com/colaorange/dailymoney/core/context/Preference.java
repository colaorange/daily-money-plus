package com.colaorange.dailymoney.core.context;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.commons.util.Jsons;
import com.colaorange.commons.util.Objects;
import com.colaorange.commons.util.Security;
import com.colaorange.commons.util.Strings;
import com.colaorange.dailymoney.core.data.CardDesktop;
import com.colaorange.dailymoney.core.util.I18N;
import com.colaorange.dailymoney.core.util.Logger;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.ui.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * @author Dennis
 */
public class Preference {

    /**
     * WARN, DON'T CHANGE SALT, it effect all old password
     **/
    private static final String PASSWORD_SALT = "powerpuffgirls";

    /**
     * WARN, if you change this value, you have to check prefs.xml too
     */
    public static final String CARD_DESKTOP_ENABLE_PREFIX = "card-desktop-enable-";
    public static final String CARD_DESKTOP_PREFIX = "card-desktop-";

    public static final String EVER_FILE_NAME = "com.colaorange.ever";

    public static final String THEME_COLA = "cola";
    public static final String THEME_ORANGE = "orange";
    public static final String THEME_LEMON = "lemon";
    public static final String THEME_SAKURA = "sakura";
    public static final String THEME_LAVENDER = "lavender";

    public static class Theme {
        final String name;
        final int metaResId;
        final int bodyResId;

        public Theme(String name, int metaResId, int bodyResId) {
            this.name = name;
            this.metaResId = metaResId;
            this.bodyResId = bodyResId;
        }
    }

    private static final LinkedHashMap<String, Theme> themeMap = new LinkedHashMap<>();

    static {
        themeMap.put(THEME_COLA, new Theme(THEME_COLA, R.style.themeCola, R.style.themeCola_body));
        themeMap.put(THEME_ORANGE, new Theme(THEME_ORANGE, R.style.themeOrange, R.style.themeOrange_body));
        themeMap.put(THEME_SAKURA, new Theme(THEME_SAKURA, R.style.themeSakura, R.style.themeSakura_body));
        themeMap.put(THEME_LEMON, new Theme(THEME_LEMON, R.style.themeLemon, R.style.themeLemon_body));
        themeMap.put(THEME_LAVENDER, new Theme(THEME_LAVENDER, R.style.themeLavender, R.style.themeLavender_body));
    }

    public static final String TEXT_SIZE_NORMAL = "normal";
    public static final String TEXT_SIZE_MEDIUM = "medium";
    public static final String TEXT_SIZE_LARGE = "large";
    private static final LinkedHashSet<String> textSizeSet = new LinkedHashSet<>();

    static {
        textSizeSet.add(TEXT_SIZE_NORMAL);
        textSizeSet.add(TEXT_SIZE_MEDIUM);
        textSizeSet.add(TEXT_SIZE_LARGE);
    }

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

    int workingBookId = Contexts.DEFAULT_BOOK_ID;

    private String v29DocTreeRootUri = "content://com.android.externalstorage.documents/tree/primary%3AbwDailyMoney";

    private CalendarHelper calendarHelper;

    private int recordListLayout = 2;
    private int maxRecords = -1;//-1 is no limit
    private int firstdayWeek = 1;//sunday
    private int startdayMonth = 1;//1-28
    private int startdayYearMonth = 0;//0-11
    private int startdayYearMonthDay = 1;//1-28
    private boolean testsDesktop = false;
    private boolean backupWithTimestamp = true;
    private String passwordHash = "";
    private boolean allowAnalytics = true;
    private String csvEncoding = "UTF8";
    private boolean hierarchicalBalance = true;
    private String lastbackup = "Unknown";

    private String dateFormat;
    private String timeFormat;
    private String dateTimeFormat;
    private String monthFormat;
    private String nonDigitalMonthFormat;
    private String monthDateFormat;
    private String yearFormat;
    private String yearMonthFormat;
    private String dayFormat;
    private boolean groupRecordsByDate = true;

    private boolean autoBackup = true;


    private String lastFromAccount;
    private String lastToAccount;

    private String theme;

    private String textSize;

    private Integer autoBackupDueDays;

    private boolean logOn = false;
    private int logMaxLine = 1000;


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
            Logger.e(x.getMessage(), x);
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
            Logger.e(x.getMessage(), x);
        }

        try {
            lastbackup = prefs.getString(Constants.PREFS_LAST_BACKUP, lastbackup);
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        }

        try {
            lastFromAccount = prefs.getString(Constants.PREFS_LAST_FROM_ACCOUNT, lastFromAccount);
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        }

        try {
            lastToAccount = prefs.getString(Constants.PREFS_LAST_TO_ACCOUNT, lastToAccount);
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
        }

        if (workingBookId != bookId) {
            workingBookId = bookId;
        }

        try {
            v29DocTreeRootUri = prefs.getString(Constants.PREFS_V29_DOC_TREE_ROOT_URI, v29DocTreeRootUri);
        } catch (Exception x) {
            Logger.e(x.getMessage(), x);
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
            allowAnalytics = Objects.coerceToBoolean(i18n.string(R.string.default_pref_allow_analytics), allowAnalytics);
            allowAnalytics = prefs.getBoolean(i18n.string(R.string.pref_allow_analytics), allowAnalytics);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
    }

    private void reloadOtherPref(SharedPreferences prefs, I18N i18n) {

        try {
            csvEncoding = i18n.string(R.string.default_pref_csv_encoding, csvEncoding);
            csvEncoding = prefs.getString(i18n.string(R.string.pref_csv_encoding), csvEncoding);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        try {
            testsDesktop = Objects.coerceToBoolean(i18n.string(R.string.default_pref_testsdekstop), testsDesktop);
            testsDesktop = prefs.getBoolean(i18n.string(R.string.pref_testsdekstop), testsDesktop);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        try {
            logOn = Objects.coerceToBoolean(i18n.string(R.string.default_pref_log_on), logOn);
            logOn = prefs.getBoolean(i18n.string(R.string.pref_log_on), logOn);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        try {
            logMaxLine = Objects.coerceToInteger(i18n.string(R.string.default_pref_log_max_line), logMaxLine);
            logMaxLine = Integer.parseInt(prefs.getString(i18n.string(R.string.pref_log_max_line), Integer.toString(logMaxLine)));
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }


        Logger.d("preference : csv encoding {}", csvEncoding);
        Logger.d("preference : open tests desktop {}", testsDesktop);
        Logger.d("preference : logOn {}", logOn);
        Logger.d("preference : max log line {}", logMaxLine);
    }

    private void reloadDataPref(SharedPreferences prefs, I18N i18n) {
        try {
            backupWithTimestamp = Objects.coerceToBoolean(i18n.string(R.string.default_pref_backup_with_timestamp), backupWithTimestamp);
            backupWithTimestamp = prefs.getBoolean(i18n.string(R.string.pref_backup_with_timestamp), backupWithTimestamp);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        try {
            autoBackup = Objects.coerceToBoolean(i18n.string(R.string.default_pref_auto_backup), autoBackup);
            autoBackup = prefs.getBoolean(i18n.string(R.string.pref_auto_backup), autoBackup);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        try {
            String str = i18n.string(R.string.default_pref_auto_backup_due_days);

            str = prefs.getString(i18n.string(R.string.pref_auto_backup_due_days), str);

            autoBackupDueDays = Integer.parseInt(str);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        Logger.d("preference : backup with timestamp {}", backupWithTimestamp);
        Logger.d("preference : autoBackup {}", autoBackup);
        Logger.d("preference : autoBackupDueDays {}", autoBackupDueDays);
    }

    private void reloadSecurityPref(SharedPreferences prefs, I18N i18n) {
        String oldPwdHash = null;
        try {
            //since 0.9.9-180418 we enhance it to password hahsh
            //clear them, they are plaintext in old version
            SharedPreferences.Editor editor = null;
            String pd1 = prefs.getString(i18n.string(R.string.pref_password), "");
            String pd2 = prefs.getString(i18n.string(R.string.pref_passwordvd), "");
            if (!Strings.isBlank(pd1)) {
                editor = editor == null ? prefs.edit() : editor;
                editor.remove(i18n.string(R.string.pref_password));
            }
            if (!Strings.isBlank(pd2)) {
                editor = editor == null ? prefs.edit() : editor;
                editor.remove(i18n.string(R.string.pref_passwordvd));
            }

            if (!Strings.isBlank(pd1) && pd1.equals(pd2)) {
                //set to new password hash
                editor = editor == null ? prefs.edit() : editor;
                editor.putString(i18n.string(R.string.pref_password_hash), oldPwdHash = Preference.passwordMD5(pd1));
            }

            if (editor != null) {
                editor.commit();
            }

        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        try {
            passwordHash = oldPwdHash == null ? prefs.getString(i18n.string(R.string.pref_password_hash), "") : oldPwdHash;
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        Logger.d("preference : passwordHash {}", Strings.isBlank(passwordHash) ? "NO" : "********");
    }

    private void reloadAccountingPref(SharedPreferences prefs, I18N i18n) {
        String str;
        try {
            str = i18n.string(R.string.default_pref_firstday_week);
            firstdayWeek = Integer.parseInt(prefs.getString(i18n.string(R.string.pref_firstday_week), str));
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        try {
            str = i18n.string(R.string.default_pref_startday_month);
            startdayMonth = Integer.parseInt(prefs.getString(i18n.string(R.string.pref_startday_month), str));
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        try {
            str = i18n.string(R.string.default_pref_startday_year_month);
            startdayYearMonth = Integer.parseInt(prefs.getString(i18n.string(R.string.pref_startday_year_month), str));
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        try {
            str = i18n.string(R.string.default_pref_startday_year_month_day);
            startdayYearMonthDay = Integer.parseInt(prefs.getString(i18n.string(R.string.pref_startday_year_month_day), str));
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }


        Logger.d("preference : firstday of week {}", firstdayWeek);
        Logger.d("preference : startday of month {}", startdayMonth);
        Logger.d("preference : startday of year {}/{}", startdayYearMonth, startdayYearMonthDay);
    }

    private void reloadDisplayPref(SharedPreferences prefs, I18N i18n) {
        String str;

        try {
            str = i18n.string(R.string.default_pref_record_list_layout);
            recordListLayout = Integer.parseInt(prefs.getString(i18n.string(R.string.pref_record_list_layout), str));
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        try {
            str = i18n.string(R.string.default_pref_max_records);
            maxRecords = Integer.parseInt(prefs.getString(i18n.string(R.string.pref_max_records), str));
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        try {
            groupRecordsByDate = Objects.coerceToBoolean(i18n.string(R.string.default_pref_group_records_by_date));
            groupRecordsByDate = prefs.getBoolean(i18n.string(R.string.pref_group_records_by_date), groupRecordsByDate);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }

        String formatDate = i18n.string(R.string.default_pref_format_date);
        String formatTime = i18n.string(R.string.default_pref_format_time);
        String formatMonth = i18n.string(R.string.default_pref_format_month);
        try {
            formatDate = prefs.getString(i18n.string(R.string.pref_format_date), formatDate);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        if (!formatDateSet.contains(formatDate)) {
            formatDate = formatDateSet.iterator().next();
        }
        try {
            formatTime = prefs.getString(i18n.string(R.string.pref_format_time), formatTime);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        if (!formatTimeSet.contains(formatTime)) {
            formatTime = formatTimeSet.iterator().next();
        }
        try {
            formatMonth = prefs.getString(i18n.string(R.string.pref_format_month), formatMonth);
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        if (!formatMonthSet.contains(formatMonth)) {
            formatMonth = formatMonthSet.iterator().next();
        }

        boolean monthDigital = false;
        yearFormat = "yyyy";
        dayFormat = "dd";

        switch (formatMonth) {
            case FORMAT_MONTH_FULL:
                nonDigitalMonthFormat = monthFormat = "MMMM";
                break;
            case FORMAT_MONTH_SHORT:
                nonDigitalMonthFormat = monthFormat = "MMM";
                break;
            case FORMAT_MONTH_DIGITAL:
            default:
                nonDigitalMonthFormat = "MMM";
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
                    monthDateFormat = monthFormat + "/dd";
                    yearMonthFormat = yearFormat + "/" + monthFormat;
                }
                break;
        }

        dateTimeFormat = dateFormat + " " + timeFormat;

        try {
            theme = prefs.getString(i18n.string(R.string.pref_theme), i18n.string(R.string.default_pref_theme));
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        if (!themeMap.containsKey(theme)) {
            theme = THEME_LEMON;
        }

        try {
            textSize = prefs.getString(i18n.string(R.string.pref_text_size), i18n.string(R.string.default_pref_text_size));
        } catch (Exception x) {
            Logger.w(x.getMessage(), x);
        }
        if (!textSizeSet.contains(textSize)) {
            textSize = textSizeSet.iterator().next();
        }


        Logger.d("preference : theme {}", theme);
        Logger.d("preference : textSize {}", textSize);
        Logger.d("preference : dateFormat {}", dateFormat);
        Logger.d("preference : timeFormat {}", timeFormat);
        Logger.d("preference : dateTimeFormat {}", dateTimeFormat);
        Logger.d("preference : monthFormat {}", monthFormat);
        Logger.d("preference : monthDateFormat {}", monthDateFormat);
        Logger.d("preference : yearFormat {}", yearFormat);
        Logger.d("preference : yearMonthFormat {}", yearMonthFormat);
        Logger.d("preference : record layout {}", recordListLayout);
        Logger.d("preference : group records by date {}", groupRecordsByDate);
        Logger.d("preference : max records {}", maxRecords);

    }

    public int getWorkingBookId() {
        return workingBookId;
    }

    void setWorkingBookId(int id) {
        if (id < 0) {
            id = Contexts.DEFAULT_BOOK_ID;
        }
        if (workingBookId != id) {
            workingBookId = id;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Constants.PREFS_WORKING_BOOK_ID, id);
            editor.commit();
        }
    }

    public String getV29DocTreeRootUri() {
        return v29DocTreeRootUri;
    }

    public void setV29DocTreeRootUri(String uri) {
        if (uri != null && !v29DocTreeRootUri.equals(uri)) {
            v29DocTreeRootUri = uri;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(Constants.PREFS_V29_DOC_TREE_ROOT_URI, uri);
            editor.commit();
        }
    }

    public int getStartdayYearMonth() {
        return startdayYearMonth;
    }

    public int getStartdayYearMonthDay() {
        return startdayYearMonthDay;
    }

    public String getPasswordHash() {
        return passwordHash;
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

    public int getRecordListLayout() {
        return recordListLayout;
    }

    public int getMaxRecords() {
        return maxRecords;
    }

    public boolean isGroupRecordsByDate() {
        return groupRecordsByDate;
    }

    public int getFirstdayWeek() {
        return firstdayWeek;
    }

    public int getStartdayMonth() {
        return startdayMonth > 28 ? 28 : (startdayMonth < 1 ? 1 : startdayMonth);
    }

    public boolean isTestsDesktop() {
        return testsDesktop;
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

    public DateFormat getWeekDayFormat() {
        //TODO config? do we need to ?
        return new SimpleDateFormat("EEE");
    }

    public DateFormat getTimeFormat() {
        return new SimpleDateFormat(timeFormat);
    }

    public DateFormat getTimeFormatWithoutSecond() {
        return new SimpleDateFormat(timeFormat.replace(":ss", ""));
    }

    public DateFormat getDateTimeFormat() {
        return new SimpleDateFormat(dateTimeFormat);
    }

    public DateFormat getMonthFormat() {
        return new SimpleDateFormat(monthFormat);
    }

    public DateFormat getNonDigitalMonthFormat() {
        return new SimpleDateFormat(nonDigitalMonthFormat);
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

    public DateFormat getDayFormat() {
        return new SimpleDateFormat(dayFormat);
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

    public boolean isAutoBackup() {
        return autoBackup;
    }

    public Integer getAutoBackupDueDays() {
        return autoBackupDueDays;
    }

    public static String passwordMD5(String pwd) {
        if (Strings.isEmpty(pwd)) {
            return "";
        }
        return Security.md5String(pwd + Preference.PASSWORD_SALT);
    }


    public Theme getTheme() {
        return themeMap.get(theme);
    }


    public String getTextSize() {
        return textSize;
    }

    public String getLastFromAccount() {
        return lastFromAccount;
    }

    public void setLastAccount(String lastFromAccount, String lastToAccount) {
        this.lastFromAccount = lastFromAccount;
        this.lastToAccount = lastToAccount;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PREFS_LAST_FROM_ACCOUNT, lastFromAccount);
        editor.putString(Constants.PREFS_LAST_TO_ACCOUNT, lastToAccount);
        editor.commit();
    }

    public String getLastToAccount() {
        return lastToAccount;
    }

    public RecordTemplateCollection getRecordTemplates() {
        int bookid = getWorkingBookId();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        String json = prefs.getString("templates-" + bookid, null);

        RecordTemplateCollection templates = null;
        if (json != null) {
            try {
                templates = Jsons.fromJson(json, RecordTemplateCollection.class);
                templates.book = bookid;
            } catch (Exception x) {
                Logger.w(x.getMessage(), x);
            }
        }
        if (templates == null) {
            templates = new RecordTemplateCollection(bookid);
        }
        return templates;
    }

    public void updateRecordTemplates(RecordTemplateCollection templates) {
        int bookid = getWorkingBookId();
        templates.book = bookid;
        String json = templates.toJson();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("templates-" + bookid, json);
        editor.commit();
    }

    public void clearRecordTemplates(int bookid) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("templates-" + bookid);
        editor.commit();
    }

    private final int alwaysEnabledDesktopSize = 1;
    private final int maxDesktopSize = 4;

    public int getDesktopSize() {
        return maxDesktopSize;
    }

    public boolean isAnyDesktop() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        for (int i = 0; i < getDesktopSize(); i++) {
            String json = prefs.getString(CARD_DESKTOP_PREFIX + i, null);
            if (!Strings.isBlank(json)) {
                return true;
            }
        }
        return false;
    }

    public boolean isLogOn() {
        return logOn;
    }

    public int getLogMaxLine() {
        return logMaxLine;
    }

    public boolean isDesktopEnabled(int index) {

        if (index < alwaysEnabledDesktopSize) {
            return true;
        }

        if (index >= getDesktopSize()) {
            return false;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        return prefs.getBoolean(CARD_DESKTOP_ENABLE_PREFIX + index, false);
    }

    public void updateDesktopEnable(int index, boolean enabled) {
        if (index >= getDesktopSize()) {
            throw new ArrayIndexOutOfBoundsException(index + ">=" + getDesktopSize());
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(CARD_DESKTOP_ENABLE_PREFIX + index, enabled);
        editor.commit();
    }

    public CardDesktop getDesktop(int index) {
        if (index >= getDesktopSize()) {
            throw new ArrayIndexOutOfBoundsException(index + ">=" + getDesktopSize());
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        String json = prefs.getString(CARD_DESKTOP_PREFIX + index, null);

        CardDesktop cards = null;
        if (json != null) {
            try {
                cards = Jsons.fromJson(json, CardDesktop.class);
            } catch (Exception x) {
                Logger.w(x.getMessage(), x);
            }
        }
        if (cards == null) {
            cards = new CardDesktop();
        }
        return cards;
    }

    public void updateDesktop(int index, CardDesktop cards) {
        updateDesktop(index, cards, null);
    }

    public void updateDesktop(int index, CardDesktop desktop, Boolean enabled) {
        if (index >= getDesktopSize()) {
            throw new ArrayIndexOutOfBoundsException(index + ">=" + getDesktopSize());
        }
        String json = desktop.toJson();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CARD_DESKTOP_PREFIX + index, json);
        if (enabled != null) {
            editor.putBoolean(CARD_DESKTOP_ENABLE_PREFIX + index, enabled);
        }
        editor.commit();
    }

    public void removeDesktop(int index) {
        if (index >= getDesktopSize()) {
            throw new ArrayIndexOutOfBoundsException(index + ">=" + getDesktopSize());
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contextsApp);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(CARD_DESKTOP_PREFIX + index);
        editor.remove(CARD_DESKTOP_ENABLE_PREFIX + index);
        editor.commit();
    }

    public boolean checkEver(String key, boolean setIfNever) {
        SharedPreferences prefs = contextsApp.getSharedPreferences(EVER_FILE_NAME, Context.MODE_PRIVATE);
        boolean ever = prefs.getBoolean(key, false);
        if (!ever && setIfNever) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(key, true);
            editor.commit();
        }
        return ever;
    }

}
