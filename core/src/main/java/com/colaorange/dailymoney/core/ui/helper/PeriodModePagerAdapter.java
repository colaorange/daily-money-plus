package com.colaorange.dailymoney.core.ui.helper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.colaorange.commons.util.CalendarHelper;
import com.colaorange.dailymoney.core.context.Contexts;
import com.colaorange.dailymoney.core.context.PeriodMode;
import com.colaorange.dailymoney.core.ui.Constants;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Dennis
 */
public abstract class PeriodModePagerAdapter extends FragmentPagerAdapter {

    protected Date baseDate;
    protected PeriodMode periodMode;
    protected CalendarHelper calHelper;
    protected int basePos;
    protected int maxPos;

    public PeriodModePagerAdapter(FragmentManager fm, Date baseDate, PeriodMode periodMode) {
        super(fm);
        this.baseDate = baseDate;
        this.periodMode = periodMode;
        calHelper = Contexts.instance().getCalendarHelper();
        calculatePos();
    }

    private void calculatePos() {
        Calendar cal0 = calHelper.calendar(new Date(0));
        Calendar calbase = calHelper.calendar(baseDate);

        int diffYear = calbase.get(Calendar.YEAR) - cal0.get(Calendar.YEAR);

        if (PeriodMode.MONTHLY.equals(periodMode)) {
            basePos = diffYear * 12 + calbase.get(Calendar.MONTH) - cal0.get(Calendar.MONTH);
        } else if (PeriodMode.WEEKLY.equals(periodMode)) {
            basePos = (diffYear * 365 + calbase.get(Calendar.DAY_OF_YEAR) - cal0.get(Calendar.DAY_OF_YEAR)) / 7;
        } else if (PeriodMode.YEARLY.equals(periodMode)) {
            basePos = diffYear;
        }
        basePos -= 1;//just for prevent hit boundary

        if (PeriodMode.MONTHLY.equals(periodMode)) {
            maxPos = basePos + Constants.MONTH_LOOK_AFTER;
        } else if (PeriodMode.WEEKLY.equals(periodMode)) {
            maxPos = basePos + Constants.WEEK_LOOK_AFTER;
        } else if (PeriodMode.YEARLY.equals(periodMode)) {
            maxPos = basePos + Constants.YEAR_LOOK_AFTER;
        }

        maxPos = basePos + (PeriodMode.MONTHLY.equals(periodMode) ? Constants.MONTH_LOOK_AFTER : Constants.YEAR_LOOK_AFTER);
    }

    public int getBasePos() {
        return basePos;
    }

    @Override
    public int getCount() {
        return maxPos;
    }

    @Override
    public Fragment getItem(int position) {
        Date targetDate;

        int diff = position - basePos;
        if (PeriodMode.MONTHLY.equals(periodMode)) {
            targetDate = calHelper.monthAfter(baseDate, diff);
        } else if (PeriodMode.WEEKLY.equals(periodMode)) {
            targetDate = calHelper.dateAfter(baseDate, diff * 7);
        }else {
            targetDate = calHelper.yearAfter(baseDate, diff);
        }

        return getItem(position, targetDate);
    }

    protected abstract Fragment getItem(int position, Date targetDate);


}