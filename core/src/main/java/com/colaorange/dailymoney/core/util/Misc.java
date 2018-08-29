package com.colaorange.dailymoney.core.util;

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


    public static String toPeriodInfo(PeriodMode periodMode, Date targetDate, boolean fromBeginning) {
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
}
