package com.colaorange.dailymoney.core.util;

import android.content.pm.PackageManager;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.R;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.context.Preference;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Dennis
 */
public class Misc {

    public static boolean isPermissionGranted(String permission, String[] permissionRequests, int[] grantResults) {
        for (int i = 0; i < permissionRequests.length; i++) {
            if (permission.equals(permissionRequests[i])) {
                if (grantResults.length > i) {
                    return grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
                return false;
            }
        }
        return false;
    }

    public static String toBalancePeriodInfo(PeriodMode periodMode, Date targetDate, boolean fromBeginning) {
        Contexts contexts = Contexts.instance();
        CalendarHelper cal = contexts.getCalendarHelper();
        I18N i18n = contexts.getI18n();
        Preference prefs = contexts.getPreference();
        DateFormat yearMonthFormat = prefs.getYearMonthFormat();
        DateFormat monthDateFormat = prefs.getMonthDateFormat();
        DateFormat yearFormat = prefs.getYearFormat();

        String info = "";

        if (fromBeginning) {
            if (PeriodMode.MONTHLY.equals(periodMode)) {
                Date monthStart = cal.monthStartDate(targetDate);
                Date monthEnd = cal.monthEndDate(targetDate);
                info = i18n.string(R.string.label_until_time, yearMonthFormat.format(monthStart),
                        monthDateFormat.format(monthEnd));
            } else if (PeriodMode.YEARLY.equals(periodMode)) {
                Date yearStart = cal.yearStartDate(targetDate);
                Date yearEnd = cal.yearEndDate(targetDate);

                info = i18n.string(R.string.label_until_time, yearFormat.format(yearStart),
                        yearFormat.format(yearEnd) + " " + monthDateFormat.format(yearEnd));
            } else if (PeriodMode.WEEKLY.equals(periodMode)) {
                Date weekStart = cal.weekStartDate(targetDate);
                Date weekEnd = cal.weekEndDate(targetDate);

                info = i18n.string(R.string.label_until_time, yearFormat.format(weekStart),
                        monthDateFormat.format(weekEnd));
            }
        } else {
            if (PeriodMode.MONTHLY.equals(periodMode)) {
                Date monthStart = cal.monthStartDate(targetDate);
                Date monthEnd = cal.monthEndDate(targetDate);
                info = i18n.string(R.string.label_in_time, yearMonthFormat.format(monthStart),
                        monthDateFormat.format(monthStart), monthDateFormat.format(monthEnd));
            } else if (PeriodMode.YEARLY.equals(periodMode)) {
                Date yearStart = cal.yearStartDate(targetDate);
                Date yearEnd = cal.yearEndDate(targetDate);
                info = i18n.string(R.string.label_in_time, yearFormat.format(yearStart),
                        monthDateFormat.format(yearStart), yearFormat.format(yearEnd) + " " + monthDateFormat.format(yearEnd));
            } else if (PeriodMode.WEEKLY.equals(periodMode)) {
                Date weekStart = cal.weekStartDate(targetDate);
                Date weekEnd = cal.weekEndDate(targetDate);
                info = i18n.string(R.string.label_in_time, yearFormat.format(weekStart),
                        monthDateFormat.format(weekStart), monthDateFormat.format(weekEnd));
            }
        }
        return info;
    }

    public static Date[] toTargetPeriodDates(PeriodMode periodMode, Date targetDate){
        Contexts contexts = Contexts.instance();
        CalendarHelper cal = contexts.getCalendarHelper();

        Date targetStartDate = null;
        Date targetEndDate = null;

        switch (periodMode) {
            case ALL:
                break;
            case MONTHLY:
                targetStartDate = cal.monthStartDate(targetDate);
                targetEndDate = cal.monthEndDate(targetDate);
                break;
            case DAILY:
                targetStartDate = cal.toDayStart(targetDate);
                targetEndDate = cal.toDayEnd(targetDate);
                break;
            case YEARLY:
                targetStartDate = cal.yearStartDate(targetDate);
                targetEndDate = cal.yearEndDate(targetDate);

                break;
            case WEEKLY:
            default:
                targetStartDate = cal.weekStartDate(targetDate);
                targetEndDate = cal.weekEndDate(targetDate);
                break;
        }
        return new Date[] {targetStartDate,targetEndDate};
    }

    public static String toRecordPeriodInfo(PeriodMode periodMode, Date targetDate, int count) {

        Contexts contexts = Contexts.instance();
        CalendarHelper cal = contexts.getCalendarHelper();
        I18N i18n = contexts.getI18n();
        Preference prefs = contexts.getPreference();
        DateFormat yearMonthFormat = prefs.getYearMonthFormat();
        DateFormat monthDateFormat = prefs.getMonthDateFormat();
        DateFormat yearFormat = prefs.getYearFormat();
        DateFormat dateFormat = prefs.getDateFormat();//new SimpleDateFormat("yyyy/MM/dd");
        DateFormat weekDayFormat = prefs.getWeekDayFormat();
        DateFormat nonDigitalMonthFormat = prefs.getNonDigitalMonthFormat();


        Date[] pDates = toTargetPeriodDates(periodMode, targetDate);
        Date targetStartDate = pDates[0];
        Date targetEndDate = pDates[1];

        String info = "";

        final boolean sameYear = targetStartDate == null || targetEndDate == null ? false : cal.isSameYear(targetStartDate, targetEndDate);
        final boolean sameMonth = targetStartDate == null || targetEndDate == null ? false : cal.isSameMonth(targetStartDate, targetEndDate);

        StringBuilder sb = new StringBuilder();
        //update info
        switch (periodMode) {
            case ALL:
                info = i18n.string(R.string.label_all_records, Integer.toString(count));
                break;
            case MONTHLY:
                //<string name="label_month_details">%1$s (%2$s records)</string>
                if (sameMonth) {
                    sb.append(yearMonthFormat.format(targetStartDate));
                } else {
                    sb.append(yearFormat.format(targetStartDate));
                    sb.append(" ").append(nonDigitalMonthFormat.format(targetStartDate)).append(", ").append(cal.dayOfMonth(targetStartDate)).append(" - ")
                            .append(nonDigitalMonthFormat.format(targetEndDate)).append(" ").append(cal.dayOfMonth(targetEndDate));
                }
                info = i18n.string(R.string.label_month_records, sb.toString(), Integer.toString(count));
                break;
            case DAILY:
                info = i18n.string(R.string.label_day_records, dateFormat.format(targetDate) + " " + weekDayFormat.format(targetDate), Integer.toString(count));
                break;
            case YEARLY:

                //<string name="label_year_details">%1$s (%2$s records)</string>
                if (sameYear) {
                    sb.append(yearFormat.format(targetStartDate));
                } else {
                    sb.append(yearFormat.format(targetStartDate));
                    sb.append(", ").append(nonDigitalMonthFormat.format(targetStartDate)).append(" ").append(cal.dayOfMonth(targetStartDate)).append(" - ")
                            .append(yearFormat.format(targetEndDate)).append(" ").append(nonDigitalMonthFormat.format(targetEndDate)).append(" ").append(cal.dayOfMonth(targetEndDate));
                }

                info = i18n.string(R.string.label_year_records, sb.toString(), Integer.toString(count));
                break;
            case WEEKLY:
            default:
                //<string name="label_week_details">%5$s %1$s to %2$s - Week %3$s/%4$s (%6$s)</string>
                info = i18n.string(R.string.label_week_records, nonDigitalMonthFormat.format(targetStartDate) + " " + cal.dayOfMonth(targetStartDate),
                        (!sameMonth ? nonDigitalMonthFormat.format(targetEndDate) + " " : "") + cal.dayOfMonth(targetEndDate),
                        cal.weekOfMonth(targetDate), cal.weekOfYear(targetDate), yearFormat.format(targetStartDate), Integer.toString(count));
                break;
        }
        return info;

    }
}
